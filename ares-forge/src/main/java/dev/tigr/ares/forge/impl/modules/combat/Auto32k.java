package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.Priorities;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.impl.modules.exploit.SecretClose;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.entity.SelfUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Auto32k", description = "Automatically place and dispense a 32k", alwaysListening = true, category = Category.COMBAT)
public class Auto32k extends Module {
    static final private TextColor COLOR = TextColor.BLUE;
    private final Setting<PlaceMode> mode = register(new EnumSetting<PlaceMode>("Mode", PlaceMode.DISPENSER));
    private final Setting<Boolean> rotate = register(new BooleanSetting("Rotate", true));
    private final Setting<Integer> placeDelay = register(new IntegerSetting("Disp.-Delay", 15, 0, 20));
    private final Setting<Boolean> SecrectClose = register(new BooleanSetting("SecretClose", false));
    private BlockPos basePos;
    private int direction;
    private int tickCount = 0;
    private int hopper;
    private int shulker;
    private int solidBlock;
    private int dispenser;
    private int redstone;

    final int key = Priorities.Rotation.AUTO_32K;

    static boolean isSuperWeapon(ItemStack item) {
        if(item == null) {
            return false;
        }

        if(item.getTagCompound() == null) {
            return false;
        }

        if(item.getEnchantmentTagList().getTagType() == 0) {
            return false;
        }

        NBTTagList enchants = (NBTTagList) item.getTagCompound().getTag("ench");

        for(int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);
            if(enchant.getInteger("id") == 16) {
                int lvl = enchant.getInteger("lvl");
                if(lvl >= 16) {
                    return true;
                }
                break;
            }
        }

        return false;

    }

    static int getBlockNotRedstone() {
        for(int i = 0; i < 9; i++) {
            if(MC.player.inventory.getStackInSlot(i) == ItemStack.EMPTY || !(MC.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock) || !Block.getBlockFromItem(MC.player.inventory.getStackInSlot(i).getItem()).getDefaultState().isFullBlock() || Block.getBlockFromItem(MC.player.inventory.getStackInSlot(i).getItem()).equals(Blocks.REDSTONE_BLOCK) || Block.getBlockFromItem(MC.player.inventory.getStackInSlot(i).getItem()).equals(Blocks.DISPENSER)) {
                continue;
            }

            return i;
        }

        return -1;
    }

    @Override
    public void onDisable() {
        ROTATIONS.setCompletedAction(key, true);
    }

    @Override
    public void onEnable() {
        if(MC.objectMouseOver == null || MC.objectMouseOver.sideHit == null) return;

        if(!run()) {
            this.setEnabled(false);
        }
    }

    private boolean run() {
        basePos = null;
        tickCount = 0;

        //java.util.function.Predicate<Entity> predicate = (java.util.function.Predicate<Entity>) entity -> !(entity instanceof EntityItem);

        if(MC.objectMouseOver == null || MC.objectMouseOver.sideHit == null) {
            return false;
        } else {
            basePos = MC.objectMouseOver.getBlockPos().offset(MC.objectMouseOver.sideHit);
        }

        //Check if place is in range
        Vec3d eyesPos = new Vec3d(MC.player.posX,
                MC.player.posY + MC.player.getEyeHeight(),
                MC.player.posZ);

        if(eyesPos.squareDistanceTo(new Vec3d(basePos.getX(), basePos.getY(), basePos.getZ())) > 16) {
            UTILS.printMessage(COLOR + "Location too far away!");
            return false;
        }

        hopper = InventoryUtils.findItemInHotbar(Item.getItemById(154));
        if(hopper == -1) {
            UTILS.printMessage(COLOR + "A hopper was not found in your hotbar!");
            return false;
        }

        for(int i = 219; i <= 234; i++) {
            shulker = InventoryUtils.findItemInHotbar(Item.getItemById(i));
            if(shulker != -1) {
                break;
            }

            if(i == 234) {
                UTILS.printMessage(COLOR + "A shulker was not found in your hotbar!");
                return false;
            }
        }

        if(mode.getValue() == PlaceMode.DISPENSER) {
            //Check for block in hotbar
            solidBlock = getBlockNotRedstone();
            if(solidBlock == -1) {
                UTILS.printMessage(COLOR + "No blocks found in hotbar!");
                return false;
            }

            //Check for dispenser in hotbar
            dispenser = InventoryUtils.findItemInHotbar(Item.getItemById(23));
            if(dispenser == -1) {
                UTILS.printMessage(COLOR + "No dispenser found in hotbar!");
                return false;
            }

            //Check for redstone block in hopper
            redstone = InventoryUtils.findItemInHotbar(Item.getItemById(152));
            if(redstone == -1) {
                UTILS.printMessage(COLOR + "No redstone block found in hotbar!");
                return false;
            }
        }

        //Direction
        direction = MathHelper.floor((double) (MC.player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

        return true;
    }

    @Override
    public void onTick() {
        //Equip 32k
        if(MC.player.openContainer instanceof ContainerHopper) {
            for(int x = 0; x < MC.player.openContainer.inventorySlots.size(); x++) {
                if(isSuperWeapon(MC.player.openContainer.inventorySlots.get(x).getStack()) && !isSuperWeapon(MC.player.inventoryContainer.inventorySlots.get(MC.player.inventory.currentItem).getStack())) {
                    MC.playerController.windowClick(MC.player.openContainer.windowId, x, MC.player.inventory.currentItem, ClickType.SWAP, MC.player);

                    if(SecrectClose.getValue()) MC.player.closeScreen();

                    return;
                }
            }
        }

        if(!getEnabled() || tickCount++ == 0) return;

        if(mode.getValue() == PlaceMode.HOPPER_ONLY) {
            //Place hopper
            MC.player.inventory.currentItem = hopper;
            SelfUtils.placeBlockMainHand(false, -1, rotate.getValue(), key, key, true, true, basePos);

            //Place shulker
            MC.player.inventory.currentItem = shulker;
            SelfUtils.placeBlockMainHand(false, -1, rotate.getValue(), key, key, true, true, new BlockPos(basePos.getX(), basePos.getY() + 1, basePos.getZ()));

            endSequence();
        } else if(mode.getValue() == PlaceMode.DISPENSER) {
            if(tickCount % placeDelay.getValue() != 0) {
                tickCount++;
                return;
            }

            //Place block
            BlockPos block;
            switch(direction) {
                //South +Z
                case 0:
                    block = new BlockPos(basePos.add(0, 0, 1));
                    break;
                //West -X
                case 1:
                    block = new BlockPos(basePos.add(-1, 0, 0));
                    break;
                //North -Z
                case 2:
                    block = new BlockPos(basePos.add(0, 0, -1));
                    break;
                //East +X
                default:
                    block = new BlockPos(basePos.add(1, 0, 0));
            }
            if(MC.world.getBlockState(block).getMaterial().isReplaceable()) {
                MC.player.inventory.currentItem = solidBlock;
                SelfUtils.placeBlockMainHand(false, -1, rotate.getValue(), key, key, true, true, block);
            }
            //End Place block

            //Place dispenser
            switch(direction) {
                //South +Z
                case 0:
                    block = new BlockPos(basePos.add(0, 1, 1));
                    break;
                //West -X
                case 1:
                    block = new BlockPos(basePos.add(-1, 1, 0));
                    break;
                //North -Z
                case 2:
                    block = new BlockPos(basePos.add(0, 1, -1));
                    break;
                //East +X
                default:
                    block = new BlockPos(basePos.add(1, 1, 0));
            }
            if(MC.world.getBlockState(block).getMaterial().isReplaceable()) {
                MC.player.inventory.currentItem = dispenser;
                SelfUtils.placeBlockMainHand(false, -1, rotate.getValue(), key, key, true, true, block);
                MC.player.inventory.currentItem = shulker;
                MC.playerController.processRightClickBlock(MC.player, MC.world, block, EnumFacing.UP, new Vec3d(block.getX(), block.getY(), block.getZ()), EnumHand.MAIN_HAND);
                tickCount++;
                return;
            }

            if(MC.player.openContainer.inventorySlots.get(1).getStack().isEmpty()) {
                MC.playerController.windowClick(MC.player.openContainer.windowId, MC.player.openContainer.inventorySlots.get(1).slotNumber, MC.player.inventory.currentItem, ClickType.SWAP, MC.player);
                MC.player.closeScreen();
            }

            //Place redstone block
            switch(direction) {
                //South +Z
                case 0:
                    block = new BlockPos(basePos.add(0, 2, 1));
                    break;
                //West -X
                case 1:
                    block = new BlockPos(basePos.add(-1, 2, 0));
                    break;
                //North -Z
                case 2:
                    block = new BlockPos(basePos.add(0, 2, -1));
                    break;
                //East +X
                default:
                    block = new BlockPos(basePos.add(1, 2, 0));
            }
            if(MC.world.getBlockState(block).getMaterial().isReplaceable()) {
                MC.player.inventory.currentItem = InventoryUtils.findItemInHotbar(Item.getItemById(152));
                SelfUtils.placeBlockMainHand(false, -1, rotate.getValue(), key, key, true, true, block);
                tickCount++;
                return;
            }
            //End Place redstone block

            //Place hopper
            MC.player.inventory.currentItem = hopper;
            SelfUtils.placeBlockMainHand(false, -1, rotate.getValue(), key, key, true, true, basePos);

            MC.player.inventory.currentItem = shulker;

            endSequence();
            setEnabled(false);
        }
    }

    private void endSequence() {

        if(SecrectClose.getValue()) {
            SecretClose.INSTANCE.setEnabled(false);
            SecretClose.INSTANCE.setEnabled(true);
        }

        //MC.client.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(basePos, EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));
        MC.playerController.processRightClickBlock(MC.player, MC.world, basePos, EnumFacing.UP, new Vec3d(basePos.getX(), basePos.getY(), basePos.getZ()), EnumHand.MAIN_HAND);
    }

    enum PlaceMode {
        HOPPER_ONLY,
        DISPENSER
    }
}
