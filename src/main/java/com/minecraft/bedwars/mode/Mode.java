package com.minecraft.bedwars.mode;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.bedwars.Bedwars;
import com.minecraft.bedwars.config.IslandConfiguration;
import com.minecraft.bedwars.config.MapConfiguration;
import com.minecraft.bedwars.event.UserConnectEvent;
import com.minecraft.bedwars.event.UserDeathEvent;
import com.minecraft.bedwars.event.UserDisconnectEvent;
import com.minecraft.bedwars.event.type.DeathType;
import com.minecraft.bedwars.room.Room;
import com.minecraft.bedwars.room.generator.VoidGenerator;
import com.minecraft.bedwars.room.island.Island;
import com.minecraft.bedwars.room.team.Team;
import com.minecraft.bedwars.room.team.column.TeamColor;
import com.minecraft.bedwars.user.User;
import com.minecraft.bedwars.util.HandleInfos;
import com.minecraft.bedwars.util.enums.Items;
import com.minecraft.bedwars.util.enums.RoomStage;
import com.minecraft.bedwars.util.generator.Generator;
import com.minecraft.bedwars.util.generator.task.GeneratorTask;
import com.minecraft.bedwars.util.generator.type.GeneratorType;
import com.minecraft.bedwars.util.visibility.Visibility;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.server.bedwars.BedwarsType;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.compression.WinRAR;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.github.paperspigot.Title;

import java.io.File;
import java.io.FileReader;
import java.util.*;

@RequiredArgsConstructor
@Getter
public abstract class Mode implements BukkitInterface, Listener {

    private static final Bedwars plugin = Bedwars.getInstance();

    @Setter
    private String name = getClass().getSimpleName();
    private final Set<BedwarsType> supportedModes;
    private final int minRooms;
    private final int maxRooms;
    private final List<File> maps;
    private JsonObject jsonReader;
    private World world;
    @Setter
    private Columns wins, loses, winstreak, winstreakRecord, games, rating;

    public void load() {

        String name = getClass().getSimpleName();

        plugin.getLogger().info("Loading " + name);

        File directory = new File("/home/ubuntu/wisemc/misc/bedwars/" + name.toLowerCase());
        File[] binaryMaps = directory.listFiles();

        if (binaryMaps == null || binaryMaps.length == 0) {
            System.out.println("No maps found for " + name);
            return;
        }

        Collections.addAll(maps, binaryMaps);

        int mapLoop = 0;

        for (int i = 0; i < minRooms; i++) {

            int id = i + 1;
            String mapName = name.toLowerCase() + "-" + id;

            plugin.getLogger().info("Loading " + mapName + " for " + name);

            File mapDirectory = new File(Bukkit.getWorldContainer(), mapName);
            File map = maps.get(mapLoop);
            WinRAR.unzip(map, mapDirectory);

            WorldCreator creator = new WorldCreator(mapName);
            creator.generateStructures(false);
            creator.generator(VoidGenerator.getInstance());

            World world = this.world = Bukkit.createWorld(creator);

            Room room = new Room(id, this, world);

            if (mapLoop + 1 >= maps.size())
                mapLoop = 0;
            else
                mapLoop++;

            MapConfiguration mapConfiguration = new MapConfiguration();

            try {
                File fileConfiguration = new File(mapDirectory, "map.json");
                JsonObject json = this.jsonReader = Constants.JSON_PARSER.parse(new FileReader(fileConfiguration)).getAsJsonObject();

                mapConfiguration.setName(json.get("name").getAsString());
                mapConfiguration.setSize(json.get("size").getAsInt());
                mapConfiguration.setHeight(json.get("max_y").getAsInt());

                String rawSpawn = json.get("spawn").getAsString();
                String[] coordinates = rawSpawn.split(", ");
                if (coordinates.length != 5) {
                    System.out.println("Map " + mapConfiguration.getName() + " is broken! Skipping it...");
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    Bedwars.getInstance().getRoomStorage().delete(folder);
                    continue;
                }

                double x = Double.parseDouble(coordinates[0]);
                double y = Double.parseDouble(coordinates[1]);
                double z = Double.parseDouble(coordinates[2]);
                float yaw = Float.parseFloat(coordinates[3]);
                float pitch = Float.parseFloat(coordinates[4]);

                mapConfiguration.setSpawnPoint(new Location(world, x,y,z, yaw, pitch));
                room.setMapConfiguration(mapConfiguration);
                setup(room);
            } catch (Exception e) {
                System.out.println("Failed to load map " + map.getName());
                e.printStackTrace();
                continue;
            }

            IslandConfiguration islandConfiguration = new IslandConfiguration();

            try {
                File fileConfiguration = new File(mapDirectory, "islands.json");
                JsonObject json = Constants.JSON_PARSER.parse(new FileReader(fileConfiguration)).getAsJsonObject();

                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("islands").entrySet()) {
                    String teamColor = entry.getKey();
                    JsonObject islandData = entry.getValue().getAsJsonObject();

                    islandConfiguration.setTeamColor(teamColor);
                    islandConfiguration.setSize(islandData.get("size").getAsInt());
                    islandConfiguration.setHeight(islandData.get("height").getAsInt());

                    String rawSpawn = islandData.get("spawnPoint").getAsString();
                    String[] spawnCoordinates = rawSpawn.split(", ");
                    if (spawnCoordinates.length != 6) {
                        plugin.getLogger().severe("[DEBUG] Island for team " + islandConfiguration.getTeamColor() + " is broken! Coordinates: " + Arrays.toString(spawnCoordinates));
                        continue;
                    }

                    Location spawnPoint = new Location(
                            world,
                            Double.parseDouble(spawnCoordinates[1]),
                            Double.parseDouble(spawnCoordinates[2]),
                            Double.parseDouble(spawnCoordinates[3]),
                            Float.parseFloat(spawnCoordinates[4]),
                            Float.parseFloat(spawnCoordinates[5])
                    );
                    islandConfiguration.setSpawnPoint(spawnPoint);

                    // Shop Location
                    String rawShopLocation = islandData.get("shopLocation").getAsString();
                    String[] shopCoordinates = rawShopLocation.split(", ");
                    if (shopCoordinates.length == 6) {
                        Location shopLocation = new Location(
                                world,
                                Double.parseDouble(shopCoordinates[1]),
                                Double.parseDouble(shopCoordinates[2]),
                                Double.parseDouble(shopCoordinates[3]),
                                Float.parseFloat(shopCoordinates[4]),
                                Float.parseFloat(shopCoordinates[5])
                        );
                        islandConfiguration.setShopLocation(shopLocation);
                    }

                    // Upgrade Location
                    String rawUpgradeLocation = islandData.get("upgradeLocation").getAsString();
                    String[] upgradeCoordinates = rawUpgradeLocation.split(", ");
                    if (upgradeCoordinates.length == 6) {
                        Location upgradeLocation = new Location(
                                world,
                                Double.parseDouble(upgradeCoordinates[1]),
                                Double.parseDouble(upgradeCoordinates[2]),
                                Double.parseDouble(upgradeCoordinates[3]),
                                Float.parseFloat(upgradeCoordinates[4]),
                                Float.parseFloat(upgradeCoordinates[5])
                        );
                        islandConfiguration.setUpgradeLocation(upgradeLocation);
                    }

                    // Bed Location
                    String rawBedLocation = islandData.get("bedLocation").getAsString();
                    String[] bedCoordinates = rawBedLocation.split(", ");
                    if (bedCoordinates.length == 6) {
                        Location bedLocation = new Location(
                                world,
                                Double.parseDouble(bedCoordinates[1]),
                                Double.parseDouble(bedCoordinates[2]),
                                Double.parseDouble(bedCoordinates[3]),
                                Float.parseFloat(bedCoordinates[4]),
                                Float.parseFloat(bedCoordinates[5])
                        );
                        islandConfiguration.setBedLocation(bedLocation);
                    }

                    // Generator Location
                    String rawGeneratorLocation = islandData.get("generatorLocation").getAsString();
                    String[] generatorCoordinates = rawGeneratorLocation.split(", ");
                    if (generatorCoordinates.length == 6) {
                        Location generatorLocation = new Location(
                                world,
                                Double.parseDouble(generatorCoordinates[1]),
                                Double.parseDouble(generatorCoordinates[2]),
                                Double.parseDouble(generatorCoordinates[3]),
                                Float.parseFloat(generatorCoordinates[4]),
                                Float.parseFloat(generatorCoordinates[5])
                        );
                        islandConfiguration.setGeneratorLocation(generatorLocation);
                    }

                    UUID islandId = UUID.randomUUID();
                    Team team = new Team(TeamColor.valueOf(teamColor.toUpperCase()).getDisplay(), TeamColor.valueOf(teamColor.toUpperCase()));
                    Island island = new Island(islandId, team);

                    island.setSpawnLocation(islandConfiguration.getSpawnPoint());

                    island.setShopLocation(islandConfiguration.getShopLocation());
                    island.setUpgradeLocation(islandConfiguration.getUpgradeLocation());

                    island.setBedLocation(islandConfiguration.getBedLocation());

                    island.setGeneratorLocation(islandConfiguration.getGeneratorLocation());

                    island.getTeam().getLocationList().add(0, island.getSpawnLocation());
                    island.getTeam().getLocationList().add(1, island.getBedLocation());
                    island.getTeam().getLocationList().add(2, island.getShopLocation());
                    island.getTeam().getLocationList().add(3, island.getUpgradeLocation());
                    island.getTeam().getLocationList().add(4, island.getGeneratorLocation());
                    island.getGenerators().add(new GeneratorTask(new Generator(GeneratorType.IRON, island.getGeneratorLocation())));
                    island.getGenerators().add(new GeneratorTask(new Generator(GeneratorType.GOLD, island.getGeneratorLocation())));

                    room.getTeams().add(team);

                    plugin.getLogger().info("[DEBUG] Loaded bed for team: " + teamColor);

                    room.getIslands().add(island);

                    plugin.getLogger().info("[DEBUG] Loaded island for team: " + teamColor);
                }

                plugin.getLogger().info("[DEBUG] All islands loaded successfully.");
            } catch (Exception e) {
                plugin.getLogger().severe("[ERROR] Failed to load islands from islands.json");
                e.printStackTrace();
            }

            plugin.getRoomStorage().register(room);

            plugin.getLogger().info("§f[DEBUG] Loaded room: §7" + room.getCode() + "§f for mode §7" + name);
        }
    }

    public void tick(Room room) {
        RoomStage stage = room.getStage();
        boolean isFull = room.isFull();
        int time = room.getTime();

        if (stage == RoomStage.WAITING && room.isMinPlayersToStart()) {
            room.setStage(RoomStage.STARTING);
            room.setTime(4);
        } else if (stage == RoomStage.STARTING) {

            if (!room.isMinPlayersToStart()) {
                room.setStage(RoomStage.WAITING);
                room.setTime(-1);
                return;
            }

            room.setTime(time = time - 1);

            if (time == 0)
                room.start();
            else if (time <= 3) {
                final Title title = new Title("§c" + time, "§ePrepare-se para a Batalha!", 1, 15, 10);

                int finalTime = time;

                room.getWorld().getPlayers().forEach(c -> {
                    c.sendTitle(title);
                    c.sendMessage(Account.fetch(c.getPlayer().getUniqueId()).getLanguage().translate("duels.start_announcement", finalTime));
                    c.playSound(c.getLocation(), Sound.CLICK, 3.5F, 3.5F);
                });
            }

        } else if (stage == RoomStage.PLAYING) {

//            if (room.getPlayers().size() == 1) {
//                room.getPlayers().forEach(winner -> {
//                    winner.getPlayer().sendMessage("aaa");
//                });
//            }
//
//            room.setTime(time + 1);
        } else if (stage == RoomStage.ENDING) {

            room.setTime(time = time + 1);

            if (room.getWorld().getPlayers().isEmpty()) {
                room.setWin(-1);
                room.rollback();
            } else if (room.getWin() + 6 == time) {

                Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.BEDWARS_LOBBY);

                room.getWorld().getPlayers().forEach(player -> {

                    final Account account = Account.fetch(player.getUniqueId());

                    if (server != null) {
                        account.connect(server);
                    } else
                        sync(() -> player.kickPlayer(account.getLanguage().translate("arcade.room.not_found")));
                });
                /*  });*/

                room.setWin(-1);
                room.rollback();
            }
        }

        room.getWorld().getPlayers().forEach(c -> handleSidebar(User.fetch(c.getUniqueId())));
    }

    public void setup(Room room) {

        World world = room.getWorld();

        world.setPVP(true);
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("naturalRegeneration", "false");
        world.setGameRuleValue("sendCommandFeedback", "false");
        world.setGameRuleValue("logAdminCommands", "false");

        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MIN_VALUE);
        world.setThunderDuration(Integer.MIN_VALUE);

        world.setSpawnLocation(0, 71, 0);
        world.setAutoSave(false);
        world.setTime(6000);

        WorldBorder worldBorder = world.getWorldBorder();

        worldBorder.setSize(5000);

        int radius = 100;
        Location spawn = room.getMapConfiguration().getSpawnPoint();

        for (int x = (spawn.getBlockX() - radius) >> 4, endX = (spawn.getBlockX() + radius) >> 4; x <= endX; x++) {
            for (int z = (spawn.getBlockZ() - radius) >> 4, endZ = (spawn.getBlockZ() + radius) >> 4; z <= endZ; z++) {
                if (Math.sqrt(Math.pow((x << 4) - spawn.getBlockX(), 2) + Math.pow((z << 4) - spawn.getBlockZ(), 2)) <= radius) {
                    world.getChunkAt(x, z).load(false);
                }
            }
        }

        world.getEntities().forEach(Entity::remove);
    }

    public void join(User user, PlayMode playMode) {
        Player player = user.getPlayer();

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getOpenInventory().getTopInventory().clear();
        player.setLevel(0);
        player.setExp(0);

        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());

        player.setFireTicks(0);

        if (playMode == PlayMode.PLAYER) {
            player.setGameMode(GameMode.SURVIVAL);
            player.spigot().setCollidesWithEntities(true);
            player.setFlying(false);
            player.setAllowFlight(false);
            if (user.getTeam() == null) Items.find(user.getAccount().getLanguage()).build(player);

            if (Vanish.getInstance().isVanished(player.getUniqueId()))
                Vanish.getInstance().setVanished(player, null);

            new UserConnectEvent(user, true).fire();
        } else {
            Account account = Account.fetch(player.getUniqueId());
            if (account.hasPermission(Rank.PARTNER_PLUS)) {
                Vanish.getInstance().setVanished(player, account.getRank());
            } else {
                player.setGameMode(GameMode.SURVIVAL);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.spigot().setCollidesWithEntities(false);
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false), true);
                if (user.getTeam() == null) Items.find(user.getAccount().getLanguage()).build(player);
            }
        }

        Visibility.refresh(player);

        if (user.getTeam() == null) Items.find(user.getAccount().getLanguage()).build(player);

        if (((CraftPlayer) player).getHandle().playerConnection != null)
            player.getInventory().setHeldItemSlot(2);
    }

    public void quit(User user) {

        Room room = user.getRoom();

        if (room.isSpectator(user)) {
            user.getRoom().getSpectators().remove(user);
        } else {
            if (room.getStage() != RoomStage.PLAYING) {
                new UserDisconnectEvent(user, true).fire();
            } else {
                new UserDeathEvent(user, null, DeathType.QUIT, true);
            }
        }

        Player player = user.getPlayer();

        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());

        player.setFireTicks(0);

        if (room.isLock()) {
            room.setLock(null);
        }
    }

    public void start(Room room) {
        int playerIndex = 0;

        for (var island : room.getIslands()) {
            for (var player : room.getPlayers()) {
                new HandleInfos().handleNPCs(player, island);
                island.getGenerators().forEach(GeneratorTask::task);
            }
        }

        for (User user : room.getPlayers()) {
            Team team = room.getTeams().get(playerIndex % room.getTeams().size());

            var player = user.getPlayer();

            team.getMembers().add(user);
            user.setTeam(team);

            spawn(user);

            player.sendMessage("");
            player.sendMessage("§4§l* §cA aliança com outros jogadores sem ser do próprio time é proibida e resultará em §lpunição§c.");
            player.sendMessage("");

            playerIndex++;
        }
    }

    public Island getIslandForTeam(Room room, Team team) {
        for (Island island : room.getIslands()) {
            if (island.getTeam().equals(team)) {
                return island;
            }
        }
        return null;
    }

    public void spawn(User user) {
        var team = user.getTeam();

        Location spawnPoint = team.getLocationList().get(0);

        var player = user.getPlayer();

        if (spawnPoint != null) {
            player.setNoDamageTicks(20 * 5);
            player.setMaxHealth(20.0D);
            player.setHealth(20.0D);
            player.teleport(spawnPoint);
            user.setState(User.State.PLAYING);

            user.getRoom().refreshTablist(user.getAccount());

            player.getInventory().clear();

            player.setFlying(false);
            player.setAllowFlight(false);

            player.getActivePotionEffects().clear();

            player.getInventory().setItem(0, new ItemFactory().setType(Material.WOOD_SWORD).getStack());
            player.getInventory().setArmorContents(team.getArmor());
        }
    }

    public void handleSidebar(User user) {

        GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            user.setScoreboard(gameScoreboard = new GameScoreboard(user.getPlayer()));

        Room room = user.getRoom();

        if (room == null)
            return;

        RoomStage stage = room.getStage();

        List<String> scores = new ArrayList<>();

        int time = room.getTime();

        switch (stage) {
            case WAITING:
            case STARTING:
                gameScoreboard.updateTitle("§b§lBED WARS");
                scores.clear();
                scores.add(" ");
                scores.add("Mapa: §a" + room.getMapConfiguration().getName());
                scores.add("Modo: §7" + room.getMode().getName());
                scores.add(" ");
                scores.add("Players: §a" + room.getAlivePlayers().size() + "/" + room.getMaxPlayers());
                scores.add(" ");
                scores.add(time == -1 ? "Em aguardo..." : "Início em §a" + time + "s");

                scores.add(" ");
                scores.add("Winstreak: §7" + user.getAccount().getDataStorage().getData(Columns.BEDWARS_SOLO_WINSTREAK).getAsInteger());

                scores.add(" ");
                scores.add("§e" + Constants.SERVER_WEBSITE.replace("www.", ""));

                gameScoreboard.updateLines(scores);
                break;
            case PLAYING:
            case ENDING:
            case ROLLBACKING:
                gameScoreboard.updateTitle("§b§lBED WARS");
                scores.clear();
                scores.add(" ");
                scores.add("Diamante em: §7-/-");
                scores.add(" ");

                for (Team team : room.getTeams()) {
                    String teamLine = team.getColor().getChatColor() + team.getColor().getCharAt() + "  §f" + team.getDisplay() + " " + (team.getMembers().isEmpty() ? "§c✘" : team.isBedBroken() ? ("§a" + team.getMembers().size()) : "§a✔") + (team.getMembers().contains(user) ? " §7*" : "");
                    scores.add(teamLine);
                }

                scores.add(" ");
                scores.add("§e" + Constants.SERVER_WEBSITE.replace("www.", ""));

                gameScoreboard.updateLines(scores);
                break;
        }
    }

    public Mode(int minRooms, int maxRooms, BedwarsType... bedwarsTypes) {
        this.minRooms = minRooms;
        this.maxRooms = maxRooms;
        this.maps = new ArrayList<>();
        this.supportedModes = new HashSet<>(Arrays.asList(bedwarsTypes));
        Bukkit.getPluginManager().registerEvents(this, Bedwars.getInstance());
    }

    public boolean isCanBuild() {
        return true;
    }

    public boolean isAllowDrops() {
        return true;
    }

    public void refresh(User user) {
        Account account = user.getAccount();
        Tag tag = account.getProperty("account_tag").getAs(Tag.class);
        PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);
        new PlayerUpdateTablistEvent(account, tag, prefixType).fire();
    }
}