package org.aresclient.ares.impl

import net.minecraft.entity.Entity

class EntityIterable(private val iterable: MutableIterable<Entity>): MutableIterable<org.aresclient.ares.api.Entity> {
    override fun iterator(): MutableIterator<org.aresclient.ares.api.Entity> {
        return EntityIterator(iterable.iterator())
    }
}