package org.aresclient.ares.module

import org.aresclient.ares.renderer.Texture

enum class Category {
    PLAYER,
    COMBAT,
    MOVEMENT,
    RENDER,
    HUD,
    MISC;

    val prettyName = name.lowercase().replaceFirstChar { it.uppercaseChar() }
    val icon by lazy { Texture(Category::class.java.getResourceAsStream("/assets/ares/textures/icons/categories/${name.lowercase()}.png"), false) }
    val modules = arrayListOf<Module>()
}
