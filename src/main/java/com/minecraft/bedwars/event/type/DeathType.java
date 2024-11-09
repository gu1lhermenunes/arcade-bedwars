package com.minecraft.bedwars.event.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor
public enum DeathType {
    VOID("§7{victim} §emorreu no void."),
    VOID_FOR_ATTACKER("§7{victim} §efoi derrubado no void por §7{attacker}"),
    PLAYER("§7{victim} §efoi morto por §7{attacker}"),
    NULL("§7{victim} §emorreu."),
    QUIT("§7{victim} §edesconectou."),
    FALL_FOR_ATTACKER("§7{victim} §efoi derrubado do alto por §7{attacker}"),
    FALL("§7{victim} §efoi eliminado ao quebrar as pernas.");

    private final String message;
}
