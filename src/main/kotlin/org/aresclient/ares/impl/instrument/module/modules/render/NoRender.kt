package org.aresclient.ares.impl.instrument.module.modules.render

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import org.aresclient.ares.api.events.PacketEvent
import org.aresclient.ares.api.instruments.Module

object NoRender: Module(Category.RENDER, "NoRender", "Prevent certain overlays and particles from rendering") {
    private val fire = settings.addBoolean("NoFire", true)
    private val water = settings.addBoolean("NoWater", true)
    private val wall = settings.addBoolean("NoWall", true)
    private val fog = settings.addBoolean("NoFog", true)
    private val weather = settings.addBoolean("NoWeather", false)
    private val hurtShake = settings.addBoolean("NoHurtShake", false)
    private val fovChange = settings.addBoolean("NoFovChange", false)
    private val explosions = settings.addBoolean("NoExplosions", false)
    private val particles = settings.addBoolean("NoParticles", false)
    private val armorMap = settings.addMap("NoArmorRender")
    private val armor = armorMap.addBoolean("Enabled", false)
    private val head = armorMap.addBoolean("Head", true)
    private val chest = armorMap.addBoolean("Chest", true)
    private val legs = armorMap.addBoolean("Legs", true)
    private val feet = armorMap.addBoolean("Feet", true)

    // see MixinInGameOverlayRenderer
    fun shouldBlockFireOverlay() = isEnabled() && fire.value
    fun shouldBlockWaterOverlay() = isEnabled() && water.value
    fun shouldBlockWallOverlay() = isEnabled() && wall.value

    // see MixinBackgroundRenderer
    fun shouldBlockFog() = isEnabled() && fog.value

    // see MixinWeatherRendering
    fun shouldBlockWeather() = isEnabled() && weather.value

    // see MixinGameRenderer
    fun shouldBlockHurtShake() = isEnabled() && hurtShake.value

    // see MixinAbstractClientPlayerEntity
    fun shouldBlockFovChange() = isEnabled() && fovChange.value

    fun shouldBlockArmorHead() = isEnabled() && armor.value && head.value
    fun shouldBlockArmorChest() = isEnabled() && armor.value && chest.value
    fun shouldBlockArmorLegs() = isEnabled() && armor.value && legs.value
    fun shouldBlockArmorFeet() = isEnabled() && armor.value && feet.value

    @field:EventHandler
    private val packetReceiveListener = EventListener<PacketEvent.Receive> {
        if(it.packet is ExplosionS2CPacket && explosions.value) it.isCancelled = true
        if(it.packet is ParticleS2CPacket && particles.value) it.isCancelled = true
    }
}