package dev.tigr.ares.forge.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.forge.impl.modules.player.AutoSign;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.GuiOpenEvent;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @authors cookiedragon234 Tigermouthbear
 */
@Module.Info(name = "BetterSign", description = "A better sign gui", category = Category.MISC)
public class BetterSign extends Module {
    @EventHandler
    public EventListener<GuiOpenEvent> openGuiEvent = new EventListener<>(event -> {
        if(event.getGui() instanceof GuiEditSign && !(event.getGui() instanceof AutoSign.Gui))
            event.setGui(new BetterSignGui(ReflectionHelper.getPrivateValue(GuiEditSign.class, (GuiEditSign) event.getGui(), "tileSign", "field_146848_f")));
    });
}

class BetterSignGui extends GuiScreen {
    private static int focusedField = 0;
    public final TileEntitySign sign;
    private List<GuiTextField> textFields;
    private String[] defaultStrings;

    public BetterSignGui(TileEntitySign sign) {
        this.sign = sign;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        textFields = new LinkedList<>();
        defaultStrings = new String[4];
        for(int i = 0; i < 4; i++) {
            GuiTextField field = new GuiTextField(i, fontRenderer, width / 2 + 4, 75 + i * 24, 120, 20);
            field.setValidator(this::validateText);
            field.setMaxStringLength(100);

            String text = sign.signText[i].getUnformattedText();
            defaultStrings[i] = text;
            field.setText(text);

            textFields.add(field);

        }
        textFields.get(focusedField).setFocused(true);

        addButton(
                new GuiButton(
                        4,
                        width / 2 + 5,
                        height / 4 + 120,
                        120,
                        20,
                        "Done"
                )
        );
        addButton(
                new GuiButton(
                        5,
                        width / 2 - 125,
                        height / 4 + 120,
                        120,
                        20,
                        "Cancel"
                )
        );

        addButton(
                new GuiButton(
                        6,
                        width / 2 - 41,
                        147,
                        40,
                        20,
                        "Shift"
                )
        );

        addButton(
                new GuiButton(
                        7,
                        width / 2 - 41,
                        123,
                        40,
                        20,
                        "Clear"
                )
        );

        sign.setEditable(false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int currentFocus = focusedField;

        textFields.forEach((field) -> field.mouseClicked(mouseX, mouseY, mouseButton));
        updateFocusField();

        if(focusedField == currentFocus && !textFields.get(focusedField).isFocused())
            textFields.get(focusedField).setFocused(true);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        switch(keyCode) {
            case 1: // Escape
                exit();
                break;
            case 15: // Tab
                int change = isShiftKeyDown() ? -1 : 1;
                tabFocus(change);
                break;
            case 200: // Arrow Up
                tabFocus(-1);
                break;
            case 28:
            case 156:
            case 208: // Enter or Arrow Down
                tabFocus(1);
            default:
                textFields.forEach((field) -> field.textboxKeyTyped(typedChar, keyCode));
                sign.signText[focusedField] = new TextComponentString(textFields.get(focusedField).getText());
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        switch(button.id) {
            case 5: // Cancel
                for(int i = 0; i < 4; i++)
                    sign.signText[i] = new TextComponentString(defaultStrings[i]);
            case 4: // Done
                exit();
                break;
            case 6: // Shift
                String[] replacements = new String[4];
                for(int i = 0; i < 4; i++) {
                    int change = isShiftKeyDown() ? 1 : -1;
                    int target = wrapLine(i + change);
                    replacements[i] = sign.signText[target].getUnformattedText();
                }

                textFields.forEach((field) -> {
                    int id = field.getId();
                    field.setText(replacements[id]);
                    sign.signText[id] = new TextComponentString(replacements[id]);
                });
                break;
            case 7: // Clear
                textFields.forEach((field) -> {
                    int id = field.getId();
                    field.setText("");
                    sign.signText[id] = new TextComponentString("");
                });

                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("sign.edit"), width / 2, 40, 16777215);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (width / 2) - 63, 0.0F, 50.0F);
        float f = 93.75F;
        GlStateManager.scale(-93.75F, -93.75F, -93.75F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        Block block = sign.getBlockType();

        if(block == Blocks.STANDING_SIGN) {
            float f1 = (sign.getBlockMetadata() * 360) / 16.0F;
            GlStateManager.rotate(f1, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -1.0625F, 0.0F);
        } else {
            int i = sign.getBlockMetadata();
            float f2 = 0.0F;

            if(i == 2)
                f2 = 180.0F;

            if(i == 4)
                f2 = 90.0F;

            if(i == 5)
                f2 = -90.0F;

            GlStateManager.rotate(f2, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -0.7625F, 0.0F);
        }

        sign.lineBeingEdited = -1;
        TileEntityRendererDispatcher.instance.render(sign, -0.5D, -0.75D, -0.5D, 0.0F);
        GlStateManager.popMatrix();

        textFields.forEach(GuiTextField::drawTextBox);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    void updateFocusField() {
        textFields.forEach((field) -> {
            if(field.isFocused())
                focusedField = field.getId();
        });
    }

    void exit() {
        sign.markDirty();
        mc.displayGuiScreen(null);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        NetHandlerPlayClient nethandlerplayclient = this.mc.getConnection();

        if(nethandlerplayclient != null)
            nethandlerplayclient.sendPacket(new CPacketUpdateSign(sign.getPos(), sign.signText));

        sign.setEditable(true);
    }

    void tabFocus(int change) {
        textFields.get(focusedField).setFocused(false);
        focusedField = wrapLine(focusedField + change);
        textFields.get(focusedField).setFocused(true);
    }

    int wrapLine(int line) {
        if(line > 3)
            return 0;
        if(line < 0)
            return 3;
        return line;
    }

    boolean validateText(String s) {
        if(fontRenderer.getStringWidth(s) > 90)
            return false;

        char[] arr = s.toCharArray();
        for(char c: arr)
            if(!ChatAllowedCharacters.isAllowedCharacter(c))
                return false;

        return true;
    }

}
