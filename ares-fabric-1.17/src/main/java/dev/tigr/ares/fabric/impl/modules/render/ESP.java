package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.fabric.mixin.accessors.EntityAccessor;
import dev.tigr.ares.fabric.mixin.accessors.ShaderEffectAccessor;
import dev.tigr.ares.fabric.mixin.accessors.WorldRendererAccessor;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.ares.fabric.utils.entity.EntityUtils;
import dev.tigr.ares.fabric.utils.render.RenderUtils;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

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
        if(MC.world.getPlayers().size() <= 0 || mode.getValue() != Mode.BOX) return;

        RenderUtils.prepare3d();

        for(Entity entity: WorldUtils.getTargets(players.getValue(), false, teammates.getValue(), passive.getValue(), hostile.getValue(), nametagged.getValue(), bots.getValue()))
            RenderUtils.cubeLines(entity.getBoundingBox(), new Color(color.getValue().r, color.getValue().g, color.getValue().b, 1f));

        if(friends.getValue()) renderFriends();

        RenderUtils.end3d();
    }

    private void renderFriends() {
        for(PlayerEntity player: MC.world.getPlayers()) {
            if(FriendManager.isFriend(player.getGameProfile().getName()) && !player.getUuid().equals(MC.player.getUuid()))
                RenderUtils.cubeLines(player.getBoundingBox(), IRenderer.rainbow());
        }
    }

    @Override
    public void onTick() {
        if(mode.getValue() != Mode.OUTLINE) return;

        ((ShaderEffectAccessor) ((WorldRendererAccessor) MC.worldRenderer).getEntityOutlineShader()).getPasses().forEach(shader -> {
            GlUniform outlineRadius = shader.getProgram().getUniformByName("Radius");
            if(outlineRadius != null) outlineRadius.set(4 / 5f);
        });

        for(Entity entity: MC.world.getEntities()) {
            boolean flag = EntityUtils.isTarget(entity, players.getValue(), friends.getValue(), teammates.getValue(), passive.getValue(), hostile.getValue(), nametagged.getValue(), bots.getValue());
            entity.setGlowing(flag);
            ((EntityAccessor) entity).setFlag(6, flag);
        }
    }

    @Override
    public void onDisable() {
        for(Entity entity: MC.world.getEntities()) {
            entity.setGlowing(false);
            ((EntityAccessor) entity).setFlag(6, false);
        }
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