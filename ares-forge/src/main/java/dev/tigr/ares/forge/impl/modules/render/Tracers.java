package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.forge.utils.RenderUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Tracers", description = "Render lines showing entities in render distance", category = Category.RENDER)
public class Tracers extends Module {
    private final Setting<Boolean> players = register(new BooleanSetting("Players", true));
    private final Setting<Boolean> mobs = register(new BooleanSetting("Mobs", false));

    @Override
    public void onRender3d() {
        MC.world.loadedEntityList.stream().filter(entity -> entity != MC.player && entity instanceof EntityLivingBase && ((players.getValue() && entity instanceof EntityPlayer) || mobs.getValue())).forEach(entity -> {
            if(entity instanceof EntityPlayer && FriendManager.isFriend(((EntityPlayer) entity).getGameProfile().getName()))
                RenderUtils.drawTracer(entity, IRenderer.rainbow());
            else
                RenderUtils.drawTracer(entity, Color.WHITE);
        });
    }
}
