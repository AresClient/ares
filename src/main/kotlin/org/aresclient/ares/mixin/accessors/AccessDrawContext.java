package org.aresclient.ares.mixin.accessors;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DrawContext.class)
public interface AccessDrawContext {
    @Accessor("vertexConsumers")
    VertexConsumerProvider.Immediate getVertexConsumers();
}
