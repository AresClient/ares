package org.aresclient.ares.impl.instrument.module.modules.hud

import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.aresclient.ares.api.render.TextColor

object Coordinates: TextHudModule("Coordinates", "Displays the players coordinates on the hud", position = 0.0 to 1.0) {
    private val nether = settings.addBoolean("Nether", true)
    private val facing = settings.addBoolean("Facing", true)

    override fun onMotion() {
        if(MC.NULL) return
        update()
    }

    override fun getText(): String? {
        if(MC.NULL) return null

        var text = "${TextColor.GRAY}XYZ ${TextColor.WHITE}${SELF.pos.toFormattedString()}"
        if(nether.value) text += " ${TextColor.GRAY}[Nether ${TextColor.WHITE}${SELF.pos.multiply(0.125).toFormattedString()}${TextColor.GRAY}]"
        if(facing.value) text += " ${TextColor.GRAY}(Facing ${TextColor.WHITE}${SELF.horizontalFacing.toFormattedString()}${TextColor.GRAY})"
        return text
    }

    private fun Vec3d.toFormattedString() = "${x.toInt()}, ${y.toInt()}, ${z.toInt()}"

    private fun Direction.toFormattedString() = when(this) {
        Direction.NORTH -> "-Z"
        Direction.SOUTH -> "+Z"
        Direction.WEST -> "-X"
        Direction.EAST -> "+X"
        else -> ""
    }
}
