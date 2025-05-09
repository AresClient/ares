package org.aresclient.ares.impl.util

import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import org.aresclient.ares.api.Wrapper
import kotlin.math.sqrt

object Comparators: Wrapper {
    object EntityDistance: Comparator<Entity> {
        override fun compare(p1: Entity?, p2: Entity?): Int {
            val one = sqrt(SELF.distanceTo(p1)).toDouble()
            val two = sqrt(SELF.distanceTo(p2)).toDouble()
            return one.compareTo(two)
        }
    }

    object BlockDistance: Comparator<BlockPos> {
        override fun compare(pos1: BlockPos, pos2: BlockPos): Int {
            val one = sqrt(SELF.squaredDistanceTo(pos1.x + 0.5, pos1.y + 0.5, pos1.z + 0.5))
            val two = sqrt(SELF.squaredDistanceTo(pos2.x + 0.5, pos2.y + 0.5, pos2.z + 0.5))
            return two.compareTo(one)
        }
    }
}