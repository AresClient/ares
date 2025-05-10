package org.aresclient.ares.impl.instrument.module.modules.render

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket
import org.aresclient.ares.api.events.PacketEvent
import org.aresclient.ares.api.instruments.Module

object NoRender: Module(Category.RENDER, "No Render", "Prevent certain overlays and particles from rendering") {
    private val fire = settings.addBoolean("Fire", true)
    private val water = settings.addBoolean("Water", true)
    private val wall = settings.addBoolean("Wall", true)
    private val portal = settings.addBoolean("Portal", false)
    private val nausea = settings.addBoolean("Nausea", true)
    private val snow = settings.addBoolean("Powder Snow", true)
    private val darkness = settings.addBoolean("Darkness", true)
    private val blindness = settings.addBoolean("Blindness", true)
    private val fog = settings.addBoolean("Fog", true)
    private val weather = settings.addBoolean("Weather", false)
    private val hurtShake = settings.addBoolean("Hurt Shake", false)
    private val changeFov = settings.addBoolean("Change FOV", false)
    private val explosions = settings.addBoolean("Explosions", false)
    private val particles = settings.addBoolean("Particles", false)
    private val armorMap = settings.addMap("Armor")
    private val armor = armorMap.addBoolean("Enabled", false)
    private val head = armorMap.addBoolean("Head", true)
    private val chest = armorMap.addBoolean("Chest", true)
    private val legs = armorMap.addBoolean("Legs", true)
    private val feet = armorMap.addBoolean("Feet", true)

    /** @see org.aresclient.ares.mixin.mixins.MixinInGameOverlayRenderer */
    fun shouldBlockFireOverlay() = isEnabled() && fire.value
    fun shouldBlockWaterOverlay() = isEnabled() && water.value
    fun shouldBlockWallOverlay() = isEnabled() && wall.value

    /** @see org.aresclient.ares.mixin.mixins.MixinInGameHud */
    /** @see org.aresclient.ares.mixin.mixins.MixinGameRenderer */
    fun shouldBlockPortalOverlay() = isEnabled() && portal.value
    fun shouldBlockNausea() = isEnabled() && nausea.value
    fun shouldBlockPowderSnowOverlay() = isEnabled() && snow.value

    /** @see org.aresclient.ares.mixin.mixins.MixinBackgroundRenderer */
    fun shouldBlockFog() = isEnabled() && fog.value

    /** @see org.aresclient.ares.mixin.mixins.MixinWeatherRendering */
    fun shouldBlockWeather() = isEnabled() && weather.value

    /** @see org.aresclient.ares.mixin.mixins.MixinGameRenderer */
    fun shouldBlockHurtShake() = isEnabled() && hurtShake.value

    /** @see org.aresclient.ares.mixin.mixins.MixinAbstractClientPlayerEntity */
    fun shouldBlockFovChange() = isEnabled() && changeFov.value

    /** @see org.aresclient.ares.mixin.mixins.MixinArmorFeatureRenderer */
    fun shouldBlockArmorHead() = isEnabled() && armor.value && head.value
    fun shouldBlockArmorChest() = isEnabled() && armor.value && chest.value
    fun shouldBlockArmorLegs() = isEnabled() && armor.value && legs.value
    fun shouldBlockArmorFeet() = isEnabled() && armor.value && feet.value

    /** @see org.aresclient.ares.mixin.mixins.MixinWorldRenderer */
    fun shouldBlockParticles() = isEnabled() && particles.value

    @field:EventHandler
    private val packetReceiveListener = EventListener<PacketEvent.Receive> {
        if(it.packet is ExplosionS2CPacket && explosions.value) it.isCancelled = true
        if(it.packet is EntityStatusEffectS2CPacket) {
            if(it.packet.effectId == StatusEffects.DARKNESS && darkness.value) it.isCancelled = true
            if(it.packet.effectId == StatusEffects.BLINDNESS && blindness.value) it.isCancelled = true
        }
    }
}
