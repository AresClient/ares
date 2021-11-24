package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.Pair;
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
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.ares.fabric.utils.entity.PlayerUtils;
import dev.tigr.ares.fabric.utils.entity.SelfUtils;
import dev.tigr.ares.fabric.utils.render.RenderUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import dev.tigr.simpleevents.listener.Priority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedGoldenAppleItem;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static dev.tigr.ares.fabric.impl.modules.player.RotationManager.ROTATIONS;
import static dev.tigr.ares.fabric.utils.HotbarTracker.HOTBAR_TRACKER;

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
    private final Setting<Switch> doSwitch = register(new EnumSetting<>("Switch", Switch.NORMAL)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Boolean> noGappleSwitch = register(new BooleanSetting("No Gapple Switch", false)).setVisibility(() -> page.getValue() == Page.GENERAL && doSwitch.getValue() != Switch.NONE);
    private final Setting<Boolean> preventSuicide = register(new BooleanSetting("Prevent Suicide", true)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<MathUtils.DmgCalcMode> calcMode = register(new EnumSetting<>("Dmg Calc Mode", MathUtils.DmgCalcMode.DAMAGE)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Target> targetSetting = register(new EnumSetting<>("Target", Target.CLOSEST)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Rotations> rotateMode = register(new EnumSetting<>("Rotations", Rotations.PACKET)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Order> order = register(new EnumSetting<>("Order", Order.PLACE_BREAK)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Boolean> pauseOnEat = register(new BooleanSetting("Pause On Eat", true)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Boolean> pauseOnPot = register(new BooleanSetting("Pause On Pot", true)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Boolean> pauseOnXP = register(new BooleanSetting("Pause On XP", true)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Boolean> pauseOnMine = register(new BooleanSetting("Pause On Mine", false)).setVisibility(() -> page.getValue() == Page.GENERAL);

    //Place Page
    private final Setting<Double> placeRange = register(new DoubleSetting("Place Range", 5, 0, 10)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Integer> placeDelay = register(new IntegerSetting("Place Delay", 2, 0, 20)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Integer> placeOffhandDelay = register(new IntegerSetting("Offh. Place Delay", 2, 0, 20)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Float> minDamage = register(new FloatSetting("Minimum Damage", 7.5f, 0, 15)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Boolean> oneDotThirteen = register(new BooleanSetting("1.13+", true)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Boolean> antiSurround = register(new BooleanSetting("Anti-Surround", true)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Boolean> predictMovement = register(new BooleanSetting("Predict Movement", true)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Boolean> strictSides = register(new BooleanSetting("Strict Sides", false)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<InteractAt> interactAt = register(new EnumSetting<>("Interact At", InteractAt.CLOSEST_POINT)).setVisibility(() -> page.getValue() == Page.PLACE);

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

    //Render Page
    private final Setting<Float> colorRed = register(new FloatSetting("Red", 0.69f, 0f, 1f)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> colorGreen = register(new FloatSetting("Green", 0f, 0f, 1f)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> colorBlue = register(new FloatSetting("Blue", 0f, 0f, 1f)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> fillAlpha = register(new FloatSetting("Fill Alpha", 0.24f, 0f, 1f)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> boxAlpha = register(new FloatSetting("Line Alpha", 1f, 0f, 1f)).setVisibility(() -> page.getValue() == Page.RENDER);
    private final Setting<Float> lineThickness = register(new FloatSetting("Line Weight", 2.4f, 0f, 10f)).setVisibility(() -> page.getValue() == Page.RENDER);

    enum Switch { NORMAL, SILENT, NONE }
    enum BreakMode { OWN, SMART, ALL }
    enum Order { PLACE_BREAK, BREAK_PLACE }
    enum Target { CLOSEST, MOST_DAMAGE }
    enum Rotations { PACKET, REAL, NONE }
    enum InteractAt { CLOSEST_POINT, STRICT_SIDE, CENTER }

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
    private PlayerEntity targetPlayer;
    private double pingWindow = 0;

    final int key = Priorities.Rotation.CRYSTAL_AURA;
    final int generalPriority = Priorities.Rotation.CRYSTAL_AURA;
    final int yawstepPriority = Priorities.Rotation.YAW_STEP;

    public CrystalAura() {
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        if(targetPlayer != null
                && !targetPlayer.removed
                && !PlayerUtils.hasZeroHealth(targetPlayer)
                && MathUtils.isInRange(SelfUtils.getEyePos(), targetPlayer.getPos(), targetRange())
        ) return targetPlayer.getGameProfile().getName();
        else return "null";
    }

    @Override
    public void onEnable() {
        HOTBAR_TRACKER.connect();
    }

    @Override
    public void onDisable() {
        ROTATIONS.setCompletedAction(key, true);
        HOTBAR_TRACKER.disconnect();
    }

    @Override
    public void onTick() {
        run();
    }

    private void run() {
        // Get ping for timing how long to wait before a crystal is lost
        if(MC.player != null) pingWindow = MC.player.networkHandler.getPlayerListEntry(MC.player.getUuid()).getLatency() / 50D;

        // Ensure it doesn't spam illegal place and break interactions without being rotated
        if(rotateMode.getValue() == Rotations.PACKET) {
            if(!ROTATIONS.isKeyCurrent(key) && !ROTATIONS.isCompletedAction() && ROTATIONS.getCurrentPriority() > key) return;
        }

        //Add crystals to spawnedcrystals map if they meet the requirements
        for(EndCrystalEntity c: SelfUtils.getEndCrystalsInRadius(Math.max(placeRange.getValue(), breakRange.getValue()) +2)) {
            if(breakMode.getValue() == BreakMode.ALL) {
                if(lostCrystals.containsKey(c)) {
                    if(c.age < lostCrystals.get(c) + pingWindow) continue;
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
                    if(c.age < lostCrystals.get(c) + pingWindow + retryAfter.getValue()) continue;
                }
                spawnedCrystals.put(c, new AtomicInteger(0));
                lostCrystals.remove(c);
            }
        }

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
        int oldSelection = MC.player.inventory.selectedSlot;
        int slot = InventoryUtils.findItemInHotbar(Items.END_CRYSTAL);
        if(!offhand && MC.player.inventory.getMainHandStack().getItem() != Items.END_CRYSTAL) {
            if(doSwitch.getValue() != Switch.NONE) {
                if(slot != -1) {
                    HOTBAR_TRACKER.setSlot(slot, true, -1);
                    HOTBAR_TRACKER.sendSlot();
                    if(doSwitch.getValue() != Switch.SILENT)
                        MC.player.inventory.selectedSlot = slot;
                }
            } else return;
        }

        Runnable switchBack = () -> {
            if(!offhand && oldSelection != slot && doSwitch.getValue() == Switch.SILENT)
                HOTBAR_TRACKER.reset();
        };

        // Get the best interaction point and side, as according to preferences
        Pair<Direction, Vec3d> closestVisibleSide = WorldUtils.getClosestVisibleSide(SelfUtils.getEyePos(), pos);

        Vec3d interactPoint;
        switch(interactAt.getValue()) {
            case CLOSEST_POINT:
                interactPoint = MathUtils.getClosestPointOfBlockPos(SelfUtils.getEyePos(), pos);
                break;
            case STRICT_SIDE:
                if(closestVisibleSide == null || closestVisibleSide.getSecond() == null) {
                    switchBack.run();
                    return;
                }
                interactPoint = closestVisibleSide.getSecond();
                break;
            default:
                // On 1.12 you send the packet with the pos within the blockPos itself (0 to 1) while on Fabric you send the position within the world (pos + (0 to 1))
                interactPoint = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }

        Direction interactSide = Direction.UP;
        if(strictSides.getValue()) {
            if(closestVisibleSide == null || closestVisibleSide.getFirst() == null) {
                switchBack.run();
                return;
            }
            interactSide = closestVisibleSide.getFirst();
        }

        // place
        MC.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, new BlockHitResult(interactPoint, interactSide, pos, false)));
        rotationTimer.reset();
        rotatePos = pos;

        // add to place map
        placedCrystals.put(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), System.nanoTime() / 1000000);

        // set render pos
        target = pos;

        // silent swap
        switchBack.run();
    }

    private void explode(boolean offhand) {
        if(!shouldBreakCrystal(offhand)) return;

        //Smart Mode Break
        if(breakMode.getValue() == BreakMode.SMART) {
            EndCrystalEntity crystal = getBestBreakCrystal();
            if(crystal == null) return;

            if(!spawnedCrystals.containsKey(crystal)) spawnedCrystals.put(crystal, new AtomicInteger(0));

            breakCrystal(crystal, offhand);
            postBreak(crystal);
            return;
        }

        //Own or All Mode break (just breaks whatever's in the spawnedCrystals list)
        for(Map.Entry<EndCrystalEntity, AtomicInteger> entry: spawnedCrystals.entrySet()) {
            if(!canBreakCrystal(entry.getKey())) continue; // check if crystal can be broken

            breakCrystal(entry.getKey(), offhand);
            postBreak(entry.getKey());
        }
    }

    private void breakCrystal(EndCrystalEntity crystal, boolean offhand) {
        // find hand
        Hand hand = offhand ? Hand.OFF_HAND : Hand.MAIN_HAND;

        // break
        MC.interactionManager.attackEntity(MC.player, crystal);
        MC.player.swingHand(hand);

        //spoof rotations
        rotationTimer.reset();
        rotatePos = crystal.getBlockPos();

        // reset timer
        breakTimer.reset();
    }

    //Remove crystal if it hits limit of tries
    private void postBreak(EndCrystalEntity crystal) {
        if(spawnedCrystals.get(crystal) == null) return;

        if(spawnedCrystals.get(crystal).get() + 1 == maxBreakTries.getValue()) {
            lostCrystals.put(crystal, crystal.age);
            spawnedCrystals.remove(crystal);
        } else spawnedCrystals.get(crystal).set(spawnedCrystals.get(crystal).get() +1);
    }

    //Loop through all crystals in the area and calculate the score for each player in the area, finding the best one
    private EndCrystalEntity getBestBreakCrystal() {
        double bestScore = 69420;
        EndCrystalEntity crystal = null;
        for(EndCrystalEntity c: SelfUtils.getEndCrystalsInRadius(breakRange.getValue())) {
            if(!canBreakCrystal(c)) continue;

            if(lostCrystals.containsKey(c)) {
                if(c.age < lostCrystals.get(c) + pingWindow + retryAfter.getValue()) continue;
                else lostCrystals.remove(c);
            }

            Vec3d cPos = Vec3d.ofCenter(c.getBlockPos());
            for(PlayerEntity player: WorldUtils.getPlayersInRadius(cPos, 10)) {
                if(!isValidTarget(player) || MathUtils.getDamage(cPos, player, false) < minDamage.getValue()) continue;

                double score = MathUtils.getScore(cPos, player, calcMode.getValue(), false);
                if(score < bestScore && score != -1) {
                    bestScore = score;
                    crystal = c;
                }
            }
        }

        return crystal;
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
            ExplosionS2CPacket packet = (ExplosionS2CPacket) event.getPacket();
            for(EndCrystalEntity crystal : WorldUtils.getEndCrystalsInBox(new BlockPos(packet.getX(), packet.getY(), packet.getZ()), 6)) {
                if(crystal.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) <= 36) {
                    //Remove from all these lists because we can be sure it has broken if the packet was received
                    spawnedCrystals.remove(crystal);
                    lostCrystals.remove(crystal);
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
        entities.addAll(MC.world.getOtherEntities(SelfUtils.getPlayer(), new Box(pos.add(1, 0, 0))));
        entities.addAll(MC.world.getOtherEntities(SelfUtils.getPlayer(), new Box(pos.add(-1, 0, 0))));
        entities.addAll(MC.world.getOtherEntities(SelfUtils.getPlayer(), new Box(pos.add(0, 0, 1))));
        entities.addAll(MC.world.getOtherEntities(SelfUtils.getPlayer(), new Box(pos.add(0, 0, -1))));
        return entities.stream().anyMatch(entity -> entity instanceof PlayerEntity);
    }

    private boolean shouldOffhand() {
        return MC.player.getMainHandStack().getItem() == Items.END_CRYSTAL;
    }

    private boolean shouldBreakCrystal(boolean offhand) {
        return breakTimer.passedTicks(offhand ? breakOffhandDelay.getValue() : breakDelay.getValue());
    }

    private boolean canBreakCrystal(EndCrystalEntity crystal) {
        return MathUtils.isInRange(SelfUtils.getEyePos(), Vec3d.ofCenter(crystal.getBlockPos()), breakRange.getValue()) // check range
                && !(MC.player.getHealth() - MathUtils.getDamage(crystal.getPos(), SelfUtils.getPlayer(), false) <= 1 && preventSuicide.getValue()) // check suicide
                && crystal.age >= breakAge.getValue(); // check that the crystal has been in the world for the minimum age specified
    }

    private BlockPos getBestPlacement() {
        double bestScore = 69420;
        BlockPos target = null;
        for(PlayerEntity targetedPlayer: getTargets()) {
            // find best location to place
            List<BlockPos> targetsBlocks = getPlaceableBlocks(targetedPlayer);
            List<BlockPos> blocks = getPlaceableBlocks(SelfUtils.getPlayer());

            for(BlockPos pos: blocks) {
                Vec3d calcPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);

                if(!targetsBlocks.contains(pos)
                        || (double) MathUtils.getDamage(calcPos, targetedPlayer, predictMovement.getValue()) < minDamage.getValue()
                        || preventSuicide.getValue() && MC.player.getHealth() - MathUtils.getDamage(calcPos, SelfUtils.getPlayer(), false) <= 1
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

    private List<PlayerEntity> getTargets() {
        List<PlayerEntity> targets = new ArrayList<>();

        if(targetSetting.getValue() == Target.CLOSEST) {
            targets.addAll(SelfUtils.getPlayersInRadius(targetRange()).stream().filter(this::isValidTarget).collect(Collectors.toList()));
            targets.sort(Comparators.entityDistance);
        } else if(targetSetting.getValue() == Target.MOST_DAMAGE) {
            for(PlayerEntity player: SelfUtils.getPlayersInRadius(targetRange())) {
                if(!isValidTarget(player))
                    continue;
                targets.add(player);
            }
        }

        return targets;
    }

    private boolean isValidTarget(PlayerEntity player) {
        return PlayerUtils.isValidTarget(player, targetRange());
    }

    private double targetRange() {
        return Math.max(placeRange.getValue(), breakRange.getValue()) + 8;
    }

    private List<BlockPos> getPlaceableBlocks(PlayerEntity player) {
        List<BlockPos> square = new ArrayList<>();

        int range = (int) Math.ceil(placeRange.getValue());

        BlockPos pos = player.getBlockPos();
        if(predictMovement.getValue()) pos.add(new Vec3i(player.getVelocity().x, player.getVelocity().y, player.getVelocity().z));

        for(int x = -range; x <= range; x++)
            for(int y = -range; y <= range; y++)
                for(int z = -range; z <= range; z++)
                    square.add(pos.add(x, y, z));

        return square.stream().filter(blockPos -> canCrystalBePlacedHere(blockPos) && MathUtils.isInRange(SelfUtils.getEyePos(), Vec3d.ofCenter(blockPos), Utils.roundDouble(placeRange.getValue(), 2)) && (!strictSides.getValue() && interactAt.getValue() != InteractAt.STRICT_SIDE || WorldUtils.getVisibleBlockSides(SelfUtils.getEyePos(), blockPos) != null)).collect(Collectors.toList());
    }

    private boolean canCrystalBePlacedHere(BlockPos pos) {
        BlockPos boost = pos.add(0, 1, 0);
        if(oneDotThirteen.getValue()) {
            return (MC.world.getBlockState(pos).getBlock() == Blocks.BEDROCK
                    || MC.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN)
                    && MC.world.getBlockState(boost).getBlock() == Blocks.AIR
                    && MC.world.getNonSpectatingEntities(Entity.class, new Box(boost)).stream().allMatch(entity -> entity instanceof EndCrystalEntity && notCrystalLost((EndCrystalEntity) entity));
        } else {
            BlockPos boost2 = pos.add(0, 2, 0);
            return (MC.world.getBlockState(pos).getBlock() == Blocks.BEDROCK
                    || MC.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN)
                    && MC.world.getBlockState(boost).getBlock() == Blocks.AIR
                    && MC.world.getBlockState(boost2).getBlock() == Blocks.AIR
                    && MC.world.getNonSpectatingEntities(Entity.class, new Box(boost)).stream().allMatch(entity -> entity instanceof EndCrystalEntity && notCrystalLost((EndCrystalEntity) entity))
                    && MC.world.getNonSpectatingEntities(Entity.class, new Box(boost2)).stream().allMatch(entity -> entity instanceof EndCrystalEntity && notCrystalLost((EndCrystalEntity) entity));
        }
    }

    private boolean notCrystalLost(EndCrystalEntity entity) {
        if(spawnedCrystals.containsKey(entity) && preventSuicide.getValue())
            return !(MC.player.getHealth() - MathUtils.getDamage(entity.getPos(), MC.player, false) <= 1);
        if(lostCrystals.containsKey(entity))
            return entity.age < lostCrystals.get(entity) + pingWindow;
        if(!spawnedCrystals.containsKey(entity) && !lostCrystals.containsKey(entity)) {
            if(breakMode.getValue() == BreakMode.SMART || breakMode.getValue() == BreakMode.ALL)
                return MathUtils.isInRange(SelfUtils.getEyePos(), entity.getPos(), breakRange.getValue());
            else return false;
        }
        return true;
    }
}
