package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.forge.utils.RenderUtils;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;
import java.util.stream.Collectors;

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
        if(MC.world.playerEntities.size() <= 0 || mode.getValue() != Mode.BOX) return;

        RenderUtils.prepare3d();
        if(others.getValue()) renderOthers();
        if(entities.getValue()) renderEntities();
        if(friends.getValue()) renderFriends();

        RenderUtils.end3d();
    }

    private void renderFriends() {
        dev.tigr.ares.core.util.render.Color color = IRenderer.rainbow();
        for(EntityPlayer player: MC.world.playerEntities) {
            if(FriendManager.isFriend(player.getGameProfile().getName()) && !player.getUniqueID().equals(MC.player.getUniqueID())) {
                RenderGlobal.drawSelectionBoundingBox(player.getEntityBoundingBox(), color.getRed(), color.getBlue(), color.getGreen(), 1);
            }
        }
    }

    private void renderOthers() {
        for(EntityPlayer player: MC.world.playerEntities) {
            if(!FriendManager.isFriend(player.getGameProfile().getName()) && !player.getUniqueID().equals(MC.player.getUniqueID())) {
                RenderGlobal.drawSelectionBoundingBox(player.getEntityBoundingBox(), color.getValue().r, color.getValue().g, color.getValue().b, 1);
            }
        }
    }

    private void renderEntities() {
        for(Entity entity: MC.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityLiving).collect(Collectors.toList())) {
            if(MC.world.playerEntities.contains(entity)) continue;
            RenderGlobal.drawSelectionBoundingBox(entity.getEntityBoundingBox(), color.getValue().r, color.getValue().g, color.getValue().b, 1);
        }
    }

    @Override
    public void onTick() {
        if(mode.getValue() != Mode.OUTLINE) return;

        ShaderGroup outlineShaderGroup = ReflectionHelper.getPrivateValue(RenderGlobal.class, MC.renderGlobal, "entityOutlineShader", "field_174991_A");
        List<Shader> shaders = ReflectionHelper.getPrivateValue(ShaderGroup.class, outlineShaderGroup, "listShaders", "field_148031_d");

        shaders.forEach(shader -> {
            ShaderUniform outlineRadius = shader.getShaderManager().getShaderUniform("Radius");
            if(outlineRadius != null) outlineRadius.set(4 / 5f);
        });

        for(Entity entity: MC.world.loadedEntityList) {
            if(entity instanceof EntityPlayer) {
                if(FriendManager.isFriend(entity.getName())) {
                    entity.setGlowing(friends.getValue());
                } else {
                    entity.setGlowing(others.getValue());
                }
            } else if(entity instanceof EntityLivingBase) {
                entity.setGlowing(entities.getValue());
            }
        }
    }

    @Override
    public void onDisable() {
        for(Entity entity: MC.world.loadedEntityList) {
            entity.setGlowing(false);
        }
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