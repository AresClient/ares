package dev.tigr.ares.forge.impl.modules.player;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.StringSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.utils.entity.SelfUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import dev.tigr.simpleevents.listener.Priority;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.item.ItemSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.GuiOpenEvent;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoSign", description = "Place a sign automatically with text", category = Category.PLAYER)
public class AutoSign extends Module {
    private static final TextColor COLOR = TextColor.BLUE;

    private final Setting<String> line1 = register(new StringSetting("Line 1", ""));
    private final Setting<String> line2 = register(new StringSetting("Line 2", "Ares Client"));
    private final Setting<String> line3 = register(new StringSetting("Line 3", "on top!"));
    private final Setting<String> line4 = register(new StringSetting("Line 4", ""));
    @EventHandler
    public EventListener<GuiOpenEvent> openGuiEvent = new EventListener<>(Priority.HIGHEST, event -> {
        if(event.getGui() instanceof GuiEditSign) {
            String l1 = line1.getValue().length() > 90 ? line1.getValue().substring(0, 90) : line1.getValue();
            String l2 = line2.getValue().length() > 90 ? line2.getValue().substring(0, 90) : line2.getValue();
            String l3 = line3.getValue().length() > 90 ? line3.getValue().substring(0, 90) : line3.getValue();
            String l4 = line4.getValue().length() > 90 ? line4.getValue().substring(0, 90) : line4.getValue();

            event.setGui(new Gui(
                            ReflectionHelper.getPrivateValue(GuiEditSign.class, (GuiEditSign) event.getGui(), "tileSign", "field_146848_f"),
                            l1, l2, l3, l4
                    )
            );

            setEnabled(false);
        }
    });

    @Override
    public void onEnable() {
        if(!run()) setEnabled(false);
    }

    private boolean run() {
        if(MC.objectMouseOver != null && MC.objectMouseOver.sideHit != null) {
            BlockPos pos = MC.objectMouseOver.getBlockPos().offset(MC.objectMouseOver.sideHit);

            int sign = findSign();
            if(sign == -1) {
                UTILS.printMessage(COLOR + "A sign was not found in your hotbar!");
                return false;
            }

            //Check if place is in range
            Vec3d eyesPos = new Vec3d(MC.player.posX,
                    MC.player.posY + MC.player.getEyeHeight(),
                    MC.player.posZ);

            if(eyesPos.squareDistanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ())) > 16) {
                UTILS.printMessage(COLOR + "Location too far away!");
                return false;
            }

            // place sign
            MC.player.inventory.currentItem = sign;
            SelfUtils.placeBlockMainHand(pos);

            return true;
        }
        return false;
    }

    private int findSign() {
        for(int i = 0; i < 9; i++) {
            if(MC.player.inventory.getStackInSlot(i).getItem() instanceof ItemSign) return i;
        }
        return -1;
    }

    public class Gui extends GuiEditSign implements Wrapper {
        public Gui(TileEntitySign teSign, String line1, String line2, String line3, String line4) {
            super(teSign);
            teSign.signText[0] = new TextComponentString(line1);
            teSign.signText[1] = new TextComponentString(line2);
            teSign.signText[2] = new TextComponentString(line3);
            teSign.signText[3] = new TextComponentString(line4);
        }

        @Override
        public void updateScreen() {
            MC.displayGuiScreen(null);
        }
    }
}
