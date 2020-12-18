package dev.tigr.ares.fabric.utils;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;

/**
 * @author Tigermouthbear 5/3/20
 */
public class Comparators {
    public static final EntityDistance entityDistance = new EntityDistance();
    public static final BlockDistance blockDistance = new BlockDistance();
    public static final ModuleNameLength moduleStrLength = new ModuleNameLength();
    public static final ModuleAlphabetic moduleAlphabetic = new ModuleAlphabetic();

    private static class EntityDistance implements Comparator<Entity>, Wrapper {
        @Override
        public int compare(Entity p1, Entity p2) {
            final double one = Math.sqrt(MC.player.distanceTo(p1));
            final double two = Math.sqrt(MC.player.distanceTo(p2));
            return Double.compare(one, two);
        }
    }

    private static class BlockDistance implements Comparator<BlockPos>, Wrapper {
        @Override
        public int compare(BlockPos pos1, BlockPos pos2) {
            final double one = Math.sqrt(MC.player.squaredDistanceTo(pos1.getX() + 0.5, pos1.getY() + 0.5, pos1.getZ() + 0.5));
            final double two = Math.sqrt(MC.player.squaredDistanceTo(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5));
            return Double.compare(one, two);
        }
    }

	private static class ModuleNameLength implements Comparator<Module>, Wrapper {
        @Override
		public int compare(Module h1, Module h2) {
			final double h1Width = FONT_RENDERER.getStringWidth(h1.getHudName());
			final double h2Width = FONT_RENDERER.getStringWidth(h2.getHudName());
			return Double.compare(h2Width, h1Width);
		}
	}

    private static class ModuleAlphabetic implements Comparator<Module> {
        @Override
        public int compare(Module h1, Module h2) {
            return h1.getName().compareTo(h2.getName());
        }
    }
}
