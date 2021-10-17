package dev.tigr.ares.fabric.impl.modules.combat;

import com.google.common.collect.Streams;
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
import dev.tigr.ares.fabric.event.client.EntityEvent;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.event.player.DestroyBlockEvent;
import dev.tigr.ares.fabric.utils.Comparators;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.MathUtils;
import dev.tigr.ares.fabric.utils.entity.PlayerUtils;
import dev.tigr.ares.fabric.utils.render.RenderUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import dev.tigr.simpleevents.listener.Priority;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedGoldenAppleItem;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static dev.tigr.ares.fabric.impl.modules.player.RotationManager.ROTATIONS;

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
    private final Setting<Boolean> predictMovement = register(new BooleanSetting("Predict Movement", true)).setVisibility(() -> page.getValue() == Page.GENERAL);

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
    private final Setting<Float> colorRed = register(new FloatSetting("Red", 0.69f, 0f, 1f)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> colorGreen = register(new FloatSetting("Green", 0f, 0f, 1f)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> colorBlue = register(new FloatSetting("Blue", 0f, 0f, 1f)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> fillAlpha = register(new FloatSetting("Fill Alpha", 0.24f, 0f, 1f)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> boxAlpha = register(new FloatSetting("Line Alpha", 1f, 0f, 1f)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> lineThickness = register(new FloatSetting("Line Weight", 2.4f, 0f, 10f)).setVisibility(() -> page.getValue() == Page.RENDER);

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
    private final LinkedHashMap<EndCrystalEntity, AtomicInteger> spawnedCrystals = new LinkedHashMap<>();
    private final LinkedHashMap<EndCrystalEntity, Integer> lostCrystals = new LinkedHashMap<>();
    private Entity targetPlayer;

    final int key = Priorities.Rotation.CRYSTAL_AURA;
    final int generalPriority = Priorities.Rotation.CRYSTAL_AURA;
    final int yawstepPriority = Priorities.Rotation.YAW_STEP;

    public CrystalAura() {
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        if (targetPlayer != null
                && !targetPlayer.removed
                && !PlayerUtils.hasZeroHealth(targetPlayer)
                && !(MC.player.distanceTo(targetPlayer) > Math.max(placeRange.getValue(), breakRange.getValue()) + 8)) {
            if(targetPlayer instanceof PlayerEntity) return ((PlayerEntity)targetPlayer).getGameProfile().getName();
            else if(targetPlayer instanceof OtherClientPlayerEntity) return targetPlayer.getDisplayName().asString();
            else return "null";
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
            for(Entity entity : MC.world.getEntities()) {
                if(entity.distanceTo(MC.player) > Math.max(placeRange.getValue(), breakRange.getValue()) +2) continue;
                if(entity instanceof EndCrystalEntity) {
                    EndCrystalEntity c = (EndCrystalEntity) entity;
                    if(breakMode.getValue() == BreakMode.SMART) {
                        // Check if the player wants to retry breaking lost crystals
                        if(!retryLost.getValue() && lostCrystals.containsKey(c)) continue;
                        if(lostCrystals.containsKey(c)) {
                            if(c.age < lostCrystals.get(c) + lostWindow.getValue() + retryAfter.getValue()) continue;
                        }
                        if(MathUtils.getDamage(entity.getPos(), targetPlayer, predictMovement.getValue()) >= (minDamage.getValue() / 2) && !spawnedCrystals.containsKey(c)) {
                            //add crystal to spawned list so that it can be broken.
                            spawnedCrystals.put(c, new AtomicInteger(0));
                            lostCrystals.remove(c);
                            continue;
                        }
                    }
                    if(breakMode.getValue() == BreakMode.ALL) {
                        if(lostCrystals.containsKey(c)) {
                            if(c.age < lostCrystals.get(c) + lostWindow.getValue()) continue;
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
                            if(c.age < lostCrystals.get(c) + lostWindow.getValue() + retryAfter.getValue()) continue;
                        }
                        spawnedCrystals.put(c, new AtomicInteger(0));
                        lostCrystals.remove(c);
                        continue;
                    }
                }
            }
        });

        // pause with options
        if((pauseOnEat.getValue() && MC.player.isUsingItem() && (MC.player.getMainHandStack().getItem().isFood() || MC.player.getOffHandStack().getItem().isFood())) ||
                (pauseOnPot.getValue() && MC.player.isUsingItem() && (MC.player.getMainHandStack().getItem() instanceof PotionItem || MC.player.getOffHandStack().getItem() instanceof PotionItem)) ||
                (pauseOnXP.getValue() && MC.player.isUsingItem() && (MC.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE || MC.player.getOffHandStack().getItem() == Items.EXPERIENCE_BOTTLE)) ||
                (pauseOnMine.getValue() && MC.interactionManager.isBreakingBlock()))
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
        if(cleanupTimer.passedSec(3)) {
            target = null;
            renderTimer.reset();
        }

        // do logic
        boolean offhand = MC.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        if(order.getValue() == Order.PLACE_BREAK) {
            place(offhand);
            explode(offhand);
        } else {
            explode(offhand);
            place(offhand);
        }

        // cleanup place map and lost crystals every ten seconds
        if(cleanupTimer.passedSec(10)) {
            for(Map.Entry<EndCrystalEntity, Integer> entry: lostCrystals.entrySet()) {
                if(MC.world.getEntityById(entry.getKey().getEntityId()) == null)
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

        // rotate for actual mode
        if(rotations != null && rotateMode.getValue() == Rotations.REAL) {
            MC.player.pitch = (float) rotations[1];
            MC.player.yaw = (float) rotations[0];
        }
    }

    private void place(boolean offhand) {
        if(placeTimer.passedTicks(offhand ? placeOffhandDelay.getValue() : placeDelay.getValue())) {
            // if no gapple switch and player is holding apple
            if(!offhand && noGappleSwitch.getValue() && MC.player.inventory.getMainHandStack().getItem() instanceof EnchantedGoldenAppleItem) {
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
        if(!offhand && MC.player.inventory.getMainHandStack().getItem() != Items.END_CRYSTAL) {
            if(doSwitch.getValue()) {
                int slot = InventoryUtils.findItemInHotbar(Items.END_CRYSTAL);
                if (slot != -1) {
                    MC.player.inventory.selectedSlot = slot;
                    MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket());
                }
            } else return;
        }

        // place
        MC.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, new BlockHitResult(new Vec3d(0.5f, 0.5f, 0.5f), Direction.UP, pos, false)));
        rotationTimer.reset();
        rotatePos = pos;

        // add to place map
        placedCrystals.put(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), System.nanoTime() / 1000000);

        // set render pos
        target = pos;
    }

    private void explode(boolean offhand) {
        if(!shouldBreakCrystal(offhand)) return;

        for(Map.Entry<EndCrystalEntity, AtomicInteger> entry: spawnedCrystals.entrySet()) {
            // check if crystal can be broken
            if(!canBreakCrystal(entry.getKey())) continue;

            breakCrystal(entry.getKey(), offhand);

            // remove if it hits limit of tries
            if(entry.getValue().get() + 1 == maxBreakTries.getValue()) {
                lostCrystals.put(entry.getKey(), entry.getKey().age);
                spawnedCrystals.remove(entry.getKey());
            }
            else entry.getValue().set(entry.getValue().get() + 1);
        }
    }

    @EventHandler
    public EventListener<EntityEvent.Spawn> spawnEntityEvent = new EventListener<>(Priority.HIGHEST, event -> {
        if(event.getEntity() instanceof EndCrystalEntity) {
            EndCrystalEntity crystal = (EndCrystalEntity) event.getEntity();

            // loop through all placed crystals to see if it matches
            for(Map.Entry<Vec3d, Long> entry: new ArrayList<>(placedCrystals.entrySet())) {
                if(entry.getKey().equals(crystal.getPos())) {
                    // break crystal if possible and add to spawned crystals map
                    boolean offhand = shouldOffhand();
                    if(shouldBreakCrystal(offhand) && canBreakCrystal(crystal) && breakOnSpawn.getValue()) {
                        breakCrystal(crystal, offhand);
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
        if(event.getEntity() instanceof EndCrystalEntity) {
            EndCrystalEntity crystal = (EndCrystalEntity) event.getEntity();
            BlockPos pos = event.getEntity().getBlockPos().down();
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

    //Remove Crystals from lists on Explosion packet received
    @EventHandler
    private EventListener<PacketEvent.Receive> packetReceiveListener = new EventListener<>(event -> {
        if(event.getPacket() instanceof ExplosionS2CPacket) {
            final ExplosionS2CPacket packet = (ExplosionS2CPacket) event.getPacket();
            for(Entity e : MC.world.getEntities()) {
                if(e instanceof EndCrystalEntity) {
                    if(e.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) <= 36) {
                        //Remove from all these lists because we can be sure it has broken if the packet was received
                        spawnedCrystals.remove(e);
                        lostCrystals.remove(e);
                    }
                }
            }
        }
    });

    // draw target
    @Override
    public void onRender3d() {
        if(target != null) {
            Color fillColor = new Color(colorRed.getValue(), colorGreen.getValue(), colorBlue.getValue(), fillAlpha.getValue());
            Color outlineColor = new Color(colorRed.getValue(), colorGreen.getValue(), colorBlue.getValue(), boxAlpha.getValue());

            Box bb = RenderUtils.getBoundingBox(target);
            if(bb != null) {
                RenderUtils.prepare3d();
                RenderUtils.cube(bb, fillColor, outlineColor, lineThickness.getValue());
                RenderUtils.end3d();
            }
        }
    }

    private boolean isPartOfHole(BlockPos pos) {
        List<Entity> entities = new ArrayList<>();
        entities.addAll(MC.world.getOtherEntities(MC.player, new Box(pos.add(1, 0, 0))));
        entities.addAll(MC.world.getOtherEntities(MC.player, new Box(pos.add(-1, 0, 0))));
        entities.addAll(MC.world.getOtherEntities(MC.player, new Box(pos.add(0, 0, 1))));
        entities.addAll(MC.world.getOtherEntities(MC.player, new Box(pos.add(0, 0, -1))));
        return entities.stream().anyMatch(entity -> entity instanceof PlayerEntity)
                || entities.stream().anyMatch(entity -> entity instanceof OtherClientPlayerEntity);
    }

    private boolean shouldOffhand() {
        return MC.player.getMainHandStack().getItem() == Items.END_CRYSTAL;
    }

    private boolean shouldBreakCrystal(boolean offhand) {
        return breakTimer.passedTicks(offhand ? breakOffhandDelay.getValue() : breakDelay.getValue());
    }

    private boolean canBreakCrystal(EndCrystalEntity crystal) {
        return MC.player.distanceTo(crystal) <= breakRange.getValue() // check range
                && !(MC.player.getHealth() - MathUtils.getDamage(crystal.getPos(), MC.player, false) <= 1 && preventSuicide.getValue()) // check suicide
                && crystal.age >= breakAge.getValue(); // check that the crystal has been in the world for the minimum age specified
    }

    private void breakCrystal(EndCrystalEntity crystal, boolean offhand) {
        // find hand
        Hand hand = offhand ? Hand.OFF_HAND : Hand.MAIN_HAND;

        // break
        if(sync.getValue()) MC.player.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(crystal, hand, false));
        MC.interactionManager.attackEntity(MC.player, crystal);
        MC.player.swingHand(hand);

        //spoof rotations
        rotationTimer.reset();
        rotatePos = crystal.getBlockPos();

        // reset timer
        breakTimer.reset();
    }

    private BlockPos getBestPlacement() {
        double bestScore = 69420;
        BlockPos target = null;
        for(Entity targetedPlayer: getTargets()) {
            // find best location to place
            List<BlockPos> targetsBlocks = getPlaceableBlocks(targetedPlayer);
            List<BlockPos> blocks = getPlaceableBlocks(MC.player);

            for(BlockPos pos: blocks) {
                Vec3d calcPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);

                if(!targetsBlocks.contains(pos)
                        || (double) MathUtils.getDamage(calcPos, targetedPlayer, predictMovement.getValue()) < minDamage.getValue()
                        || preventSuicide.getValue() && MC.player.getHealth() - MathUtils.getDamage(calcPos, MC.player, false) <= 1
                )
                    continue;

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

    private List<Entity> getTargets() {
        List<Entity> targets = new ArrayList<>();

        if(targetSetting.getValue() == Target.CLOSEST) {
            targets.addAll(Streams.stream(MC.world.getEntities()).filter(this::isValidTarget).collect(Collectors.toList()));
            targets.sort(Comparators.entityDistance);
        } else if(targetSetting.getValue() == Target.MOST_DAMAGE) {
            for(Entity entity: MC.world.getEntities()) {
                if(!isValidTarget(entity))
                    continue;
                targets.add(entity);
            }
        }

        return targets;
    }

    private boolean isValidTarget(Entity entity) {
        return PlayerUtils.isValidTarget(entity, Math.max(placeRange.getValue(), breakRange.getValue()) + 8);
    }

    private List<BlockPos> getPlaceableBlocks(Entity player) {
        List<BlockPos> square = new ArrayList<>();

        int range = (int) Utils.roundDouble(placeRange.getValue(), 0);

        BlockPos pos = player.getBlockPos();
        if(predictMovement.getValue()) pos.add(new Vec3i(player.getVelocity().x, player.getVelocity().y, player.getVelocity().z));

        for(int x = -range; x <= range; x++)
            for(int y = -range; y <= range; y++)
                for(int z = -range; z <= range; z++)
                    square.add(pos.add(x, y, z));

        return square.stream().filter(blockPos -> canCrystalBePlacedHere(blockPos) && MC.player.squaredDistanceTo(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5) <= (range * range)).collect(Collectors.toList());
    }

    private boolean canCrystalBePlacedHere(BlockPos pos) {
        BlockPos boost = pos.add(0, 1, 0);
        if(oneDotThirteen.getValue()) {
            return (MC.world.getBlockState(pos).getBlock() == Blocks.BEDROCK
                    || MC.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN)
                    && MC.world.getBlockState(boost).getBlock() == Blocks.AIR
                    && MC.world.getNonSpectatingEntities(Entity.class, new Box(boost)).stream().allMatch(entity -> entity instanceof EndCrystalEntity && !isCrystalLost((EndCrystalEntity) entity));
        } else {
            BlockPos boost2 = pos.add(0, 2, 0);
            return (MC.world.getBlockState(pos).getBlock() == Blocks.BEDROCK
                    || MC.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN)
                    && MC.world.getBlockState(boost).getBlock() == Blocks.AIR
                    && MC.world.getBlockState(boost2).getBlock() == Blocks.AIR
                    && MC.world.getNonSpectatingEntities(Entity.class, new Box(boost)).stream().allMatch(entity -> entity instanceof EndCrystalEntity && !isCrystalLost((EndCrystalEntity) entity))
                    && MC.world.getNonSpectatingEntities(Entity.class, new Box(boost2)).stream().allMatch(entity -> entity instanceof EndCrystalEntity && !isCrystalLost((EndCrystalEntity) entity));
        }
    }

    private boolean isCrystalLost(EndCrystalEntity entity) {
        if(spawnedCrystals.containsKey(entity) && preventSuicide.getValue())
            return MC.player.getHealth() - MathUtils.getDamage(entity.getPos(), MC.player, false) <= 1;
        if(lostCrystals.containsKey(entity)) {
            return entity.age >= lostCrystals.get(entity) + lostWindow.getValue();
        }
        return false;
    }
}
