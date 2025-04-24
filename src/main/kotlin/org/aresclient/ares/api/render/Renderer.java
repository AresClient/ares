package org.aresclient.ares.api.render;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlBackend;
import net.minecraft.client.render.Camera;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.util.Window;
import org.aresclient.ares.Ares;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

// written by Tigermouthbear years ago
public class Renderer {
    public static class Uniforms {
        private final Uniform.F1 roundedRadius = Shader.ROUNDED.uniformF1("radius");
        private final Uniform.F2 roundedSize = Shader.ROUNDED.uniformF2("size");
        private final Uniform.F1 roundedCutoff = Shader.ROUNDED.uniformF1("cutoff");

        private Uniforms() {
        }

        public Uniform.F1 getRoundedRadius() {
            return roundedRadius;
        }

        public Uniform.F2 getRoundedSize() {
            return roundedSize;
        }

        public Uniform.F1 getRoundedCutoff() {
            return roundedCutoff;
        }
    }

    public static class Buffers {
        private final Buffer triangle;
        private final Buffer triangleTex;
        private final Buffer triangleTexColor;
        private final Buffer ellipse;
        private final Buffer rounded;
        private final Buffer lines;
        private final Uniforms uniforms;

        private Buffers(Uniforms uniforms) {
            triangle = Buffer.createDynamic(Shader.POSITION_COLOR, VertexFormat.POSITION_COLOR);
            triangleTex = Buffer.createDynamic(Shader.POSITION_TEXTURE, VertexFormat.POSITION_UV);
            triangleTexColor = Buffer.createDynamic(Shader.POSITION_TEXTURE_COLOR, VertexFormat.POSITION_UV_COLOR);
            ellipse = Buffer.createDynamic(Shader.ELLIPSE, VertexFormat.POSITION_UV_COLOR);
            rounded = Buffer.createDynamic(Shader.ROUNDED, VertexFormat.POSITION_UV_COLOR)
                    .uniform(uniforms.getRoundedRadius())
                    .uniform(uniforms.getRoundedSize());
            lines = Buffer.createDynamic(Shader.LINES, VertexFormat.LINES).lines();
            this.uniforms = uniforms;
        }

        public void draw() {
            triangle.draw();
            triangleTex.draw();
            triangleTexColor.draw();
            ellipse.draw();
            rounded.draw();
            lines.draw();
        }

        public Buffer getTriangle() {
            return triangle;
        }

        public Buffer getTriangleTex() {
            return triangleTex;
        }

        public Buffer getTriangleTexColor() {
            return triangleTexColor;
        }

        public Buffer getEllipse() {
            return ellipse;
        }

        public Buffer getRounded() {
            return rounded;
        }

        public Buffer getLines() {
            return lines;
        }

        public Buffer[] getAll() {
            return new Buffer[] {
                triangle,
                triangleTex,
                triangleTexColor,
                ellipse,
                rounded,
                lines
            };
        }

        public Uniforms getUniforms() {
            return uniforms;
        }
    }

    private static final Buffers BUFFERS = new Buffers(new Uniforms());

    public static Buffers getBuffers() {
        return BUFFERS;
    }

    public static class State {
        private final boolean depth;
        private final boolean blend;
        private final boolean cull;
        private final Buffers buffers;
        private final MatrixStack matrixStack;

        private State(Buffers buffers, MatrixStack matrixStack, boolean depth, boolean blend, boolean cull) {
            this.buffers = buffers;
            this.matrixStack = matrixStack;
            this.depth = depth;
            this.blend = blend;
            this.cull = cull;
        }

        public void draw() {
            for(Buffer buffer: buffers.getAll()) {
                if(buffer.shouldRender()) {
                    buffer.draw(matrixStack);
                    buffer.reset();
                }
            }
        }

        public Buffers getBuffers() {
            return buffers;
        }

        public MatrixStack getMatrixStack() {
            return matrixStack;
        }
    }

    private static State begin(MatrixStack matrixStack) {
        Framebuffer framebuffer = Ares.getMC().getFramebuffer();
        GpuTexture gpuTexture = framebuffer.getColorAttachment();
        GpuTexture gpuTexture2 = framebuffer.getDepthAttachment();
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER,
                ((GlTexture)gpuTexture).getOrCreateFramebuffer(((GlBackend) RenderSystem.getDevice()).getFramebufferManager(), gpuTexture2));
        GlStateManager._viewport(0, 0, gpuTexture.getWidth(0), gpuTexture.getHeight(0));

        State state = new State(
                BUFFERS,
                matrixStack,
                GL11.glIsEnabled(GL11.GL_DEPTH_TEST),
                GL11.glIsEnabled(GL11.GL_BLEND),
                GL11.glIsEnabled(GL11.GL_CULL_FACE)
        );

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColorMask(true, true, true, true);

        return state;
    }

    public static State begin2d() {
        Window window = Ares.getMC().getWindow();
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.projection()
                .setOrtho(0f, window.getFramebufferWidth(), window.getFramebufferHeight(), 0f, 1000f, 21000f);
        matrixStack.model().translation(0f, 0f, -11000f);
        // idk why minecraft uses these weird values

        return begin(matrixStack);
    }

    public static State begin3d(Matrix4f bobhurt) {
        Camera camera = Ares.getMC().gameRenderer.getCamera();

        MatrixStack matrixStack = new MatrixStack();
        matrixStack.projection()
            .set(RenderSystem.getProjectionMatrix())
            .mul(bobhurt.invert())
            .rotate(toRadians(wrapDegrees(camera.getPitch())), 1f, 0f, 0f)
            .rotate(toRadians(wrapDegrees(camera.getYaw() + 180f)), 0f, 1f, 0f);

        return begin(matrixStack);
    }

    public static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360f;
        if(wrapped >= 180f) wrapped -= 360f;
        if(wrapped < -180f) wrapped += 360f;
        return wrapped;
    }

    public static float toRadians(float ang) {
        return ang / 180f * 3.1415927f;
    }

    public static void end(State state) {
        state.draw();

        glEnableDisable(GL11.GL_DEPTH_TEST, state.depth);
        glEnableDisable(GL11.GL_BLEND, state.blend);
        glEnableDisable(GL11.GL_CULL_FACE, state.cull);

        GlStateManager._bindTexture(0);
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
    }

    private static void glEnableDisable(int code, boolean state) {
        if(state) GL11.glEnable(code);
        else GL11.glDisable(code);
    }

    public static void scissorBegin(float x, float y, float width, float height) {
        Framebuffer framebuffer = Ares.getMC().getFramebuffer();
        Window window = Ares.getMC().getWindow();

        float scaleWidth = (float) framebuffer.viewportWidth / (float) window.getScaledWidth();
        float scaleHeight = (float) framebuffer.viewportHeight / (float) window.getScaledHeight();

        GL11.glScissor(
                (int) (x * scaleWidth),
                framebuffer.viewportHeight - (int) ((y + height) * scaleHeight),
                (int) (width * scaleWidth),
                (int) (height * scaleHeight)
        );
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    public static void scissorEnd() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    // ref is the number of clips that this clip will be inside + 1
    // so clip(ref = 2) would be for clipping inside of a clipped area
    public static void clipBegin(int ref) {
        GL11.glStencilMask(0xFF);
        GL11.glStencilFunc(GL11.GL_ALWAYS, ref, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        if(ref == 1) GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
    }

    public static void clipBegin() {
        clipBegin(1);
    }

    public static void clipMask(int ref) {
        GL11.glStencilMask(0x00);
        GL11.glStencilFunc(GL11.GL_EQUAL, ref, 0xFF);
    }

    public static void clipMask() {
        clipMask(1);
    }

    public static void clipEnd(int ref) {
        if(ref == 1) GL11.glDisable(GL11.GL_STENCIL_TEST);
        else GL11.glStencilFunc(GL11.GL_EQUAL, ref - 1, 0xFF);
    }

    public static void clipEnd() {
        clipEnd(1);
    }

    public static void cleanup() {
        BlurFrameBuffer.clear();
        MSAAFrameBuffer.clear();
        Buffer.clear();
        Shader.clear();
        Texture.clear();
        SkyBox.clear();
    }
}
