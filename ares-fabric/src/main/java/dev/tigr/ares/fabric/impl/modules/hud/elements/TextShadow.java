package dev.tigr.ares.fabric.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;


/**
 * @author Tigermouthbear 7/23/20
 * updated to 1.16.2 on 8/28/20
 */
@Module.Info(name = "TextShadow", description = "Sets HUD to use text shadow or not", category = Category.HUD)
public class TextShadow extends Module {
    public static TextShadow INSTANCE;

    public TextShadow() {
        INSTANCE = this;
    }
}
