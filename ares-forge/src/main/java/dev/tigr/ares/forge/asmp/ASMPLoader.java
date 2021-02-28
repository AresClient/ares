package dev.tigr.ares.forge.asmp;

import dev.tigr.ares.forge.asmp.client.NetworkManagerPatch;
import dev.tigr.asmp.impl.forge.ASMPForgeLoader;
import dev.tigr.asmp.impl.forge.ASMPForgeTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

/**
 * @author Tigermouthbear 2/15/21
 */
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class ASMPLoader extends ASMPForgeLoader {
    public ASMPLoader() {
        super("ares", Transformer.class);
        register(NetworkManagerPatch.class);

        MixinBootstrap.init();
        Mixins.addConfigurations("mixins.ares.json", "mixins.baritone.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
    }

    public static class Transformer extends ASMPForgeTransformer {
        public Transformer() {
            super(ASMPForgeLoader.get("ares"));
        }
    }
}
