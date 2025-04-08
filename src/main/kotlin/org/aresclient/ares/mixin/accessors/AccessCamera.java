package org.aresclient.ares.mixin.accessors;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface AccessCamera {
	@Invoker("setPos") void setPos(double x, double y, double z);

	@Invoker("setRotation") void setRotation(float yaw, float pitch);

	@Accessor("ready") void setReady(boolean value);

	@Accessor("area") void setArea(BlockView value);

	@Accessor("focusedEntity") void setFocusedEntity(Entity value);

	@Accessor("thirdPerson") boolean getThirdPerson();
	@Accessor("thirdPerson") void setThirdPerson(boolean value);

	@Accessor("lastTickProgress") void setLastTickProgress(float value);

	@Accessor("cameraY") float getCameraY();

	@Accessor("lastCameraY") float getLastCameraY();

	@Invoker("clipToSpace") float doClipToSpace(float value);
}
