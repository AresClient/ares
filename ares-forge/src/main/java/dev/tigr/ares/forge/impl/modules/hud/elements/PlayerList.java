package dev.tigr.ares.forge.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.forge.impl.modules.hud.HudElement;
import dev.tigr.ares.forge.utils.Comparators;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "PlayerList", description = "Shows a list of all players in render distance", category = Category.HUD)
public class PlayerList extends HudElement {
    private final Setting<Boolean> health = register(new BooleanSetting("Health", true));
    private final Setting<Boolean> distance = register(new BooleanSetting("Distance", true));

    public PlayerList() {
        super(100, 70, 0, 0);
        background.setVisibility(() -> false);
    }

    public void draw() {
        List<EntityPlayer> players = MC.world.playerEntities.stream().filter(player -> !player.getName().equals(MC.player.getName())).collect(Collectors.toList());
        players.sort(Comparators.entityDistance);

        int y = 0;
        int biggestWidth = 0;
        for(EntityPlayer player: players) {
            String name = player.getName();
            double nameWidth = FONT_RENDERER.getStringWidth(name);
            int health = (int) (player.getHealth() + MC.player.getAbsorptionAmount());

            String healthColor = "FFFFFF";
            if(health >= 15) healthColor = "00FF00";
            else if(health > 10) healthColor = "FFF000";
            else if(health < 10) healthColor = "FF0000";

            //-
            drawString("- ", getX(), getY() + y, new Color(Integer.parseInt("FFFFFF", 16)));
            double x = FONT_RENDERER.getStringWidth("-");

            //health
            if(this.health.getValue()) {
                drawString(String.valueOf(health), getX() + x, getY() + y, new Color(Integer.parseInt(healthColor, 16)));
                x += FONT_RENDERER.getStringWidth(health + " ");
            }

            //name
            drawString(name, getX() + x, getY() + y, FriendManager.isFriend(name) ? IRenderer.rainbow() : Color.WHITE);
            x += nameWidth + FONT_RENDERER.getStringWidth(" ");

            //distance
            if(distance.getValue())
                drawString(String.valueOf((int) MC.player.getDistance(player)), getX() + x, getY() + y, new Color(Integer.parseInt("808080", 16)));

            y += FONT_RENDERER.getFontHeight() + 2;
            if(nameWidth > biggestWidth) biggestWidth = (int) nameWidth;
        }

        setWidth(biggestWidth);
        setHeight(y);
    }
}
