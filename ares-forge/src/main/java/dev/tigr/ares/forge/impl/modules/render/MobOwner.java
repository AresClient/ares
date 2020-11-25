package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.MojangApi;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "MobOwner", description = "Show you owners of mobs", category = Category.RENDER)
public class MobOwner extends Module {
    @Override
    public void onTick() {
        for(Entity entity: MC.world.loadedEntityList) {
            if(entity instanceof EntityTameable) {
                EntityTameable tameable = (EntityTameable) entity;
                if(tameable.isTamed() && tameable.getOwner() != null) {
                    tameable.setAlwaysRenderNameTag(true);
                    tameable.setCustomNameTag("Owner: " + tameable.getOwner().getDisplayName().getFormattedText());
                }
            }
            if(entity instanceof AbstractHorse) {
                AbstractHorse horse = (AbstractHorse) entity;
                if(horse.isTame() && horse.getOwnerUniqueId() != null) {
                    horse.setAlwaysRenderNameTag(true);

                    horse.setCustomNameTag("Owner: " + MojangApi.getUsername(horse.getOwnerUniqueId().toString()));
                    //horse.setCustomNameTag(mojangWebApi.grabRealName("0c716198-b9cb-4ac3-8216-c5b6c68e0c51"));
                }
            }
        }
    }

    @Override
    public void onDisable() {
        for(Entity entity: MC.world.loadedEntityList) {
            if(entity instanceof EntityTameable || entity instanceof AbstractHorse) {
                try {
                    entity.setAlwaysRenderNameTag(false);
                } catch(Exception ignored) {
                }
            }
        }
    }
}