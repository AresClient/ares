package org.aresclient.ares.impl.instrument.modules.hud

import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import org.aresclient.ares.api.nrender.font.TextColor
import org.aresclient.ares.impl.util.WorldUtil
import org.joml.Vector2d

object Coordinates: TextHudModule("Coordinates", "Displays the players coordinates on the hud", position = Vector2d(1.0, 1.0)) {
    private val nether = settings.addBoolean("Nether/Overworld", true)
    private val facing = settings.addBoolean("Facing", true)

    override fun onMotion() {
        if(MC.NULL) return
        update()
    }

    override fun getText(): Text? {
        if(MC.NULL) return null

        val pos = CAMERA.blockPos.add(Direction.DOWN.vector)
        var text = "${TextColor.GRAY}XYZ ${TextColor.WHITE}${pos.toFormattedString()}"
        if(nether.value) text += when(WorldUtil.getDimension()) {
            World.OVERWORLD -> " ${TextColor.GRAY}[Nether ${TextColor.WHITE}${pos.divideXZ(8.0).toFormattedString()}${TextColor.GRAY}]"
            World.NETHER -> " ${TextColor.GRAY}[Overworld ${TextColor.WHITE}${pos.multiplyXZ(8.0).toFormattedString()}${TextColor.GRAY}]"
            else -> ""
        }
        if(facing.value) text += " ${TextColor.GRAY}(Facing ${TextColor.WHITE}${SELF.horizontalFacing.toFormattedString()}${TextColor.GRAY})"
        return Text.literal(text)
    }

    private fun BlockPos.toFormattedString() = "$x, $y, $z"

    private fun BlockPos.divideXZ(value: Double) = BlockPos((x / value).toInt(), y, (z / value).toInt())
    private fun BlockPos.multiplyXZ(value: Double) = BlockPos((x * value).toInt(), y, (z * value).toInt())

    private fun Direction.toFormattedString() = when(this) {
        Direction.NORTH -> "-Z"
        Direction.SOUTH -> "+Z"
        Direction.WEST -> "-X"
        Direction.EAST -> "+X"
        else -> ""
    }
}
