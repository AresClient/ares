package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.*;
import dev.tigr.ares.core.setting.settings.numerical.*;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.fabric.impl.modules.combat.Burrow.CurrBlock;
import dev.tigr.ares.fabric.impl.modules.combat.Burrow.RubberbandMode;
import dev.tigr.ares.fabric.impl.modules.movement.NoBlockPush;
import dev.tigr.ares.fabric.mixin.accessors.MinecraftClientAccessor;
import dev.tigr.ares.fabric.mixin.accessors.RenderTickCounterAccessor;
import dev.tigr.ares.fabric.utils.HoleType;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;

/**
 * @author Makrennel
 */
@Module.Info(name = "Burrow", description = "Places a block inside your feet", category = Category.COMBAT)
public class Burrow extends Module {
    private final Setting<Boolean> holeOnly = register(new BooleanSetting("Hole Only", false));
    private final Setting<Boolean> toggleSurround = register(new BooleanSetting("Toggle Surround On", false));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));
    private final Setting<Boolean> preventBlockPush = register(new BooleanSetting("Prevent Block Push", true));

    private final Setting<Boolean> fakeJump = register(new BooleanSetting("FakeJump", true));
    private final Setting<Double> height = register(new DoubleSetting("Trigger Height", 1.12, 1.0, 1.3)).setVisibility(() -> !fakeJump.getValue());
    private final Setting<Boolean> useTimer = register(new BooleanSetting("Use Timer", true)).setVisibility(() -> !fakeJump.getValue());
    private final Setting<Integer> timerTPS = register(new IntegerSetting("TPS", 2500, 1, 12000)).setVisibility(() -> useTimer.getValue() && !fakeJump.getValue());
    private final Setting<RubberbandMode> rubberband = register(new EnumSetting<>("Rubberband", RubberbandMode.Packet)).setVisibility(() -> !fakeJump.getValue());
    private final Setting<Float> fakeClipHeight = register(new FloatSetting("Packet Height", 12, -60, 60)).setVisibility(() -> rubberband.getValue() == RubberbandMode.Packet || fakeJump.getValue());

    private final Setting<CurrBlock> blockToUse = register(new EnumSetting<>("Block", CurrBlock.Obsidian));
    private final Setting<CurrBlock> backupBlock = register(new EnumSetting<>("Backup", CurrBlock.EnderChest));

    enum RubberbandMode { Jump, Packet }
    enum CurrBlock {Obsidian, EnderChest, CryingObsidian, NetheriteBlock, AncientDebris, EnchantingTable, RespawnAnchor, Anvil}

    private BlockPos playerPos;

    float oldYaw;
    float oldPitch;

    int oldSelection = -1;

    //get the Block value of all three options selected
    private Block getCurrBlock(){
        Block index = null;
        if (blockToUse.getValue() == CurrBlock.Obsidian) {index = Blocks.OBSIDIAN;}
        else if (blockToUse.getValue() == CurrBlock.EnderChest) {index = Blocks.ENDER_CHEST;}
        else if (blockToUse.getValue() == CurrBlock.CryingObsidian) {index = Blocks.CRYING_OBSIDIAN;}
        else if (blockToUse.getValue() == CurrBlock.NetheriteBlock) {index = Blocks.NETHERITE_BLOCK;}
        else if (blockToUse.getValue() == CurrBlock.AncientDebris) {index = Blocks.ANCIENT_DEBRIS;}
        else if (blockToUse.getValue() == CurrBlock.RespawnAnchor) {index = Blocks.RESPAWN_ANCHOR;}
        else if (blockToUse.getValue() == CurrBlock.EnchantingTable) {index = Blocks.ENCHANTING_TABLE;}
        else if (blockToUse.getValue() == CurrBlock.Anvil) {index = Blocks.ANVIL;}
        return index;
    }
    private Block getBackBlock(){
        Block index = null;
        if (backupBlock.getValue() == CurrBlock.Obsidian) {index = Blocks.OBSIDIAN;}
        else if (backupBlock.getValue() == CurrBlock.EnderChest) {index = Blocks.ENDER_CHEST;}
        else if (backupBlock.getValue() == CurrBlock.CryingObsidian) {index = Blocks.CRYING_OBSIDIAN;}
        else if (backupBlock.getValue() == CurrBlock.NetheriteBlock) {index = Blocks.NETHERITE_BLOCK;}
        else if (backupBlock.getValue() == CurrBlock.AncientDebris) {index = Blocks.ANCIENT_DEBRIS;}
        else if (backupBlock.getValue() == CurrBlock.RespawnAnchor) {index = Blocks.RESPAWN_ANCHOR;}
        else if (backupBlock.getValue() == CurrBlock.EnchantingTable) {index = Blocks.ENCHANTING_TABLE;}
        else if (backupBlock.getValue() == CurrBlock.Anvil) {index = Blocks.ANVIL;}
        return index;
    }

    private void switchToBlock() {
        oldSelection = MC.player.getInventory().selectedSlot;
        //main block
        int newItem = InventoryUtils.findBlockInHotbar(getCurrBlock());
        //backup block to use when either is unavailable
        if (newItem == -1) newItem = InventoryUtils.findBlockInHotbar(getBackBlock());
        if (newItem != -1) MC.player.getInventory().selectedSlot = newItem;
        else toggle();
    }

    @Override
    public void onEnable() {
        playerPos = new BlockPos(MC.player.getX(), MC.player.getY(), MC.player.getZ());

        //determine if player is already burrowed
        if (MC.world.getBlockState(playerPos).getBlock() == getCurrBlock() || MC.world.getBlockState(playerPos).getBlock() == getBackBlock()) {
            UTILS.printMessage(TextColor.BLUE + "Already Burrowed!");
            setEnabled(false);
            return;
        }

        //toggles off if player is not in hole and set to hole only
        if (WorldUtils.isHole(MC.player.getBlockPos()) == HoleType.NONE && holeOnly.getValue()) {
            UTILS.printMessage(TextColor.RED + "Not in a hole!");
            setEnabled(false);
            return;
        }

        //checks that player has the blocks needed available
        if (InventoryUtils.amountBlockInHotbar(getCurrBlock()) <= 0 && InventoryUtils.amountBlockInHotbar(getBackBlock()) <= 0) {
            UTILS.printMessage(TextColor.RED + "No Burrow Blocks Found!");
            setEnabled(false);
            return;
        }

        //toggles on NoBurrowPush
        if(preventBlockPush.getValue()) {
            NoBlockPush.INSTANCE.setEnabled(true);
        }

        //toggles Surround with snap turned off (snap breaks burrow)
        if(toggleSurround.getValue()) {
            Surround.INSTANCE.setEnabled(true);
            Surround.toggleCenter(false);
        }

        //jump if not Instant mode
        if (!fakeJump.getValue()) {
            MC.player.jump();
        }
    }
    public void onTick() {
        //turns on Timer if Fast Mode is set to Timer
        if (!fakeJump.getValue() && useTimer.getValue()) {
            ((RenderTickCounterAccessor) ((MinecraftClientAccessor) MC).getRenderTickCounter()).setTickTime(1000.0F / timerTPS.getValue());
        }
        //run the main sequence
        run();
    }
    public void onDisable() {
        //turns off Timer
        if(!fakeJump.getValue() && useTimer.getValue()) {
            ((RenderTickCounterAccessor) ((MinecraftClientAccessor) MC).getRenderTickCounter()).setTickTime(1000.0F / 20.0F);
        }
        if (rotate.getValue()) {
            MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(oldYaw, oldPitch, MC.player.isOnGround()));
        }
    }
    public void run() {
        if (MC.player == null || MC.world == null) return;

        oldYaw = MC.player.getYaw();
        oldPitch = MC.player.getPitch();

        switchToBlock();

        if (fakeJump.getValue()) {
            WorldUtils.fakeJump();
            runSequence();
        }

        if (!fakeJump.getValue()) {
            if (MC.player.getY() >= playerPos.getY() + height.getValue()) {
                runSequence();
            }
        }
    }

    private void runSequence(){
        //place block where the player was before jumping
        WorldUtils.placeBlockMainHand(playerPos, rotate.getValue(), true, true);

        MC.player.getInventory().selectedSlot = oldSelection;

        //tries to produce a rubberband
        if (rubberband.getValue() == RubberbandMode.Packet || fakeJump.getValue()) {
            MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + fakeClipHeight.getValue(), MC.player.getZ(), false));
        } else MC.player.jump();

        //disable module
        setEnabled(false);
    }
}