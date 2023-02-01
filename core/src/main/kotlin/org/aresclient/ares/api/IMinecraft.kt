package org.aresclient.ares.api

import org.aresclient.ares.renderer.Resolution
import org.aresclient.ares.renderer.Screen

interface IMinecraft {
    fun nullCheck(): Boolean

    fun getLoadedEntities(): MutableIterable<Entity>

    fun getPlayer(): ClientPlayerEntity

    fun getRenderer(): IRenderer

    fun getResolution(): Resolution

    fun getFrameBuffer(): FrameBuffer

    fun getBufferRenderer(): BufferRenderer

    fun openScreen(screen: Screen)

    fun closeScreen()

    fun shutdown()

    fun getTextRenderer(): TextRenderer
}