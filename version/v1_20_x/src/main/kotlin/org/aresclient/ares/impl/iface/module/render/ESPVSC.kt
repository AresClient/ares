package org.aresclient.ares.impl.iface.module.render

import net.meshmc.mesh.loader.Mod
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.util.math.MathHelper
import org.aresclient.ares.api.minecraft.math.Box
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Grouped
import org.aresclient.ares.impl.instrument.global.Render
import org.aresclient.ares.impl.instrument.module.modules.render.ESP

@Mod.Interface
class ESPVSC: ESP.VSC {
    override fun render(delta: Float, buffers: Renderer.Buffers?, matrixStack: MatrixStack?, entities: Grouped<ESP.EntityTrait, ESP.Type>) {
        MinecraftClient.getInstance().world?.entities?.forEach {  entity ->
            val type = if(entity.type == EntityType.PLAYER) ESP.Type.PLAYER else ESP.Type.OTHER
            entities.trait(type)?.color?.value?.let {
                Render.Lines.box(entity.getInterpolatedBoundingBox(delta), it, 2f)
            }
        }
    }

    private fun Entity.getInterpolatedBoundingBox(delta: Float): Box {
        val x = MathHelper.lerp(delta.toDouble(), lastRenderX, x) - x
        val y = MathHelper.lerp(delta.toDouble(), lastRenderY, y) - y
        val z = MathHelper.lerp(delta.toDouble(), lastRenderZ, z) - z
        return Box.create(
            boundingBox.minX + x, boundingBox.minY + y, boundingBox.minZ + z,
            boundingBox.maxX + x, boundingBox.maxY + y, boundingBox.maxZ + z
        )
    }
}