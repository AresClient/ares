package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import org.aresclient.ares.api.JWrapper;
import org.aresclient.ares.api.events.LoadChunkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager implements JWrapper {
	@Inject(method = "loadChunkFromPacket", at = @At("RETURN"))
	private void onLoadedChunk(int x, int z, PacketByteBuf buf, Map<Heightmap.Type, long[]> heightmaps, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> cir) {
		EVENTS.post(new LoadChunkEvent(cir.getReturnValue()));
	}
}
