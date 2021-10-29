package dev.tigr.ares.forge.mixin.accessor;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerControllerMP.class)
public interface PlayerControllerMPAccessor {
    @Accessor("blockHitDelay")
    int getBlockHitDelay();

    @Accessor("blockHitDelay")
    void setBlockHitDelay(int blockHitDelay);

    @Accessor("isHittingBlock")
    void setIsHittingBlock(boolean isHittingBlock);

    @Accessor("curBlockDamageMP")
    float getCurBlockDamageMP();

    @Accessor("curBlockDamageMP")
    void setCurBlockDamageMP(float curBlockDamageMP);

    @Accessor("stepSoundTickCounter")
    float getStepSoundTickCounter();

    @Accessor("stepSoundTickCounter")
    void setStepSoundTickCounter(float stepSoundTickCounter);

    @Accessor("currentBlock")
    BlockPos getCurrentBlock();

    @Invoker("syncCurrentPlayItem")
    void syncCurrentPlayItem();

    @Invoker("isHittingPosition")
    boolean isHittingPosition(BlockPos blockPos);
}
