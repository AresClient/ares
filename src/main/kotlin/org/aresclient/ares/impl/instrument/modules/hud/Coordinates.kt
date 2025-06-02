package org.aresclient.ares.impl.instrument.modules.hud

import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import org.aresclient.ares.api.nrender.TextColor
import org.joml.Vector2d

object Coordinates: TextHudModule("Coordinates", "Displays the players coordinates on the hud", position = Vector2d(0.0, 1.0)) {
    private val nether = settings.addBoolean("Nether", true)
    private val facing = settings.addBoolean("Facing", true)

    override fun onMotion() {
        if(MC.NULL) return
        update()
    }

    override fun getText(): Text? {
        if(MC.NULL) return null

        val pos = CAMERA.blockPos.add(Direction.DOWN.vector)
        var text = "${TextColor.GRAY}XYZ ${TextColor.WHITE}${pos.toFormattedString()}"
        if(nether.value) text += " ${TextColor.GRAY}[Nether ${TextColor.WHITE}${pos.divide(8.0).toFormattedString()}${TextColor.GRAY}]"
        if(facing.value) text += " ${TextColor.GRAY}(Facing ${TextColor.WHITE}${SELF.horizontalFacing.toFormattedString()}${TextColor.GRAY})"
        return Text.literal(text)
    }

    private fun BlockPos.toFormattedString() = "$x, $y, $z"

    private fun BlockPos.divide(value: Double) = BlockPos((x / value).toInt(), (y / value).toInt(), (z / value).toInt())

    private fun Direction.toFormattedString() = when(this) {
        Direction.NORTH -> "-Z"
        Direction.SOUTH -> "+Z"
        Direction.WEST -> "-X"
        Direction.EAST -> "+X"
        else -> ""
    }
}
