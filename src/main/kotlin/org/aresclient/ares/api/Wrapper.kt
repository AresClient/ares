package org.aresclient.ares.api

import net.minecraft.client.MinecraftClient
import net.minecraft.util.profiler.Profiler
import net.minecraft.util.profiler.Profilers
import org.aresclient.ares.Main
import org.aresclient.ares.api.events.AresEventManager

interface Wrapper {

	val profiler:Profiler get() = Profilers.get()

	val EVENTS:AresEventManager get() = Main.EVENTS

	val MC:MinecraftClient get() = MinecraftClient.getInstance()

}