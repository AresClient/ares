package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.MojangApi;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.text.LiteralText;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "MobOwner", description = "Show you owners of mobs", category = Category.RENDER)
public class MobOwner extends Module {
    @Override
    public void onTick() {
        for(Entity entity: MC.world.getEntities()) {
            if(entity instanceof TameableEntity) {
                TameableEntity tameable = (TameableEntity) entity;
                if(tameable.isTamed() && tameable.getOwner() != null) {
                    tameable.setCustomNameVisible(true);
                    tameable.setCustomName(new LiteralText("Owner: " + tameable.getOwner().getDisplayName().asString()));
                }
            }
            if(entity instanceof HorseBaseEntity) {
                HorseBaseEntity horse = (HorseBaseEntity) entity;
                if(horse.isTame() && horse.getOwnerUuid() != null) {
                    horse.setCustomNameVisible(true);
                    horse.setCustomName(new LiteralText("Owner: " + MojangApi.getUsername(horse.getOwnerUuid().toString())));
                    //horse.setCustomNameTag(mojangWebApi.grabRealName("0c716198-b9cb-4ac3-8216-c5b6c68e0c51"));
                }
            }
        }
    }

    @Override
    public void onDisable() {
        for(Entity entity: MC.world.getEntities()) {
            if(entity instanceof TameableEntity || entity instanceof HorseBaseEntity) {
                try {
                    entity.setCustomNameVisible(false);
                } catch(Exception ignored) {
                }
            }
        }
    }
}