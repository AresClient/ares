package org.aresclient.ares.module

enum class Category {
    COMBAT,
    EXPLOIT,
    HUD,
    MOVEMENT,
    PLAYER,
    RENDER,
    MISC;

    val modules = arrayListOf<Module>()
}