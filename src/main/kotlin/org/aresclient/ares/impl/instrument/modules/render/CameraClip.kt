package org.aresclient.ares.impl.instrument.modules.render

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import org.aresclient.ares.api.events.BlockOcclusionEvent
import org.aresclient.ares.api.instruments.Module

object CameraClip: Module(Category.RENDER, "CameraClip", "Allows the 3rd person camera to go through walls") {
    private val skipFront = settings.addBoolean("Skip Front", false)
    private val modifyDistance = settings.addBoolean("Modify Distance", false)
    private val distance = settings.addFloat("Distance", 3.5f).setMin(2f).setMax(15f).setVisibility(modifyDistance::getValue)

    /** @see org.aresclient.ares.mixin.mixins.MixinPerspective **/
    fun shouldSkipFront(): Boolean = skipFront.value

    /** @see org.aresclient.ares.mixin.mixins.MixinCamera **/
    fun shouldModifyDistance(): Boolean = modifyDistance.value
    fun getDistance(): Float = distance.value

    @field:EventHandler
    private val blockOcclusionEvent = EventListener<BlockOcclusionEvent> { event ->
        event.isCancelled = true
    }
}
