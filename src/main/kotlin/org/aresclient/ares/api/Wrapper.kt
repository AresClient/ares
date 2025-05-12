package org.aresclient.ares.api

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.render.Camera
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.profiler.Profiler
import net.minecraft.util.profiler.Profilers
import org.aresclient.ares.Ares
import org.aresclient.ares.api.events.AresEventManager
import org.slf4j.Logger
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

interface Wrapper {

	val profiler: Profiler get() = Profilers.get()

	val EVENTS: AresEventManager get() = Ares.EVENT_MANAGER

	val MC: MinecraftClient get() = MinecraftClient.getInstance()

	val MinecraftClient.NULL: Boolean get() = world == null || player == null

	val SELF: ClientPlayerEntity get() = MC.player!!

	val WORLD: ClientWorld get() = MC.world!!

	val CAMERA: Camera get() = MC.gameRenderer.camera

	val LOGGER: Logger get() = Ares.LOGGER

	val EXECUTOR: ExecutorService get() = Executors.newCachedThreadPool()

}