package org.aresclient.ares.impl.instrument.modules.player

import net.minecraft.item.Items
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.mixin.accessors.AccessMinecraftClient

object FastPlace: Module(Category.PLAYER, "FastPlace", "Place blocks or use items faster") {
    enum class Whitelist {
        ALL,
        EXP,
        CRYSTAL,
        EXP_CRYSTAL
    }

    private val whitelist = settings.addEnum("Whitelist", Whitelist.ALL)

    override fun onTick() {
        if(MC.NULL) return

        val item = SELF.getStackInHand(SELF.activeHand).item
        val allowed = when(whitelist.value) {
            Whitelist.EXP -> item == Items.EXPERIENCE_BOTTLE
            Whitelist.CRYSTAL -> item == Items.END_CRYSTAL
            Whitelist.EXP_CRYSTAL -> item == Items.EXPERIENCE_BOTTLE || item == Items.END_CRYSTAL
            Whitelist.ALL -> true
        }

        if(allowed) (MC as AccessMinecraftClient).setItemUseCooldown(0)
    }

    override fun getInfo() = whitelist.value.name
}
