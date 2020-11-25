package dev.tigr.ares.forge.impl.commands;

import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.util.render.TextColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityDonkey;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

/**
 * @author Tigermouthbear
 */
public class Vanish extends Command {
    private final EntityDonkey clone = null;
    private Entity dismountedEntity = null;

    public Vanish() {
        super("vanish", "dismount/remount an entity without the server knowing");

        register(literal("v").redirect(register(literal("vanish").then(
                literal("remount").executes(c -> {
                    if(dismountedEntity == null) {
                        UTILS.printMessage(TextColor.RED + "You didnt dismount an entity");
                        return 1;
                    }

                    dismountedEntity.isDead = false;
                    MC.world.spawnEntity(dismountedEntity);
                    MC.player.startRiding(dismountedEntity);

                    UTILS.printMessage(TextColor.GREEN + "Remounted Entity");

                    return 1;
                })).then(
                literal("dismount").executes(c -> {
                    if(MC.player.getRidingEntity() != null) {
                        dismountedEntity = MC.player.getRidingEntity();
                        MC.player.dismountRidingEntity();
                        MC.world.removeEntity(dismountedEntity);
                        UTILS.printMessage(TextColor.GREEN + "Dismounted Entity");
                    } else {
                        UTILS.printMessage(TextColor.RED + "You aren't riding an entity");
                    }

                    return 1;
                }))
        )));
    }
}
