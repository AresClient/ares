package org.aresclient.ares.impl

import net.minecraft.entity.Entity

class EntityIterator(val iterator: MutableIterator<Entity>): MutableIterator<org.aresclient.ares.api.Entity> {
    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    override fun next(): org.aresclient.ares.api.Entity {
        return EntityWrapper(iterator.next())
    }

    override fun remove() {
        iterator.remove()
    }
}