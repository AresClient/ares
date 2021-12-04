package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.ares.fabric.utils.render.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Tracers", description = "Render lines showing entities in render distance", category = Category.RENDER)
public class Tracers extends Module {
    private final Setting<Boolean> distance = register(new BooleanSetting("Distance", true));
    private final Setting<Boolean> players = register(new BooleanSetting("Players", true));
    private final Setting<Boolean> friends = register(new BooleanSetting("Friends", true)).setVisibility(players::getValue);
    private final Setting<Boolean> teammates = register(new BooleanSetting("Teammates", true)).setVisibility(players::getValue);
    private final Setting<Boolean> passive = register(new BooleanSetting("Passive", false));
    private final Setting<Boolean> hostile = register(new BooleanSetting("Hostile", false));
    private final Setting<Boolean> nametagged = register(new BooleanSetting("Nametagged", false));
    private final Setting<Boolean> bots = register(new BooleanSetting("Bots", false));

    @Override
    public void onRender3d() {
        RenderUtils.prepare3d();
        for(Entity entity: WorldUtils.getTargets(players.getValue(), friends.getValue(), teammates.getValue(), passive.getValue(), hostile.getValue(), nametagged.getValue(), bots.getValue())) {
            if(entity instanceof PlayerEntity && FriendManager.isFriend(((PlayerEntity) entity).getGameProfile().getName()))
                RenderUtils.drawTracer(entity, IRenderer.rainbow());
            else
                RenderUtils.drawTracer(entity, distance.getValue() ? Color.fromDistance(entity.distanceTo(MC.player)) : Color.WHITE);
        }
        RenderUtils.end3d();
    }
}
