package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.ares.forge.utils.entity.EntityUtils;
import dev.tigr.ares.forge.utils.render.RenderUtils;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ESP", description = "See outlines of players through walls", category = Category.RENDER)
public class ESP extends Module {
    private final Setting<Mode> mode = register(new EnumSetting<>("Mode", Mode.BOX));
    private final Setting<Colors> color = register(new EnumSetting<>("Color", Colors.RED)).setVisibility(() -> mode.getValue() == Mode.BOX);

    private final Setting<Boolean> players = register(new BooleanSetting("Players", true));
    private final Setting<Boolean> friends = register(new BooleanSetting("Friends", true)).setVisibility(players::getValue);
    private final Setting<Boolean> teammates = register(new BooleanSetting("Teammates", true)).setVisibility(players::getValue);
    private final Setting<Boolean> passive = register(new BooleanSetting("Passive", true));
    private final Setting<Boolean> hostile = register(new BooleanSetting("Hostile", true));
    private final Setting<Boolean> nametagged = register(new BooleanSetting("Nametagged", true));
    private final Setting<Boolean> bots = register(new BooleanSetting("Bots", false));

    @Override
    public void onRender3d() {
        if(MC.world.playerEntities.size() <= 0 || mode.getValue() != Mode.BOX) return;

        RenderUtils.prepare3d();

        for(Entity entity: WorldUtils.getTargets(players.getValue(), false, teammates.getValue(), passive.getValue(), hostile.getValue(), nametagged.getValue(), bots.getValue()))
            RenderUtils.cubeLines(entity.getEntityBoundingBox(), new Color(color.getValue().r, color.getValue().g, color.getValue().b, 1f));

        if(friends.getValue()) renderFriends();

        RenderUtils.end3d();
    }

    private void renderFriends() {
        for(EntityPlayer player: MC.world.playerEntities) {
            if(FriendManager.isFriend(player.getGameProfile().getName()) && !player.getUniqueID().equals(MC.player.getUniqueID()))
                RenderUtils.cubeLines(player.getEntityBoundingBox(), IRenderer.rainbow());
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

        for(Entity entity: MC.world.loadedEntityList)
            entity.setGlowing(EntityUtils.isTarget(entity, players.getValue(), friends.getValue(), teammates.getValue(), passive.getValue(), hostile.getValue(), nametagged.getValue(), bots.getValue()));
    }

    @Override
    public void onDisable() {
        for(Entity entity: MC.world.loadedEntityList) entity.setGlowing(false);
    }

    @Override
    public String getInfo() {
        return mode.getValue().name();
    }

    enum Mode {BOX, OUTLINE}

    enum Colors {
        RED(1, 0, 0),
        BLUE(0, 0, 1),
        BLACK(0, 0, 0),
        GREEN(0, 1, 0),
        WHITE(1, 1, 1);

        public float r;
        public float g;
        public float b;

        Colors(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
}