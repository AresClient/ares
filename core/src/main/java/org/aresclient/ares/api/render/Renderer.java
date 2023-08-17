package org.aresclient.ares.api.render;

import net.meshmc.mesh.loader.MeshLoader;
import org.aresclient.ares.api.Ares;
import org.aresclient.ares.api.minecraft.render.Camera;
import org.aresclient.ares.api.minecraft.render.Framebuffer;
import org.lwjgl.opengl.GL11;

public class Renderer {
    // TODO: BETTER CHECK FOR LEGACY OPENGL
    private static final boolean LEGACY = MeshLoader.getInstance().getGameVersion().startsWith("1.12");

    public static class Uniforms {
        private final Uniform.F1 roundedRadius = Shader.ROUNDED.uniformF1("radius");
        private final Uniform.F2 roundedSize = Shader.ROUNDED.uniformF2("size");

        private Uniforms() {
        }

        public Uniform.F1 getRoundedRadius() {
            return roundedRadius;
        }

        public Uniform.F2 getRoundedSize() {
            return roundedSize;
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
        private final boolean alpha;
        private final Buffers buffers;
        private final MatrixStack matrixStack;

        private State(Buffers buffers, MatrixStack matrixStack, boolean depth, boolean blend,
                      boolean cull, boolean alpha) {
            this.buffers = buffers;
            this.matrixStack = matrixStack;
            this.depth = depth;
            this.blend = blend;
            this.cull = cull;
            this.alpha = alpha;
        }

        public Buffers getBuffers() {
            return buffers;
        }

        public MatrixStack getMatrixStack() {
            return matrixStack;
        }
    }

    private static State begin(MatrixStack matrixStack) {
        Ares.getMinecraft().getRenderer().getVertexBuffers().unbind();

        State state = new State(
                BUFFERS,
                matrixStack,
                GL11.glIsEnabled(GL11.GL_DEPTH_TEST),
                GL11.glIsEnabled(GL11.GL_BLEND),
                GL11.glIsEnabled(GL11.GL_CULL_FACE),
                LEGACY && GL11.glIsEnabled(GL11.GL_ALPHA_TEST)
        );

        if(LEGACY) {
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();

            GL11.glDisable(GL11.GL_ALPHA_TEST);
        }

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColorMask(true, true, true, true);

        return state;
    }

    public static State begin2d() {
        Resolution resolution = Ares.getMinecraft().getResolution();

        MatrixStack matrixStack = new MatrixStack();
        matrixStack.projection()
            .setOrtho(0F, resolution.getWidth(), resolution.getHeight(), 0F, 0F, 1F);

        return begin(matrixStack);
    }

    public static State begin3d() {
        Camera camera = Ares.getMinecraft().getRenderer().getCamera();

        // TODO: THIS DOES NOT WORK ON LEGACY
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.projection()
            .set(Ares.getMinecraft().getRenderer().getProjectionMatrix())
            .rotate(toRadians(wrapDegrees(camera.getPitch())), 1f, 0f, 0f)
            .rotate(toRadians(wrapDegrees(camera.getYaw() + 180f)), 0f, 1f, 0f)
            .translate((float) -camera.getX(), (float) -camera.getY(), (float) -camera.getZ());

        return begin(matrixStack);
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360f;
        if(wrapped >= 180f) wrapped -= 360f;
        if(wrapped < -180f) wrapped += 360f;
        return wrapped;
    }

    private static float toRadians(float ang) {
        return ang / 180f * 3.1415927f;
    }

    public static void end(State state) {
        for(Buffer buffer: state.buffers.getAll()) {
            if(buffer.shouldRender()) buffer.draw(state.matrixStack);
            buffer.reset();
        }

        glEnableDisable(GL11.GL_DEPTH_TEST, state.depth);
        glEnableDisable(GL11.GL_BLEND, state.blend);
        glEnableDisable(GL11.GL_CULL_FACE, state.cull);

        if(LEGACY) {
            glEnableDisable(GL11.GL_ALPHA_TEST, state.alpha);

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
        }
    }

    private static void glEnableDisable(int code, boolean state) {
        if(state) GL11.glEnable(code);
        else GL11.glDisable(code);
    }

    public static void scissorBegin(float x, float y, float width, float height) {
        Framebuffer framebuffer = Ares.getMinecraft().getFramebuffer();
        Resolution resolution = Ares.getMinecraft().getResolution();
        float scaleWidth = (float) framebuffer.getWidth() / (float) resolution.getScaledWidth();
        float scaleHeight = (float) framebuffer.getHeight() / (float) resolution.getScaledHeight();

        GL11.glScissor(
                (int) (x * scaleWidth),
                framebuffer.getHeight() - (int) ((y + height) * scaleHeight),
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
}
