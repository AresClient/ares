package org.aresclient.ares.impl

import org.aresclient.ares.api.PlayerEntity

open class PlayerEntityWrapper(entity: net.minecraft.entity.player.PlayerEntity) : EntityWrapper(entity), PlayerEntity {
}