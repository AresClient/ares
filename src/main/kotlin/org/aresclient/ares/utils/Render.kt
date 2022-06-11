package org.aresclient.ares.utils

import org.aresclient.ares.Ares
import org.aresclient.ares.renderer.MatrixStack
import org.lwjgl.opengl.GL11

// TODO: BETTER CHECK FOR LEGACY OPENGL
private val LEGACY = Ares.MESH.loaderVersion.startsWith("1.12")

public fun render2d(callback: () -> Unit) {
    val state = begin()
    callback()
    state.end()
}

public fun render3d(callback: (MatrixStack) -> Unit) {
    val state = begin()

    val matrixStack = MatrixStack()
    Ares.MESH.renderer.camera.also {
        matrixStack.projection().translate(-it.x.toFloat(), -it.y.toFloat(), -it.z.toFloat())
    }
    callback(matrixStack)

    state.end()
}

private data class State(val depth: Boolean, val blend: Boolean, val cull: Boolean,  val texture: Boolean, val alpha/*LEGACY*/: Boolean)

private fun begin(): State {
    if(LEGACY) {
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()

        GL11.glEnable(GL11.GL_ALPHA_TEST)
    }

    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_DEPTH_TEST)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

    return State(
        GL11.glIsEnabled(GL11.GL_DEPTH_TEST),
        GL11.glIsEnabled(GL11.GL_BLEND),
        GL11.glIsEnabled(GL11.GL_CULL_FACE),
        if(LEGACY) GL11.glIsEnabled(GL11.GL_TEXTURE_2D) else false,
        if(LEGACY) GL11.glIsEnabled(GL11.GL_ALPHA_TEST) else false
    )
}

private fun State.end() {
    depth.set(GL11.GL_DEPTH_TEST)
    blend.set(GL11.GL_BLEND)
    cull.set(GL11.GL_CULL_FACE)

    if(LEGACY) {
        texture.set(GL11.GL_TEXTURE_2D)
        alpha.set(GL11.GL_ALPHA_TEST)

        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPopMatrix()
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glPopMatrix()
    }
}

private fun Boolean.set(type: Int) {
    if(this) GL11.glEnable(type)
    else GL11.glDisable(type)
}
