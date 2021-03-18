package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.*;
import dev.tigr.ares.core.setting.settings.numerical.*;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.render.TextColor;
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
 * Built in Timer for FastMode TPS adapted from Timer Module
 */
@Module.Info(name = "Burrow", description = "Places a block inside your feet", category = Category.COMBAT)
public class Burrow extends Module {

    private final Setting<Double> height = register(new DoubleSetting("Height", 1.12, 1.0, 1.3));
    private final Setting<Boolean> autoSwitch = register(new BooleanSetting("Auto Switch", true));
    private final Setting<Boolean> autoReturn = register(new BooleanSetting("Auto Switch Return", true));
    private final Setting<Boolean> holeOnly = register(new BooleanSetting("Hole Only", true));
    private final Setting<Boolean> toggleSurround = register(new BooleanSetting("Toggle Surround On", false));
    private final Setting<FastMode> fastMode = register(new EnumSetting<>("Fast Mode", FastMode.NONE));
    private final Setting<Integer> timerModeTPS = register(new IntegerSetting("FastMode-TPS", 2500, 1, 30000));
    private final Setting<CurrBlock> blockToUse = register(new EnumSetting<>("Block", CurrBlock.OBSIDIAN));
    private final Setting<HoleBlock> holeBlock = register(new EnumSetting<>("In Hole", HoleBlock.OBSIDIAN));
    private final Setting<BackBlock> backupBlock = register(new EnumSetting<>("Backup", BackBlock.ENDER_CHEST));

    enum FastMode {NONE, TPS}
    enum CurrBlock {OBSIDIAN, ENDER_CHEST, CRYING_OBSIDIAN, NETHERITE_BLOCK, ANCIENT_DEBRIS, ENCHANTING_TABLE, ANVIL}
    enum HoleBlock {OBSIDIAN, ENDER_CHEST, CRYING_OBSIDIAN, NETHERITE_BLOCK, ANCIENT_DEBRIS, ENCHANTING_TABLE, ANVIL}
    enum BackBlock {OBSIDIAN, ENDER_CHEST, CRYING_OBSIDIAN, NETHERITE_BLOCK, ANCIENT_DEBRIS, ENCHANTING_TABLE, ANVIL}

    private BlockPos playerPos;
    private BlockPos airAbove;

    private double jumpHeight;

    float oldYaw;
    float oldPitch;

    //get the Block value of all three options selected
    private Block getCurrBlock(){
        Block current = null;
        if (blockToUse.getValue() == CurrBlock.OBSIDIAN) {current = Blocks.OBSIDIAN;}
        else if (blockToUse.getValue() == CurrBlock.ENDER_CHEST) {current = Blocks.ENDER_CHEST;}
        else if (blockToUse.getValue() == CurrBlock.CRYING_OBSIDIAN) {current = Blocks.CRYING_OBSIDIAN;}
        else if (blockToUse.getValue() == CurrBlock.NETHERITE_BLOCK) {current = Blocks.NETHERITE_BLOCK;}
        else if (blockToUse.getValue() == CurrBlock.ANCIENT_DEBRIS) {current = Blocks.ANCIENT_DEBRIS;}
        else if (blockToUse.getValue() == CurrBlock.ENCHANTING_TABLE) {current = Blocks.ENCHANTING_TABLE;}
        else if (blockToUse.getValue() == CurrBlock.ANVIL) {current = Blocks.ANVIL;}
        return current;
    }
    private Block getHoleBlock(){
        Block hole = null;
        if (holeBlock.getValue() == HoleBlock.OBSIDIAN) {hole = Blocks.OBSIDIAN;}
        else if (holeBlock.getValue() == HoleBlock.ENDER_CHEST) {hole = Blocks.ENDER_CHEST;}
        else if (holeBlock.getValue() == HoleBlock.CRYING_OBSIDIAN) {hole = Blocks.CRYING_OBSIDIAN;}
        else if (holeBlock.getValue() == HoleBlock.NETHERITE_BLOCK) {hole = Blocks.NETHERITE_BLOCK;}
        else if (holeBlock.getValue() == HoleBlock.ANCIENT_DEBRIS) {hole = Blocks.ANCIENT_DEBRIS;}
        else if (holeBlock.getValue() == HoleBlock.ENCHANTING_TABLE) {hole = Blocks.ENCHANTING_TABLE;}
        else if (holeBlock.getValue() == HoleBlock.ANVIL) {hole = Blocks.ANVIL;}
        return hole;
    }
    private Block getBackBlock(){
        Block backup = null;
        if (backupBlock.getValue() == BackBlock.OBSIDIAN) {backup = Blocks.OBSIDIAN;}
        else if (backupBlock.getValue() == BackBlock.ENDER_CHEST) {backup = Blocks.ENDER_CHEST;}
        else if (backupBlock.getValue() == BackBlock.CRYING_OBSIDIAN) {backup = Blocks.CRYING_OBSIDIAN;}
        else if (backupBlock.getValue() == BackBlock.NETHERITE_BLOCK) {backup = Blocks.NETHERITE_BLOCK;}
        else if (backupBlock.getValue() == BackBlock.ANCIENT_DEBRIS) {backup = Blocks.ANCIENT_DEBRIS;}
        else if (backupBlock.getValue() == BackBlock.ENCHANTING_TABLE) {backup = Blocks.ENCHANTING_TABLE;}
        else if (backupBlock.getValue() == BackBlock.ANVIL) {backup = Blocks.ANVIL;}
        return backup;
    }

    @Override
    public void onEnable() {
        playerPos = new BlockPos(MC.player.getX(), MC.player.getY(), MC.player.getZ());
        double airAboveY = playerPos.getY() + 3;
        airAbove = new BlockPos(playerPos.getX(), airAboveY, playerPos.getZ());
        double eTableHeight = height.getValue() - 0.25;

        //determine if player is already burrowed
        if (MC.world.getBlockState(playerPos).getBlock() == getCurrBlock() || MC.world.getBlockState(playerPos).getBlock() == getBackBlock() || MC.world.getBlockState(playerPos).getBlock() == getHoleBlock() || MC.world.getBlockState(playerPos).getBlock() == Blocks.ENCHANTING_TABLE) {
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

        //checks there is enough space above player, and automatically changes height if not so enchanting table can be used if in hotbar.
        if (MC.world.getBlockState(airAbove).getBlock() != Blocks.AIR) {
            if (InventoryUtils.amountBlockInHotbar(Blocks.ENCHANTING_TABLE) > 0) {
                jumpHeight = eTableHeight;
            }
            else if (InventoryUtils.amountBlockInHotbar(Blocks.ENCHANTING_TABLE) <= 0) {
                UTILS.printMessage(TextColor.RED + "Not enough space above!");
                setEnabled(false);
                return;
            }
        }
        else jumpHeight = height.getValue();

        //checks that player has the blocks needed available
        if (InventoryUtils.amountBlockInHotbar(getCurrBlock()) <= 0 && InventoryUtils.amountBlockInHotbar(getBackBlock()) <= 0 || InventoryUtils.amountBlockInHotbar(getHoleBlock()) <= 0 && WorldUtils.isHole(MC.player.getBlockPos()) != HoleType.NONE && InventoryUtils.amountBlockInHotbar(getBackBlock()) <= 0){
            UTILS.printMessage(TextColor.RED + "No Burrow Blocks Found!");
            setEnabled(false);
            return;
        }

        //toggles Surround with snap turned off (snap breaks burrow)
        if(toggleSurround.getValue()) {
            Surround.INSTANCE.setEnabled(true);
            Surround.toggleCenter(false);
        }

        //turns on Timer if Fast Mode is set to TPS
        if (fastMode.getValue() == FastMode.TPS) {
            ReflectionHelper.setPrivateValue(RenderTickCounter.class, ReflectionHelper.getPrivateValue(MinecraftClient.class, MC, "renderTickCounter", "field_1728"), 1000.0F / timerModeTPS.getValue(), "tickTime", "field_1968");
        }

        //jump
        MC.player.jump();
    }
    public void onTick() {
        //run the main sequence
        run();
    }
    public void onDisable() {
        //turns off Timer
        if(fastMode.getValue() == FastMode.TPS) {
            ReflectionHelper.setPrivateValue(RenderTickCounter.class, ReflectionHelper.getPrivateValue(MinecraftClient.class, MC, "renderTickCounter", "field_1728"), 1000.0F / 20.0F, "tickTime", "field_1968");
        }
        MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(oldYaw, oldPitch, MC.player.isOnGround()));
    }
    public void run() {
        if (MC.player == null || MC.world == null) return;

        int oldSelection = -1;
        oldYaw = MC.player.yaw;
        oldPitch = MC.player.pitch;

        if (MC.player.getY() >= playerPos.getY() + jumpHeight) {
            //main block to use
            if (autoSwitch.getValue() && !(InventoryUtils.amountBlockInHotbar(Blocks.ENCHANTING_TABLE) <= 0) && MC.world.getBlockState(airAbove).getBlock() != Blocks.AIR) {
                oldSelection = MC.player.inventory.selectedSlot;
                MC.player.inventory.selectedSlot = InventoryUtils.findBlockInHotbar(Blocks.ENCHANTING_TABLE);
            }

            //block when in hole
            else if (autoSwitch.getValue() && !(InventoryUtils.amountBlockInHotbar(getHoleBlock()) <= 0) && WorldUtils.isHole(MC.player.getBlockPos()) != HoleType.NONE) {
                oldSelection = MC.player.inventory.selectedSlot;
                MC.player.inventory.selectedSlot = InventoryUtils.findBlockInHotbar(getHoleBlock());
            }

            //main block
            else if (autoSwitch.getValue() && !(InventoryUtils.amountBlockInHotbar(getCurrBlock()) <= 0)) {
                oldSelection = MC.player.inventory.selectedSlot;
                MC.player.inventory.selectedSlot = InventoryUtils.findBlockInHotbar(getCurrBlock());
            }

            //backup block to use when either is unavailable
            else if (autoSwitch.getValue() && !(InventoryUtils.amountBlockInHotbar(getBackBlock()) <= 0)) {
                oldSelection = MC.player.inventory.selectedSlot;
                MC.player.inventory.selectedSlot = InventoryUtils.findBlockInHotbar(getBackBlock());
            }

            //place block where the player was before jumping
            WorldUtils.placeBlockMainHand(playerPos);

            //switches back to initial item held if both Auto Switch and Auto Switch Return are true
            if (autoSwitch.getValue() && autoReturn.getValue()) {
                MC.player.inventory.selectedSlot = oldSelection;
            }

            //jumps again to snap player down
            MC.player.jump();

            //disable module
            setEnabled(false);
        }
    }
}