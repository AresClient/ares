package org.aresclient.ares.module

enum class Category {
    PLAYER,
    COMBAT,
    MOVEMENT,
    RENDER,
    HUD,
    MISC;

    val modules = arrayListOf<Module>()
}
