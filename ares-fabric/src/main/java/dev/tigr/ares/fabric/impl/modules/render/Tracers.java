package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.fabric.utils.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Tracers", description = "Render lines showing entities in render distance", category = Category.RENDER)
public class Tracers extends Module {
    private final Setting<Boolean> players = register(new BooleanSetting("Players", true));
    private final Setting<Boolean> mobs = register(new BooleanSetting("Mobs", false));

    @Override
    public void onRender3d() {
        RenderUtils.glBegin();
        for(Entity entity: MC.world.getEntities()) {
            if(entity == MC.player || !(entity instanceof LivingEntity) || !((players.getValue() && entity instanceof PlayerEntity) || mobs.getValue())) continue;

            if(entity instanceof PlayerEntity && FriendManager.isFriend(((PlayerEntity) entity).getGameProfile().getName()))
                RenderUtils.drawTracer(entity, IRenderer.rainbow());
            else
                RenderUtils.drawTracer(entity, Color.WHITE);
        }
        RenderUtils.glEnd();
    }
}
