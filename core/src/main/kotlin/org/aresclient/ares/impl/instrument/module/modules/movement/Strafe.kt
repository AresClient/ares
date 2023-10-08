package org.aresclient.ares.impl.instrument.module.modules.movement

import net.meshmc.mesh.loader.Mod.Interface
import org.aresclient.ares.api.instrument.module.Category
import org.aresclient.ares.api.instrument.module.Module

object Strafe: Module(Category.MOVEMENT, "Strafe", "") {
    val lowHop = settings.addBoolean("Low Hop", false)!!
    val height = settings.addDouble("Height", 0.3)
        .setMin(0.3)
        .setMax(0.5)
        .setVisibility(lowHop::getValue)!!
    val speedBool = settings.addBoolean("Modify Speed", false)!!
    val speedVal = settings.addDouble("Speed", 0.32)
        .setMin(0.2)
        .setMax(0.6)
        .setVisibility(speedBool::getValue)!!
    val sprintBool = settings.addBoolean("Auto Sprint", true)!!

    @Interface
    private lateinit var vsc: VSC
    interface VSC {
        fun strafe(strafe: Strafe)
    }

    override fun onMotion() {
        vsc.strafe(this)
    }
}