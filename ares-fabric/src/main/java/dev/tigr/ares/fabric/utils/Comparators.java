package dev.tigr.ares.fabric.utils;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.Module;
import net.minecraft.entity.Entity;

import java.util.Comparator;

/**
 * @author Tigermouthbear 5/3/20
 */
public class Comparators {
    public static final EntityDistance entityDistance = new EntityDistance();
    public static final ModuleNameLength moduleStrLength = new ModuleNameLength();
    public static final ModuleAlphabetic moduleAlphabetic = new ModuleAlphabetic();
    public static final StringLength strLength = new StringLength();

    private static class EntityDistance implements Comparator<Entity>, Wrapper {
        public int compare(Entity p1, Entity p2) {
            final int one = (int) Math.sqrt(MC.player.distanceTo(p1));
            final int two = (int) Math.sqrt(MC.player.distanceTo(p2));
            return Integer.compare(one, two);
        }
    }

	private static class ModuleNameLength implements Comparator<Module>, Wrapper {
		public int compare(Module h1, Module h2) {
			final double h1Width = FONT_RENDERER.getStringWidth(h1.getHudName());
			final double h2Width = FONT_RENDERER.getStringWidth(h2.getHudName());
			return Double.compare(h2Width, h1Width);
		}
	}

    private static class ModuleAlphabetic implements Comparator<Module> {
        public int compare(Module h1, Module h2) {
            return h1.getName().compareTo(h2.getName());
        }
    }

    private static class StringLength implements Comparator<String> {
        public int compare(String o1, String o2) {
            return Integer.compare(o1.length(), o2.length());
        }
    }
}
