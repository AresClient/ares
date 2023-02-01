package org.aresclient.ares.impl

import org.aresclient.ares.api.Entity
import org.aresclient.ares.api.EntityType
import org.aresclient.ares.api.math.Box
import org.aresclient.ares.api.math.Vec3d
import org.aresclient.ares.mixininterface.IEntityType


open class EntityWrapper(val entity: net.minecraft.entity.Entity): Entity {
    override fun getEntityType(): EntityType {
        return EntityType.VALUES[(entity.type as IEntityType).ordinal()]
    }

    override fun isSameAs(entity: Entity?): Boolean {
        if(entity == null) return false
        return (entity as EntityWrapper).entity == this.entity
    }

    override fun getBoundingBox(): Box {
        return entity.boundingBox as Box
    }

    override fun getPrevX(): Double {
        return entity.prevX
    }

    override fun getPrevY(): Double {
        return entity.prevY
    }

    override fun getPrevZ(): Double {
        return entity.prevZ
    }

    override fun getPos(): Vec3d {
        return entity.pos as Vec3d
    }

    override fun getX(): Double {
        return entity.x
    }

    override fun getY(): Double {
        return entity.y
    }

    override fun getZ(): Double {
        return entity.z
    }

    override fun getLastRenderX(): Double {
        return entity.lastRenderX
    }

    override fun getLastRenderY(): Double {
        return entity.lastRenderY
    }

    override fun getLastRenderZ(): Double {
        return entity.lastRenderZ
    }

    override fun isOnGround(): Boolean {
        return entity.isOnGround
    }

    override fun setOnGround(value: Boolean) {
        entity.isOnGround = value
    }

    override fun getRenderHeadYaw(): Float = entity.headYaw

    override fun setRenderHeadYaw(headYaw: Float) {
        entity.headYaw = headYaw
    }

    override fun setRenderBodyYaw(bodyYaw: Float) {
        entity.bodyYaw = bodyYaw
    }
}