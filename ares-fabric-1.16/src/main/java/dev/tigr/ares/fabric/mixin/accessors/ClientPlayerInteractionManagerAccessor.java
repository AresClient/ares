package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerInteractionManager.class)
public interface ClientPlayerInteractionManagerAccessor {
    @Accessor("currentBreakingPos")
    BlockPos getCurrentBreakingPos();

    @Accessor("blockBreakingCooldown")
    int getBlockBreakingCooldown();

    @Accessor("blockBreakingCooldown")
    void setBlockBreakingCooldown(int blockBreakingCooldown);

    @Accessor("currentBreakingProgress")
    float getCurrentBreakingProgress();

    @Accessor("currentBreakingProgress")
    void setCurrentBreakingProgress(float currentBreakingProgress);

    @Accessor("blockBreakingSoundCooldown")
    float getBlockBreakingSoundCooldown();

    @Accessor("blockBreakingSoundCooldown")
    void setBlockBreakingSoundCooldown(float blockBreakingSoundCooldown);

    @Accessor("breakingBlock")
    void setBreakingBlock(boolean breakingBlock);

    @Invoker("syncSelectedSlot")
    void syncSelectedSlot();

    @Invoker("sendPlayerAction")
    void sendPlayerAction(PlayerActionC2SPacket.Action action, BlockPos pos, Direction direction);

    @Invoker("isCurrentlyBreaking")
    boolean getIsCurrentlyBreaking(BlockPos pos);
}
