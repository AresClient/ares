package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.Priorities;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.fabric.mixin.accessors.MinecraftClientAccessor;
import dev.tigr.ares.fabric.mixin.accessors.RenderTickCounterAccessor;
import dev.tigr.ares.fabric.utils.HoleType;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.ares.fabric.utils.entity.SelfUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static dev.tigr.ares.fabric.impl.modules.player.RotationManager.ROTATIONS;

/**
 * @author Makrennel
 */
@Module.Info(name = "Burrow", description = "Places a block inside your feet", category = Category.COMBAT)
public class Burrow extends Module {
    private final Setting<Boolean> holeOnly = register(new BooleanSetting("Hole Only", false));
    private final Setting<Boolean> toggleSurround = register(new BooleanSetting("Toggle Surround On", false));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));

    private final Setting<Boolean> fakeJump = register(new BooleanSetting("FakeJump", true));
    private final Setting<Double> height = register(new DoubleSetting("Trigger Height", 1.12, 1.0, 1.3)).setVisibility(() -> !fakeJump.getValue());
    private final Setting<Boolean> useTimer = register(new BooleanSetting("Use Timer", true)).setVisibility(() -> !fakeJump.getValue());
    private final Setting<Integer> timerTPS = register(new IntegerSetting("TPS", 2500, 1, 12000)).setVisibility(() -> useTimer.getValue() && !fakeJump.getValue());
    private final Setting<RubberbandMode> rubberband = register(new EnumSetting<>("Rubberband", RubberbandMode.Packet)).setVisibility(() -> !fakeJump.getValue());
    private final Setting<Float> fakeClipHeight = register(new FloatSetting("Packet Height", 12, -60, 60)).setVisibility(() -> rubberband.getValue() == RubberbandMode.Packet || fakeJump.getValue());

    private final Setting<BlockItem> blockToUse = register(new EnumSetting<>("Block", BlockItem.Obsidian));
    private final Setting<BlockItem> backupBlock = register(new EnumSetting<>("Backup", BlockItem.EnderChest));

    enum RubberbandMode { Jump, Packet }
    enum BlockItem {
        Obsidian(Blocks.OBSIDIAN),
        EnderChest(Blocks.ENDER_CHEST),
        CryingObsidian(Blocks.CRYING_OBSIDIAN),
        NetheriteBlock(Blocks.NETHERITE_BLOCK),
        AncientDebris(Blocks.ANCIENT_DEBRIS),
        EnchantingTable(Blocks.ENCHANTING_TABLE),
        RespawnAnchor(Blocks.RESPAWN_ANCHOR),
        Anvil(Blocks.ANVIL);

        final Block block;

        BlockItem(Block block) {
            this.block = block;
        }
    }

    int key = Priorities.Rotation.BURROW;

    private BlockPos initialBlockPos;
    private Vec3d initialPos;

    int oldSelection = -1;

    private void switchToBlock() {
        oldSelection = MC.player.inventory.selectedSlot;
        //main block
        int newSelection = InventoryUtils.findBlockInHotbar(blockToUse.getValue().block);
        //backup block to use when main is unavailable
        if(newSelection == -1) newSelection = InventoryUtils.findBlockInHotbar(backupBlock.getValue().block);
        if(newSelection != -1) {
            MC.player.inventory.selectedSlot = newSelection;
            MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(newSelection));
        }
        else setEnabled(false);
    }

    @Override
    public void onEnable() {
        initialBlockPos = SelfUtils.getBlockPosCorrected();
        initialPos = SelfUtils.getPlayer().getPos();

        //determine if player is already burrowed
        if(MC.world.getBlockState(initialBlockPos).getBlock() == blockToUse.getValue().block || MC.world.getBlockState(initialBlockPos).getBlock() == backupBlock.getValue().block) {
            UTILS.printMessage(TextColor.BLUE + "Already Burrowed!");
            setEnabled(false);
            return;
        }

        //toggles off if player is not in hole and set to hole only
        if(WorldUtils.isHole(MC.player.getBlockPos()) == HoleType.NONE && holeOnly.getValue()) {
            UTILS.printMessage(TextColor.RED + "Not in a hole!");
            setEnabled(false);
            return;
        }

        //checks that player has the blocks needed available
        if(InventoryUtils.amountBlockInHotbar(blockToUse.getValue().block) <= 0 && InventoryUtils.amountBlockInHotbar(backupBlock.getValue().block) <= 0) {
            UTILS.printMessage(TextColor.RED + "No Burrow Blocks Found!");
            setEnabled(false);
            return;
        }

        //toggles Surround with snap turned off (snap breaks burrow)
        if(toggleSurround.getValue()) {
            Surround.INSTANCE.setEnabled(true);
            Surround.toggleCenter(false);
        }

        //jump if not Instant mode
        if(!fakeJump.getValue()) {
            MC.player.jump();
        }
    }

    public void onTick() {
        //turns on Timer if Fast Mode is set to Timer
        if(!fakeJump.getValue() && useTimer.getValue()) {
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

        ROTATIONS.setCompletedAction(key, true);
    }

    public void run() {
        if(MC.player == null || MC.world == null) return;

        switchToBlock();

        if(fakeJump.getValue()) {
            SelfUtils.fakeJump(0,4);
            runSequence();
        }

        if(!fakeJump.getValue()) {
            if(MC.player.getY() >= initialPos.y + height.getValue()) {
                runSequence();
            }
        }
    }

    private void runSequence(){
        //place block where the player was before jumping
        SelfUtils.placeBlockMainHand(rotate.getValue(), key, key, true, true, initialBlockPos, true, true);

        MC.player.inventory.selectedSlot = oldSelection;
        MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(oldSelection));

        //tries to produce a rubberband
        if(rubberband.getValue() == RubberbandMode.Packet || fakeJump.getValue()) {
            MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(initialPos.x, initialPos.y + fakeClipHeight.getValue(), initialPos.z, false));
        } else MC.player.jump();

        //disable module
        setEnabled(false);
    }
}