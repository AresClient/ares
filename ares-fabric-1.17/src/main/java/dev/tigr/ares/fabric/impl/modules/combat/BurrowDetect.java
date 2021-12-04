package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.ares.fabric.utils.entity.PlayerUtils;
import dev.tigr.ares.fabric.utils.entity.SelfUtils;
import dev.tigr.ares.fabric.utils.render.RenderUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * @author Hoosiers, 11/25/2020
 */
@Module.Info(name = "BurrowDetect", description = "Sends a message in chat whenever a player is burrowed", category = Category.COMBAT)
public class BurrowDetect extends Module {
    private final Setting<Double> radius = register(new DoubleSetting("Radius", 20, 0, 100));
    private final Setting<Boolean> notification = register(new BooleanSetting("Notification", true));
    private final Setting<Boolean> render = register(new BooleanSetting("Render", true));
    private final Setting<Float> colorRed = register(new FloatSetting("Red", 0.6f, 0, 1)).setVisibility(render::getValue);
    private final Setting<Float> colorGreen = register(new FloatSetting("Green", 0, 0, 1)).setVisibility(render::getValue);
    private final Setting<Float> colorBlue = register(new FloatSetting("Blue", 0.6f, 0, 1)).setVisibility(render::getValue);
    private final Setting<Float> fillAlpha = register(new FloatSetting("Fill Alpha", 0.24f, 0, 1)).setVisibility(render::getValue);
    private final Setting<Float> boxAlpha = register(new FloatSetting("Box Alpha", 1f, 0, 1)).setVisibility(render::getValue);

    private final List<PlayerEntity> burrowedPlayers = new ArrayList<>();

    @Override
    public void onTick() {
        SelfUtils.getPlayersInRadius(radius.getValue()).stream().filter(entityPlayer -> entityPlayer != MC.player).forEach(playerEntity -> {
            if(!burrowedPlayers.contains(playerEntity) && PlayerUtils.isInBurrow(playerEntity)) {
                if(notification.getValue()) UTILS.printMessage(TextColor.BLUE + playerEntity.getEntityName() + TextColor.GREEN + " has burrowed");
                burrowedPlayers.add(playerEntity);
            }
        });

        try {
            for(PlayerEntity playerEntity : burrowedPlayers) {
                if(!PlayerUtils.isInBurrow(playerEntity)) {
                    if(notification.getValue()) UTILS.printMessage(TextColor.BLUE + playerEntity.getEntityName() + TextColor.RED + " is no longer burrowed");
                    burrowedPlayers.remove(playerEntity);
                }
            }
        } catch(ConcurrentModificationException ignore) {}
    }

    @Override
    public void onRender3d() {
        if(!render.getValue() || burrowedPlayers.isEmpty()) return;

        Color fillColor = new Color(colorRed.getValue(), colorGreen.getValue(), colorBlue.getValue(), fillAlpha.getValue());
        Color outlineColor = new Color(colorRed.getValue(), colorGreen.getValue(), colorBlue.getValue(), boxAlpha.getValue());

        RenderUtils.prepare3d();

        for(PlayerEntity player: burrowedPlayers) {
            if(!PlayerUtils.isInBurrow(player)) continue;

            BlockPos pos = new BlockPos(WorldUtils.getMiddlePosition(player.getX()), player.getY(), WorldUtils.getMiddlePosition(player.getZ()));
            BlockPos playerPos = WorldUtils.roundBlockPos(new Vec3d(pos.getX(), player.getY(), pos.getZ()));

            Box box = RenderUtils.getBoundingBox(playerPos);
            RenderUtils.cube(box, fillColor, outlineColor);
        }

        RenderUtils.end3d();
    }
}