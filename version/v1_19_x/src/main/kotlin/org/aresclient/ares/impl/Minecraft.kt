package org.aresclient.ares.impl

import net.meshmc.mesh.loader.Mod
import net.minecraft.client.MinecraftClient
import org.aresclient.ares.api.*
import org.aresclient.ares.renderer.Resolution
import org.aresclient.ares.renderer.Screen

@Mod.Interface
class Minecraft: IMinecraft {
    override fun nullCheck(): Boolean = MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null
    override fun getLoadedEntities(): MutableIterable<Entity> = EntityIterable(MinecraftClient.getInstance().world!!.entities)
    override fun getPlayer(): ClientPlayerEntity = ClientPlayerEntityWrapper(MinecraftClient.getInstance().player!!)
    override fun getRenderer(): IRenderer = Renderer

    override fun getResolution(): Resolution =
        Resolution(
            MinecraftClient.getInstance().window.width,
            MinecraftClient.getInstance().window.height,
            MinecraftClient.getInstance().window.scaledWidth,
            MinecraftClient.getInstance().window.scaledHeight,
            MinecraftClient.getInstance().window.scaleFactor
        )

    override fun getFrameBuffer(): FrameBuffer = MinecraftClient.getInstance().framebuffer as FrameBuffer
    override fun getBufferRenderer(): BufferRenderer = BufferRendererWrapper
    override fun openScreen(screen: Screen) {
        MinecraftClient.getInstance().setScreen(ScreenAdapter(screen))
    }

    override fun closeScreen() {
        MinecraftClient.getInstance().setScreen(null)
    }

    override fun shutdown() {
        MinecraftClient.getInstance().scheduleStop()
    }

    override fun getTextRenderer(): TextRenderer = MinecraftClient.getInstance().textRenderer as TextRenderer
}