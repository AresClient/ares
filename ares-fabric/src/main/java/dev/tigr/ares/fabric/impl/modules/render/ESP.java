package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.fabric.utils.RenderUtils;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostProcessShader;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ESP", description = "See outlines of players through walls", category = Category.RENDER)
public class ESP extends Module {
    private final Setting<Mode> mode = register(new EnumSetting<>("Mode", Mode.BOX));
    private final Setting<Boolean> friends = register(new BooleanSetting("Friends", true));
    private final Setting<Boolean> others = register(new BooleanSetting("Players", true));
    private final Setting<Boolean> entities = register(new BooleanSetting("Entities", false));
    private final Setting<Color> color = register(new EnumSetting<>("Color", Color.RED)).setVisibility(() -> mode.getValue() == Mode.BOX);

    @Override
    public void onRender3d() {
        if(MC.world.getPlayers().size() <= 0 || mode.getValue() != Mode.BOX) return;

        RenderUtils.glBegin();

        if(others.getValue()) renderOthers();
        if(entities.getValue()) renderEntities();
        if(friends.getValue()) renderFriends();

        RenderUtils.glEnd();
    }

    private void renderFriends() {
        for(PlayerEntity player: MC.world.getPlayers()) {
            if(FriendManager.isFriend(player.getGameProfile().getName()) && !player.getUuid().equals(MC.player.getUuid()))
                RenderUtils.renderSelectionBoundingBox(player.getBoundingBox(), IRenderer.rainbow());
        }
    }

    private void renderOthers() {
        for(PlayerEntity player: MC.world.getPlayers()) {
            if(!FriendManager.isFriend(player.getGameProfile().getName()) && !player.getUuid().equals(MC.player.getUuid()))
                RenderUtils.renderSelectionBoundingBox(player.getBoundingBox(), color.getValue().r, color.getValue().g, color.getValue().b, 1);
        }
    }

    private void renderEntities() {
        for(Entity entity: MC.world.getEntities()) {
            if(!(entity instanceof LivingEntity) || MC.world.getPlayers().contains(entity)) continue;
            RenderUtils.renderSelectionBoundingBox(entity.getBoundingBox(), color.getValue().r, color.getValue().g, color.getValue().b, 1);
        }
    }

    @Override
    public void onTick() {
        if(mode.getValue() != Mode.OUTLINE) return;

        ShaderEffect outlineShaderGroup = ReflectionHelper.getPrivateValue(WorldRenderer.class, MC.worldRenderer, "entityOutlineShader", "field_4059");
        List<PostProcessShader> shaders = ReflectionHelper.getPrivateValue(ShaderEffect.class, outlineShaderGroup, "passes", "field_1497");

        shaders.forEach(shader -> {
            GlUniform outlineRadius = shader.getProgram().getUniformByName("Radius");
            if(outlineRadius != null) outlineRadius.set(4 / 5f);
        });

        for(Entity entity: MC.world.getEntities()) {
            if(entity instanceof PlayerEntity) {
                if(FriendManager.isFriend(((PlayerEntity) entity).getGameProfile().getName())) {
                    entity.setGlowing(friends.getValue());
                } else {
                    entity.setGlowing(others.getValue());
                }
            } else if(entity instanceof LivingEntity) {
                entity.setGlowing(entities.getValue());
            }
        }
    }

    @Override
    public void onDisable() {
        for(Entity entity: MC.world.getEntities()) entity.setGlowing(false);
    }

    @Override
    public String getInfo() {
        return mode.getValue().name();
    }

    enum Mode {BOX, OUTLINE}

    enum Color {
        RED(1, 0, 0),
        BLUE(0, 0, 1),
        BLACK(0, 0, 0),
        GREEN(0, 1, 0),
        WHITE(1, 1, 1);

        public float r;
        public float g;
        public float b;

        Color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
}