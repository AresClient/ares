package org.aresclient.ares.api.nrender

import net.minecraft.client.texture.TextureManager
import net.minecraft.util.Identifier
import org.aresclient.ares.Ares

object Textures {
    private val textures = mutableListOf<Identifier>()

    val logo_bg = add("icons/ares_bg.png")
    val logo_fg = add("icons/ares_fg.png")
    val minecraft = add("icons/minecraft.png")
    val exit = add("icons/exit.png")

    init {
        textures.addAll((0..5).map { Ares.identifier("textures/panorama/panorama_$it.jpg") })
    }

    private fun add(path: String): Identifier {
        val identifier = Ares.identifier("textures/$path")
        textures.add(identifier)
        return identifier
    }

    fun register(textureManager: TextureManager) {
        textures.forEach(textureManager::registerTexture)
    }
}
