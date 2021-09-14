package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.forge.event.events.render.RenderHeldItemEvent;
import dev.tigr.ares.forge.utils.render.RenderUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.util.EnumHand;

@Module.Info(name = "HandView", description = "Modifies the view of items held in the hands", category = Category.RENDER)
public class HandView extends Module {
    private final Setting<Boolean> separate = register(new BooleanSetting("Separate Hands", false));

    private final Setting<Boolean> translate = register(new BooleanSetting("Translation", true));
    private final Setting<Double> translateMainX = register(new DoubleSetting("T. Main X", 0, -3, 5)).setVisibility(translate::getValue);
    private final Setting<Double> translateMainY = register(new DoubleSetting("T. Main Y", -0.69, -3, 5)).setVisibility(translate::getValue);
    private final Setting<Double> translateMainZ = register(new DoubleSetting("T. Main Z", 1.5, -3, 5)).setVisibility(translate::getValue);
    private final Setting<Double> translateOffX = register(new DoubleSetting("T. Off X", 0, -3, 5)).setVisibility(() -> translate.getValue() && separate.getValue());
    private final Setting<Double> translateOffY = register(new DoubleSetting("T. Off Y", -0.69, -3, 5)).setVisibility(() -> translate.getValue() && separate.getValue());
    private final Setting<Double> translateOffZ = register(new DoubleSetting("T. Off Z", 1.5, -3, 5)).setVisibility(() -> translate.getValue() && separate.getValue());

    private final Setting<Boolean> scale = register(new BooleanSetting("Scale", true));
    private final Setting<Float> scaleMainX = register(new FloatSetting("S. Main X", 1.65f, -3, 5)).setVisibility(scale::getValue);
    private final Setting<Float> scaleMainY = register(new FloatSetting("S. Main Y", 1, -3, 5)).setVisibility(scale::getValue);
    private final Setting<Float> scaleMainZ = register(new FloatSetting("S. Main Z", 3, -3, 5)).setVisibility(scale::getValue);
    private final Setting<Float> scaleOffX = register(new FloatSetting("S. Off X", 1.65f, -3, 5)).setVisibility(() -> scale.getValue() && separate.getValue());
    private final Setting<Float> scaleOffY = register(new FloatSetting("S. Off Y", 1, -3, 5)).setVisibility(() -> scale.getValue() && separate.getValue());
    private final Setting<Float> scaleOffZ = register(new FloatSetting("S. Off Z", 3, -3, 5)).setVisibility(() -> scale.getValue() && separate.getValue());

    private final Setting<Boolean> rotation = register(new BooleanSetting("Rotation", true));
    private final Setting<Integer> rotationMainX = register(new IntegerSetting("R. Main X", 0, -180, 180)).setVisibility(rotation::getValue);
    private final Setting<Integer> rotationMainY = register(new IntegerSetting("R. Main Y", 0, -180, 180)).setVisibility(rotation::getValue);
    private final Setting<Integer> rotationMainZ = register(new IntegerSetting("R. Main Z", 0, -180, 180)).setVisibility(rotation::getValue);
    private final Setting<Integer> rotationOffX = register(new IntegerSetting("R. Off X", 0, -180, 180)).setVisibility(() -> rotation.getValue() && separate.getValue());
    private final Setting<Integer> rotationOffY = register(new IntegerSetting("R. Off Y", 0, -180, 180)).setVisibility(() -> rotation.getValue() && separate.getValue());
    private final Setting<Integer> rotationOffZ = register(new IntegerSetting("R. Off Z", 0, -180, 180)).setVisibility(() -> rotation.getValue() && separate.getValue());

    @EventHandler
    private final EventListener<RenderHeldItemEvent.Invoke> onInvoke = new EventListener<>(event -> event.setCancelled(true));

    @EventHandler
    private final EventListener<RenderHeldItemEvent.Cancelled> onHand = new EventListener<>(event -> {
        if(event.getHand() == EnumHand.MAIN_HAND) {
            if(translate.getValue()) event.translate(translateMainX.getValue(), translateMainY.getValue(), -translateMainZ.getValue());
            if(scale.getValue()) event.scale(scaleMainX.getValue(), scaleMainY.getValue(), scaleMainZ.getValue());
            if(rotation.getValue()) event.multiply(RenderUtils.newQuaternion(rotationMainX.getValue(), rotationMainY.getValue(), rotationMainZ.getValue(), true));
        }
        else {
            if(separate.getValue()) {
                if(translate.getValue()) event.translate(translateOffX.getValue(), translateOffY.getValue(), -translateOffZ.getValue());
                if(scale.getValue()) event.scale(scaleOffX.getValue(), scaleOffY.getValue(), scaleOffZ.getValue());
                if(rotation.getValue()) event.multiply(RenderUtils.newQuaternion(rotationOffX.getValue(), rotationOffY.getValue(), rotationOffZ.getValue(), true));
            }
            else {
                if(translate.getValue()) event.translate(translateMainX.getValue(), translateMainY.getValue(), -translateMainZ.getValue());
                if(scale.getValue()) event.scale(scaleMainX.getValue(), scaleMainY.getValue(), scaleMainZ.getValue());
                if(rotation.getValue()) event.multiply(RenderUtils.newQuaternion(rotationMainX.getValue(), rotationMainY.getValue(), rotationMainZ.getValue(), true));
            }
        }
    });
}
