package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.Priorities;
import dev.tigr.ares.core.util.Timer;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.forge.event.events.client.EntityEvent;
import dev.tigr.ares.forge.event.events.player.DestroyBlockEvent;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.ares.forge.utils.Comparators;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.MathUtils;
import dev.tigr.ares.forge.utils.entity.PlayerUtils;
import dev.tigr.ares.forge.utils.render.RenderUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import dev.tigr.simpleevents.listener.Priority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static dev.tigr.ares.forge.impl.modules.player.RotationManager.ROTATIONS;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "CrystalAura", description = "Automatically breaks and places crystals", category = Category.COMBAT)
public class CrystalAura extends Module {
    public static CrystalAura INSTANCE;

    //Setting which enumerates the pages
    private final Setting<Page> page = register(new EnumSetting<>("Page", Page.GENERAL));
    enum Page { GENERAL, RENDER, BREAK, PLACE }

    //General Page
    private final Setting<Boolean> doSwitch = register(new BooleanSetting("Do Switch", true)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Boolean> noGappleSwitch = register(new BooleanSetting("No Gapple Switch", false)).setVisibility(() -> page.getValue() == Page.GENERAL && doSwitch.getValue());
    private final Setting<Boolean> preventSuicide = register(new BooleanSetting("Prevent Suicide", true)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<MathUtils.DmgCalcMode> calcMode = register(new EnumSetting<>("Dmg Calc Mode", MathUtils.DmgCalcMode.DAMAGE)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Target> targetSetting = register(new EnumSetting<>("Target", Target.CLOSEST)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Rotations> rotateMode = register(new EnumSetting<>("Rotations", Rotations.PACKET)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Order> order = register(new EnumSetting<>("Order", Order.PLACE_BREAK)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Boolean> pauseOnEat = register(new BooleanSetting("Pause On Eat", true)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Boolean> pauseOnPot = register(new BooleanSetting("Pause On Pot", true)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Boolean> pauseOnXP = register(new BooleanSetting("Pause On XP", false)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Boolean> pauseOnMine = register(new BooleanSetting("Pause On Mine", false)).setVisibility(() -> page.getValue() == Page.GENERAL);

    //Place Page
    private final Setting<Double> placeRange = register(new DoubleSetting("Place Range", 5, 0, 10)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Integer> placeDelay = register(new IntegerSetting("Place Delay", 2, 0, 20)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Integer> placeOffhandDelay = register(new IntegerSetting("Offh. Place Delay", 2, 0, 20)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Float> minDamage = register(new FloatSetting("Minimum Damage", 7.5f, 0, 15)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Boolean> oneDotThirteen = register(new BooleanSetting("1.13+", true)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Boolean> antiSurround = register(new BooleanSetting("Anti-Surround", true)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Boolean> predictMovement = register(new BooleanSetting("Predict Movement", true)).setVisibility(() -> page.getValue() == Page.PLACE);

    //Break Page
    private final Setting<Double> breakRange = register(new DoubleSetting("Break Range", 5, 0, 10)).setVisibility(() -> page.getValue() == Page.BREAK);
    private final Setting<Integer> breakDelay = register(new IntegerSetting("Break Delay", 2, 0, 20)).setVisibility(() -> page.getValue() == Page.BREAK);
    private final Setting<Integer> breakOffhandDelay = register(new IntegerSetting("Offh. Break Delay", 2, 0, 20)).setVisibility(() -> page.getValue() == Page.BREAK);
    private final Setting<Integer> breakAge = register(new IntegerSetting("Break Age", 0, 0, 20)).setVisibility(() -> page.getValue() == Page.BREAK);
    private final Setting<Boolean> breakOnSpawn = register(new BooleanSetting("Break On Spawn", true)).setVisibility(() -> page.getValue() == Page.BREAK && !(breakAge.getValue() > 0));
    private final Setting<BreakMode> breakMode = register(new EnumSetting<>("Break Mode", BreakMode.SMART)).setVisibility(() -> page.getValue() == Page.BREAK);
    private final Setting<Integer> maxBreakTries = register(new IntegerSetting("Break Attempts", 3, 1, 5)).setVisibility(() -> page.getValue() == Page.BREAK);
    private final Setting<Integer> lostWindow = register(new IntegerSetting("Fail Window", 6, 0, 20)).setVisibility(() -> page.getValue() == Page.BREAK);
    private final Setting<Boolean> retryLost = register(new BooleanSetting("Retry Failed Crystals", true)).setVisibility(() -> page.getValue() == Page.BREAK && breakMode.getValue() != BreakMode.ALL);
    private final Setting<Integer> retryAfter = register(new IntegerSetting("Retry After", 4, 0, 20)).setVisibility(() -> page.getValue() == Page.BREAK && breakMode.getValue() != BreakMode.ALL && retryLost.getValue());
    private final Setting<Boolean> sync = register(new BooleanSetting("Sync", true)).setVisibility(() -> page.getValue() == Page.BREAK);

    //Render Page
    private final Setting<Float> colorRed = register(new FloatSetting("Red", 0.69f, 0, 1)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> colorGreen = register(new FloatSetting("Green", 0, 0, 1)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> colorBlue = register(new FloatSetting("Blue", 0, 0, 1)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> fillAlpha = register(new FloatSetting("Fill Alpha", 0.24f, 0, 1)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> boxAlpha = register(new FloatSetting("Box Alpha", 1f, 0, 1)).setVisibility(() -> page.getValue() == Page.RENDER);

    enum BreakMode { OWN, SMART, ALL }
    enum Order { PLACE_BREAK, BREAK_PLACE }
    enum Target { CLOSEST, MOST_DAMAGE }
    enum Rotations { PACKET, REAL, NONE }

    private final Timer renderTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private final Timer cleanupTimer = new Timer();
    private final Timer rotationTimer = new Timer();
    private BlockPos rotatePos = null;
    private double[] rotations = null;
    public BlockPos target = null;
    private final LinkedHashMap<Vec3d, Long> placedCrystals = new LinkedHashMap<>();
    private final LinkedHashMap<EntityEnderCrystal, AtomicInteger> spawnedCrystals = new LinkedHashMap<>();
    private final LinkedHashMap<EntityEnderCrystal, Integer> lostCrystals = new LinkedHashMap<>();
    private EntityPlayer targetPlayer;

    final int key = Priorities.Rotation.CRYSTAL_AURA;
    final int generalPriority = Priorities.Rotation.CRYSTAL_AURA;
    final int yawstepPriority = Priorities.Rotation.YAW_STEP;

    public CrystalAura() {
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        if (targetPlayer != null
                && !targetPlayer.isDead
                && !(targetPlayer.getHealth() <= 0)
                && !(MC.player.getDistance(targetPlayer) > Math.max(placeRange.getValue(), breakRange.getValue()) + 8)) {
            return targetPlayer.getGameProfile().getName();
        }
        else return "null";
    }

    @Override
    public void onDisable() {
        ROTATIONS.setCompletedAction(key, true);
    }

    @Override
    public void onTick() {
        run();
    }

    private void run() {
        //Ensure it doesn't spam illegal place and break interactions without being rotated
        if(rotateMode.getValue() == Rotations.PACKET) {
            if(!ROTATIONS.isKeyCurrent(key) && !ROTATIONS.isCompletedAction() && ROTATIONS.getCurrentPriority() > key) return;
        }

        // Break modes on a separate thread, otherwise the smart break damage calculations hold up the onTick function for all modules.
        EXECUTOR.execute(() -> {
            for(Entity entity : MC.world.getLoadedEntityList()) {
                if(entity.getDistance(MC.player) > Math.max(placeRange.getValue(), breakRange.getValue()) +2) continue;
                if(entity instanceof EntityEnderCrystal) {
                    EntityEnderCrystal c = (EntityEnderCrystal) entity;
                    if(breakMode.getValue() == BreakMode.SMART) {
                        // Check if the player wants to retry breaking lost crystals
                        if(!retryLost.getValue() && lostCrystals.containsKey(c)) continue;
                        if(lostCrystals.containsKey(c)) {
                            if(c.ticksExisted < lostCrystals.get(c) + lostWindow.getValue() + retryAfter.getValue()) continue;
                        }
                        if(MathUtils.getDamage(entity.getPositionVector(), targetPlayer, predictMovement.getValue()) >= (minDamage.getValue() / 2) && !spawnedCrystals.containsKey(c)) {
                            //add crystal to spawned list so that it can be broken.
                            spawnedCrystals.put(c, new AtomicInteger(0));
                            lostCrystals.remove(c);
                            continue;
                        }
                    }
                    if(breakMode.getValue() == BreakMode.ALL) {
                        if(lostCrystals.containsKey(c)) {
                            if(c.ticksExisted < lostCrystals.get(c) + lostWindow.getValue()) continue;
                        }
                        if(!spawnedCrystals.containsKey(c)) {
                            spawnedCrystals.put(c, new AtomicInteger(0));
                            lostCrystals.remove(c);
                            continue;
                        }
                    }
                    if(breakMode.getValue() == BreakMode.OWN && retryLost.getValue()) {
                        if(!lostCrystals.containsKey(c)) continue;
                        if(lostCrystals.containsKey(c)) {
                            if(c.ticksExisted < lostCrystals.get(c) + lostWindow.getValue() + retryAfter.getValue()) continue;
                        }
                        spawnedCrystals.put(c, new AtomicInteger(0));
                        lostCrystals.remove(c);
                        continue;
                    }
                }
            }
        });

        // pause with options
        if((pauseOnEat.getValue() && MC.player.isHandActive() && (MC.player.getHeldItemMainhand().getItem() instanceof ItemFood || MC.player.getHeldItemOffhand().getItem() instanceof ItemFood)) ||
                (pauseOnPot.getValue() && MC.player.isHandActive() && (MC.player.getHeldItemMainhand().getItem() instanceof ItemPotion || MC.player.getHeldItemOffhand().getItem() instanceof ItemPotion)) ||
                (pauseOnXP.getValue() && MC.player.isHandActive() && (MC.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE || MC.player.getHeldItemOffhand().getItem() == Items.EXPERIENCE_BOTTLE)) ||
                (pauseOnMine.getValue() && MC.playerController.getIsHittingBlock()))
            return;

        // rotations
        if(rotations != null && rotationTimer.passedTicks(10)) {
            rotatePos = null;
            rotations = null;
            ROTATIONS.setCompletedAction(key, true);
            rotationTimer.reset();
        } else if(rotatePos != null) {
            rotations = PlayerUtils.calculateLookFromPlayer(rotatePos.getX() + 0.5, rotatePos.getY() + 0.5, rotatePos.getZ() + 0.5, MC.player);
            if(rotateMode.getValue() == Rotations.PACKET) ROTATIONS.setCurrentRotation((float) rotations[0], (float) rotations[1], key, generalPriority, false, false);
        }

        // cleanup render
        if(cleanupTimer.passedMillis(3000)) {
            target = null;
            renderTimer.reset();
        }

        // do logic
        boolean offhand = MC.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
        if(order.getValue() == Order.PLACE_BREAK) {
            place(offhand);
            explode(offhand);
        } else {
            explode(offhand);
            place(offhand);
        }

        // cleanup place map and lost crystals every ten seconds
        if(cleanupTimer.passedSec(10)) {
            for(Map.Entry<EntityEnderCrystal, Integer> entry: lostCrystals.entrySet()) {
                if(MC.world.getEntityByID(entry.getKey().getEntityId()) == null)
                    lostCrystals.remove(entry.getKey());
            }

            // cleanup crystals that never spawned
            Optional<Map.Entry<Vec3d, Long>> first = placedCrystals.entrySet().stream().findFirst();
            if(first.isPresent()) {
                Map.Entry<Vec3d, Long> entry = first.get();
                if((System.nanoTime() / 1000000) - entry.getValue() >= 10000) placedCrystals.remove(entry.getKey());
            }
            cleanupTimer.reset();
        }
    }

    private void place(boolean offhand) {
        if(placeTimer.passedTicks(offhand ? placeOffhandDelay.getValue() : placeDelay.getValue())) {
            // if no gapple switch and player is holding apple
            if(!offhand && noGappleSwitch.getValue() && MC.player.inventory.getCurrentItem().getItem() instanceof ItemAppleGold) {
                if(target != null) target = null;
                return;
            }

            // find best crystal spot
            BlockPos target = getBestPlacement();
            if(target == null) return;

            placeCrystal(offhand, target);
            placeTimer.reset();
        }
    }

    private void placeCrystal(boolean offhand, BlockPos pos) {
        // switch to crystals if not holding
        if(!offhand && MC.player.inventory.getCurrentItem().getItem() != Items.END_CRYSTAL) {
            if(doSwitch.getValue()) {
                int slot = InventoryUtils.findItemInHotbar(Items.END_CRYSTAL);
                if (slot != -1) {
                    MC.player.inventory.currentItem = slot;
                    MC.player.connection.sendPacket(new CPacketHeldItemChange());
                }
            } else return;
        }

        // place
        MC.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.UP, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));
        rotationTimer.reset();
        rotatePos = pos;

        // add to place map
        placedCrystals.put(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), System.nanoTime() / 1000000);

        // set render pos
        target = pos;
    }

    private void explode(boolean offhand) {
        if(!shouldBreakCrystal(offhand)) return;

        for(Map.Entry<EntityEnderCrystal, AtomicInteger> entry: spawnedCrystals.entrySet()) {
            // check if crystal can be broken
            if(!canBreakCrystal(entry.getKey())) continue;

            breakCrystal(entry.getKey(), offhand);

            // remove if it hits limit of tries
            if(entry.getValue().get() + 1 == maxBreakTries.getValue()) {
                lostCrystals.put(entry.getKey(), entry.getKey().ticksExisted);
                spawnedCrystals.remove(entry.getKey());
            }
            else entry.getValue().set(entry.getValue().get() + 1);
        }
    }

    @EventHandler
    public EventListener<EntityEvent.Spawn> spawnEntityEvent = new EventListener<>(Priority.HIGHEST, event -> {
        if(event.getEntity() instanceof EntityEnderCrystal) {
            EntityEnderCrystal crystal = (EntityEnderCrystal) event.getEntity();

            // loop through all placed crystals to see if it matches
            for(Map.Entry<Vec3d, Long> entry: new ArrayList<>(placedCrystals.entrySet())) {
                if(entry.getKey().equals(crystal.getPositionVector())) {
                    // break crystal if possible and add to spawned crystals map
                    boolean offhand = shouldOffhand();
                    if(shouldBreakCrystal(offhand) && canBreakCrystal(crystal) && breakOnSpawn.getValue()) {
                        MC.addScheduledTask(() -> breakCrystal(crystal, offhand));
                        spawnedCrystals.put(crystal, new AtomicInteger(1));
                    } else spawnedCrystals.put(crystal, new AtomicInteger(0));

                    // remove from placed list
                    placedCrystals.remove(entry.getKey());
                }
            }
        }
    });

    @EventHandler
    public EventListener<EntityEvent.Remove> removeEntityEvent = new EventListener<>(Priority.HIGHEST, event -> {
        // remove spawned crystals from map when they are removed
        if(event.getEntity() instanceof EntityEnderCrystal) {
            EntityEnderCrystal crystal = (EntityEnderCrystal) event.getEntity();
            BlockPos pos = event.getEntity().getPosition().down();
            if(canCrystalBePlacedHere(pos) && pos.equals(getBestPlacement()) && spawnedCrystals.containsKey(crystal)) placeCrystal(shouldOffhand(), pos);

            spawnedCrystals.remove(crystal);
        }
    });

    @EventHandler
    public EventListener<DestroyBlockEvent> destroyBlockEvent = new EventListener<>(Priority.HIGHEST, event -> {
        // place crystal at broken block place
        if(antiSurround.getValue()) {
            BlockPos pos = event.getPos().down();
            if(isPartOfHole(pos) && canCrystalBePlacedHere(pos)) placeCrystal(shouldOffhand(), pos);
        }
    });

    // draw target
    @Override
    public void onRender3d() {
        if(target != null) {
            Color fillColor = new Color(colorRed.getValue(), colorGreen.getValue(), colorBlue.getValue(), fillAlpha.getValue());
            Color outlineColor = new Color(colorRed.getValue(), colorGreen.getValue(), colorBlue.getValue(), boxAlpha.getValue());

            AxisAlignedBB bb = RenderUtils.getBoundingBox(target);
            if(bb != null) {
                RenderUtils.prepare3d();
                RenderUtils.cube(bb, fillColor, outlineColor);
                RenderUtils.end3d();
            }
        }
    }

    @EventHandler
    public EventListener<LivingEvent.LivingUpdateEvent> livingUpdateEvent = new EventListener<>(event -> {
        // rotate for actual mode
        if(rotations != null && rotateMode.getValue() == Rotations.REAL) {
            MC.player.rotationPitch = (float) rotations[1];
            MC.player.rotationYaw = (float) rotations[0];
        }
    });

    //Remove Crystals from lists on Sound packet received
    @EventHandler
    private EventListener<PacketEvent.Receive> packetReceiveListener = new EventListener<>(event -> {
        if (event.getPacket() instanceof SPacketSoundEffect) {
            final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
            if(packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                //Get all End Crystals within box
                List<EntityEnderCrystal> crystalList =
                        new ArrayList<>(MC.world.getEntitiesWithinAABB(
                                EntityEnderCrystal.class,
                                new AxisAlignedBB(
                                        new BlockPos(packet.getX(), packet.getY(), packet.getZ())
                                ).grow(6,6,6)
                        ));
                for(EntityEnderCrystal c: crystalList) {
                    if(c.getDistanceSq(packet.getX(), packet.getY(), packet.getZ()) <= 36) {
                        //Remove from all these lists because we can be sure it has broken if the packet was received
                        spawnedCrystals.remove(c);
                        lostCrystals.remove(c);
                    }
                }
            }
        }
    });

    private boolean isPartOfHole(BlockPos pos) {
        List<Entity> entities = new ArrayList<>();
        entities.addAll(MC.world.getEntitiesWithinAABBExcludingEntity(MC.player, new AxisAlignedBB(pos.add(1, 0, 0))));
        entities.addAll(MC.world.getEntitiesWithinAABBExcludingEntity(MC.player, new AxisAlignedBB(pos.add(-1, 0, 0))));
        entities.addAll(MC.world.getEntitiesWithinAABBExcludingEntity(MC.player, new AxisAlignedBB(pos.add(0, 0, 1))));
        entities.addAll(MC.world.getEntitiesWithinAABBExcludingEntity(MC.player, new AxisAlignedBB(pos.add(0, 0, -1))));
        return entities.stream().anyMatch(entity -> entity instanceof EntityPlayer);
    }

    private boolean shouldOffhand() {
        return MC.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
    }

    private boolean shouldBreakCrystal(boolean offhand) {
        return breakTimer.passedTicks(offhand ? breakOffhandDelay.getValue() : breakDelay.getValue());
    }

    private boolean canBreakCrystal(EntityEnderCrystal crystal) {
        return MC.player.getDistance(crystal) <= breakRange.getValue() // check range
                && !(MC.player.getHealth() - MathUtils.getDamage(crystal.getPositionVector(), MC.player, false) <= 1 && preventSuicide.getValue()) // check suicide
                && crystal.ticksExisted >= breakAge.getValue(); // check that the crystal has been in the world for the minimum age specified
    }

    private void breakCrystal(EntityEnderCrystal crystal, boolean offhand) {
        // find hand
        EnumHand hand = offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;

        // break
        if(sync.getValue()) MC.player.connection.sendPacket(new CPacketUseEntity(crystal, hand));
        MC.playerController.attackEntity(MC.player, crystal);
        MC.player.swingArm(hand);

        //spoof rotations
        rotationTimer.reset();
        rotatePos = crystal.getPosition();

        // reset timer
        breakTimer.reset();
    }

    private BlockPos getBestPlacement() {
        double bestScore = 69420;
        BlockPos target = null;
        for(EntityPlayer targetedPlayer: getTargets()) {
            // find best location to place
            List<BlockPos> targetsBlocks = getPlaceableBlocks(targetedPlayer);
            List<BlockPos> blocks = getPlaceableBlocks(MC.player);

            for(BlockPos pos: blocks) {
                Vec3d calcPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);

                if(!targetsBlocks.contains(pos)
                        || (double) MathUtils.getDamage(calcPos, targetedPlayer, predictMovement.getValue()) < minDamage.getValue()
                        || preventSuicide.getValue() && MC.player.getHealth() - MathUtils.getDamage(calcPos, MC.player, false) <= 1
                ) continue;

                double score = MathUtils.getScore(calcPos, targetedPlayer, calcMode.getValue(), predictMovement.getValue());

                if (target != null) {
                    targetPlayer = targetedPlayer;
                } else targetPlayer = null;

                if(target == null || (score < bestScore && score != -1)) {
                    target = pos;
                    bestScore = score;
                }
            }
        }
        return target;
    }

    private List<EntityPlayer> getTargets() {
        List<EntityPlayer> targets = new ArrayList<>();

        if(targetSetting.getValue() == Target.CLOSEST) {
            targets.addAll(MC.world.playerEntities.stream().filter(this::isValidTarget).collect(Collectors.toList()));
            targets.sort(Comparators.entityDistance);
        } else if(targetSetting.getValue() == Target.MOST_DAMAGE) {
            for(EntityPlayer entityPlayer: MC.world.playerEntities) {
                if (!isValidTarget(entityPlayer))
                    continue;
                targets.add(entityPlayer);
            }
        }

        return targets;
    }

    private boolean isValidTarget(EntityPlayer player) {
        return !FriendManager.isFriend(player.getGameProfile().getName())
                && !player.isDead
                && !(player.getHealth() <= 0)
                && !(MC.player.getDistance(player) > Math.max(placeRange.getValue(), breakRange.getValue()) + 8)
                && player != MC.player;
    }

    private List<BlockPos> getPlaceableBlocks(EntityPlayer player) {
        List<BlockPos> square = new ArrayList<>();

        int range = (int) Utils.roundDouble(placeRange.getValue(), 0);

        BlockPos pos = player.getPosition();
        if(predictMovement.getValue()) pos.add(new Vec3i(player.motionX, player.motionY, player.motionZ));

        for(int x = -range; x <= range; x++)
            for(int y = -range; y <= range; y++)
                for(int z = -range; z <= range; z++)
                    square.add(pos.add(x, y, z));

        return square.stream().filter(blockPos -> canCrystalBePlacedHere(blockPos) && MC.player.getDistanceSq(blockPos) <= (range * range)).collect(Collectors.toList());
    }

    private boolean canCrystalBePlacedHere(BlockPos pos) {
        BlockPos boost = pos.add(0, 1, 0);
        if (oneDotThirteen.getValue()) {
            return (MC.world.getBlockState(pos).getBlock() == Blocks.BEDROCK
                    || MC.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN)
                    && MC.world.getBlockState(boost).getBlock() == Blocks.AIR
                    && MC.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).stream().allMatch(entity -> entity instanceof EntityEnderCrystal && !isCrystalLost((EntityEnderCrystal) entity));
        } else {
            BlockPos boost2 = pos.add(0, 2, 0);
            return (MC.world.getBlockState(pos).getBlock() == Blocks.BEDROCK
                    || MC.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN)
                    && MC.world.getBlockState(boost).getBlock() == Blocks.AIR
                    && MC.world.getBlockState(boost2).getBlock() == Blocks.AIR
                    && MC.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).stream().allMatch(entity -> entity instanceof EntityEnderCrystal && !isCrystalLost((EntityEnderCrystal) entity))
                    && MC.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).stream().allMatch(entity -> entity instanceof EntityEnderCrystal && !isCrystalLost((EntityEnderCrystal) entity));
        }
    }

    private boolean isCrystalLost(EntityEnderCrystal entity) {
        if(spawnedCrystals.containsKey(entity) && preventSuicide.getValue())
            return MC.player.getHealth() - MathUtils.getDamage(entity.getPositionVector(), MC.player, false) <= 1;
        if(lostCrystals.containsKey(entity)) {
            return entity.ticksExisted >= lostCrystals.get(entity) + lostWindow.getValue();
        }
        return false;
    }
}
