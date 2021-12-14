package dev.tigr.ares;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.util.interfaces.ISelf;
import dev.tigr.ares.core.util.IGUIManager;
import dev.tigr.ares.core.util.IKeyboardManager;
import dev.tigr.ares.core.util.IUtils;
import dev.tigr.ares.core.util.render.IRenderStack;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.core.util.render.ITextureManager;
import dev.tigr.ares.core.util.render.font.AbstractFontRenderer;

/**
 * @author Tigermouthbear 11/23/20
 */
public interface CoreWrapper {
    IUtils UTILS = Ares.UTILS;
    IGUIManager GUI_MANAGER = Ares.GUI_MANAGER;
    IKeyboardManager KEYBOARD_MANAGER = Ares.KEYBOARD_MANAGER;
    IRenderer RENDERER = Ares.RENDERER;
    IRenderStack RENDER_STACK = Ares.RENDER_STACK;
    AbstractFontRenderer FONT_RENDERER = Ares.FONT_RENDERER;
    ITextureManager TEXTURE_MANAGER = Ares.TEXTURE_MANAGER;

    ISelf SELF = Ares.SELF;
}
