package org.aresclient.ares.impl.instrument.modules.render.esp

import net.minecraft.entity.Entity
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import org.aresclient.ares.api.nrender.drawer.WorldDrawer

object BoxESP: ESP.RenderMode() {
    override fun draw(entity: Entity, group: ESP.EntityGroup, drawer: WorldDrawer, delta: Float) {
        val box = entity.getInterpolatedBoundingBox(delta)

        drawer.fillBox(box, group.fillColor.value)
        drawer.outlineBox(box, group.lineColor.value, 2f)
    }

    private fun Entity.getInterpolatedBoundingBox(delta: Float): Box {
        val camera = ESP.MC.gameRenderer.camera.pos
        val x = MathHelper.lerp(delta.toDouble(), lastRenderX, x) - x - camera.x
        val y = MathHelper.lerp(delta.toDouble(), lastRenderY, y) - y - camera.y
        val z = MathHelper.lerp(delta.toDouble(), lastRenderZ, z) - z - camera.z
        return Box(
            boundingBox.minX + x, boundingBox.minY + y, boundingBox.minZ + z,
            boundingBox.maxX + x, boundingBox.maxY + y, boundingBox.maxZ + z
        )
    }
}
