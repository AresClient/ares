package dev.tigr.ares.forge.event;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.settings.BindSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.global.UpdateHelper;
import dev.tigr.ares.forge.event.events.render.CrosshairRenderEvent;
import dev.tigr.ares.forge.gui.AresChatGUI;
import dev.tigr.ares.forge.gui.AresUpdateGUI;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

/**
 * @author Tigermouthbear
 */
public class ForgeEvents implements Wrapper {
    private boolean showed = false;

    @SubscribeEvent
    public void renderGameOverlay(RenderGameOverlayEvent event) {
        if(event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            CrosshairRenderEvent crosshairRenderEvent = Ares.EVENT_MANAGER.post(new CrosshairRenderEvent(event.getPartialTicks()));
            if(crosshairRenderEvent.isCancelled()) event.setCanceled(true);
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(Keyboard.getEventKeyState()) {
            int eventKey = Keyboard.getEventKey();

            if(eventKey != 0) {
                String keyName = Keyboard.getKeyName(eventKey);

                if(!keyName.equalsIgnoreCase("NONE")) {
                    for(BindSetting setting: BindSetting.getBinds()) {
                        if(keyName.equalsIgnoreCase(setting.getValue())) setting.invoke();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        // open update menu or main menu
        if(event.getGui() instanceof GuiMainMenu) {
            if(!showed && UpdateHelper.shouldUpdate()) {
                event.setGui(new AresUpdateGUI());
                showed = true;
            }
        }

        // open chat screen if needed
        if(event.getGui() instanceof GuiChat && event.getGui().getClass() == GuiChat.class && !MC.player.isPlayerSleeping())
            event.setGui(new AresChatGUI(ReflectionHelper.getPrivateValue(GuiChat.class, (GuiChat) event.getGui(), "defaultInputFieldText", "field_146409_v")));

        Ares.EVENT_MANAGER.post(event);
    }

    @SubscribeEvent
    public void render3d(RenderWorldLastEvent event) {
        Module.render3d();
    }

    @SubscribeEvent
    public void render2d(RenderGameOverlayEvent.Post event) {
        if(event.getType() == RenderGameOverlayEvent.ElementType.EXPERIENCE || (MC.player.getRidingEntity() instanceof AbstractHorse && event.getType() == RenderGameOverlayEvent.ElementType.HEALTHMOUNT)) {
            try {
                Module.render2d();
            } catch(Throwable ignored) {
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlace(PlayerInteractEvent.RightClickBlock event) {
        Ares.EVENT_MANAGER.post(event);
    }

    @SubscribeEvent
    public void onClientChat(ClientChatEvent event) {
        if(event.getMessage().startsWith(Command.PREFIX.getValue())) Command.execute(event.getMessage());
    }

    @SubscribeEvent
    public void livingUpdateEvent(LivingEvent.LivingUpdateEvent event) {
        if(MC.world == null || MC.player == null || event.getEntityLiving() != MC.player) return;
        Ares.EVENT_MANAGER.post(event);
    }

    @SubscribeEvent
    public void mouseEvent(MouseEvent event) {
        Ares.EVENT_MANAGER.post(event);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        Ares.EVENT_MANAGER.post(event);
    }

    @SubscribeEvent
    public void onRenderLivingPreEvent(RenderLivingEvent.Pre event) {
        Ares.EVENT_MANAGER.post(event);
    }

    @SubscribeEvent
    public void onRenderLivingPostEvent(RenderLivingEvent.Post event) {
        Ares.EVENT_MANAGER.post(event);
    }

    @SubscribeEvent
    public void onLivingDeathEvent(LivingDeathEvent event) {
        Ares.EVENT_MANAGER.post(event);
    }

    @SubscribeEvent
    public void onRenderBlockOverlayEvent(RenderBlockOverlayEvent event) {
        Ares.EVENT_MANAGER.post(event);
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Ares.EVENT_MANAGER.post(event);
    }

    @SubscribeEvent
    public void renderTooltip(RenderTooltipEvent.PostText event) {
        Ares.EVENT_MANAGER.post(event);
    }

    @SubscribeEvent
    public void inputUpdateEvent(InputUpdateEvent event) {
        Ares.EVENT_MANAGER.post(event);
    }
}
