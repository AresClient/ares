package dev.tigr.ares.fabric.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.render.AbstractPanoramaRenderer;
import dev.tigr.ares.core.util.render.LocationIdentifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

/**
 * @author Tigermouthbear 11/28/21
 * implements AbstractPanoramaRenderer for fabric
 */
public class CustomPanoramaRenderer extends AbstractPanoramaRenderer implements Wrapper {
    public static final CustomPanoramaRenderer ARES_DEFAULT_PANORAMA_RENDERER = new CustomPanoramaRenderer(DEFAULT_PANORAMA_PATHS);

    private float timer;

    public CustomPanoramaRenderer(LocationIdentifier[] paths) {
        super(paths);
        for(LocationIdentifier path: paths) {
            TEXTURE_MANAGER.bindTexture(path);
            //((CustomTextureManager) TEXTURE_MANAGER).get(path).setFilter(true, true); // blur
        }
    }

    @Override
    public void draw() {
        this.timer += MC.getTickDelta();
        draw(MC, MathHelper.sin(this.timer * 0.001F) * 5.0F + 25.0F, -this.timer * 0.1F, 1);
    }

    public void draw(MinecraftClient client, float x, float y, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        Matrix4f matrix4f = Matrix4f.viewboxMatrix(85.0D, (float) client.getWindow().getFramebufferWidth() / (float) client.getWindow().getFramebufferHeight(), 0.05F, 10.0F);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(matrix4f);
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.loadIdentity();
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180.0F));
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();

        for(int j = 0; j < 4; ++j) {
            matrixStack.push();
            float f = ((float) (j % 2) / 2.0F - 0.5F) / 256.0F;
            float g = ((float) (j / 2) / 2.0F - 0.5F) / 256.0F;
            matrixStack.translate(f, g, 0.0D);
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(x));
            matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(y));
            RenderSystem.applyModelViewMatrix();

            for(int k = 0; k < 6; ++k) {
                RenderSystem.setShaderTexture(0, ((CustomTextureManager) TEXTURE_MANAGER).get(faces[k]).getGlId());
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                int l = Math.round(255.0F * alpha) / (j + 1);
                if(k == 0) {
                    bufferBuilder.vertex(-1.0D, -1.0D, 1.0D).texture(0.0F, 0.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(-1.0D, 1.0D, 1.0D).texture(0.0F, 1.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(1.0D, 1.0D, 1.0D).texture(1.0F, 1.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(1.0D, -1.0D, 1.0D).texture(1.0F, 0.0F).color(255, 255, 255, l).next();
                }

                if(k == 1) {
                    bufferBuilder.vertex(1.0D, -1.0D, 1.0D).texture(0.0F, 0.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(1.0D, 1.0D, 1.0D).texture(0.0F, 1.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(1.0D, 1.0D, -1.0D).texture(1.0F, 1.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(1.0D, -1.0D, -1.0D).texture(1.0F, 0.0F).color(255, 255, 255, l).next();
                }

                if(k == 2) {
                    bufferBuilder.vertex(1.0D, -1.0D, -1.0D).texture(0.0F, 0.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(1.0D, 1.0D, -1.0D).texture(0.0F, 1.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(-1.0D, 1.0D, -1.0D).texture(1.0F, 1.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(-1.0D, -1.0D, -1.0D).texture(1.0F, 0.0F).color(255, 255, 255, l).next();
                }

                if(k == 3) {
                    bufferBuilder.vertex(-1.0D, -1.0D, -1.0D).texture(0.0F, 0.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(-1.0D, 1.0D, -1.0D).texture(0.0F, 1.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(-1.0D, 1.0D, 1.0D).texture(1.0F, 1.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(-1.0D, -1.0D, 1.0D).texture(1.0F, 0.0F).color(255, 255, 255, l).next();
                }

                if(k == 4) {
                    bufferBuilder.vertex(-1.0D, -1.0D, -1.0D).texture(0.0F, 0.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(-1.0D, -1.0D, 1.0D).texture(0.0F, 1.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(1.0D, -1.0D, 1.0D).texture(1.0F, 1.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(1.0D, -1.0D, -1.0D).texture(1.0F, 0.0F).color(255, 255, 255, l).next();
                }

                if(k == 5) {
                    bufferBuilder.vertex(-1.0D, 1.0D, 1.0D).texture(0.0F, 0.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(-1.0D, 1.0D, -1.0D).texture(0.0F, 1.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(1.0D, 1.0D, -1.0D).texture(1.0F, 1.0F).color(255, 255, 255, l).next();
                    bufferBuilder.vertex(1.0D, 1.0D, 1.0D).texture(1.0F, 0.0F).color(255, 255, 255, l).next();
                }

                tessellator.draw();
            }

            matrixStack.pop();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.colorMask(true, true, true, false);
        }

        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.restoreProjectionMatrix();
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }
}
