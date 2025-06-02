package org.aresclient.ares.impl.instrument.modules.movement

import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.PlayerInput
import net.minecraft.util.math.BlockPos
import org.aresclient.ares.api.instruments.Module

object SafeWalk: Module(Category.MOVEMENT, "SafeWalk", "Keeps you from walking off ledges") {
    enum class Mode {
        CLIP, GRIM
    }

    private val mode = settings.addEnum("Mode",
        Mode.CLIP
    )
    private val tolerance = settings.addDouble("Tolerance", 0.0).setMin(0.0).setMax(0.5).setVisibility { mode.value == Mode.CLIP }

    fun getTolerance(): Double = tolerance.value
    fun shouldAlterTolerance() = isEnabled() && mode.value == Mode.CLIP
    fun shouldClip() = isEnabled()

    // see MixinPlayerEntity for clip mode
    // below is grim mode

    private var wasSneaking: Boolean? = null

    override fun onTick() {
        if(mode.value != Mode.GRIM || MC.NULL) return

        if(shouldSneak()) {
            val input = with(SELF.input.playerInput) {
                PlayerInput(
                    forward, backward, left, right,
                    jump, true, false
                )
            }

            if(wasSneaking == null) {
                wasSneaking = MC.options.sneakKey.isPressed
                //MC.networkHandler?.sendPacket(PlayerInputC2SPacket(input))
            }
            SELF.input.playerInput = input
            SELF.isSneaking = true
            MC.options.sneakKey.isPressed = true
        } else if(wasSneaking != null) {
            MC.options.sneakKey.isPressed = wasSneaking!!
            SELF.isSneaking = wasSneaking!!
            wasSneaking = null
        }
    }

    private fun shouldSneak(): Boolean {
        return canFallUnderneath(SELF.blockPos) && SELF.isOnGround
    }

    private fun canFallUnderneath(blockPos: BlockPos): Boolean {
        val under = blockPos.add(0, -1, 0)
        val state = WORLD.getBlockState(under)
        return state.isAir || (!state.isFullCube(WORLD, under) && !state.isIn(BlockTags.SLABS) && !state.isIn(BlockTags.STAIRS))
    }

    override fun getInfo() = mode.value.name
}
