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
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.event.client.EntityEvent;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.event.player.DestroyBlockEvent;
import dev.tigr.ares.fabric.utils.Comparators;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.RenderUtils;
import dev.tigr.ares.fabric.utils.Timer;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import dev.tigr.simpleevents.listener.Priority;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedGoldenAppleItem;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.explosion.Explosion;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    private final Setting<Boolean> predictMovement = register(new BooleanSetting("Predict Movement", true)).setVisibility(() -> page.getValue() == Page.GENERAL);
    private final Setting<Boolean> preventSuicide = register(new BooleanSetting("Prevent Suicide", true)).setVisibility(() -> page.getValue() == Page.GENERAL);
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
    private final Setting<Boolean> oneDotTwelve = register(new BooleanSetting("1.12-", false)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Boolean> antiSurround = register(new BooleanSetting("Anti-Surround", true)).setVisibility(() -> page.getValue() == Page.PLACE);
    private final Setting<Mode> placeMode = register(new EnumSetting<>("Place Mode", Mode.DAMAGE)).setVisibility(() -> page.getValue() == Page.PLACE);

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
    private final Setting<Float> expandRender = register(new FloatSetting("Box Scale", 0f, -0.12f, 0.12f)).setVisibility(() -> page.getValue() == Page.RENDER);

    enum Mode { DAMAGE, DISTANCE }
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

    public CrystalAura() {
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        if (targetPlayer != null
                && !targetPlayer.removed
                && !WorldUtils.hasZeroHealth(targetPlayer)
                && !(MC.player.distanceTo(targetPlayer) > Math.max(placeRange.getValue(), breakRange.getValue()) + 8)) {
            if(targetPlayer instanceof PlayerEntity) return ((PlayerEntity)targetPlayer).getGameProfile().getName();
            else if(targetPlayer instanceof OtherClientPlayerEntity) return targetPlayer.getDisplayName().asString();
            else return "null";
        }
        else return "null";
    }

    @Override
    public void onTick() {
        run();
    }

    private void run() {
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
                        if(getDamage(entity.getPos(), targetPlayer) >= (minDamage.getValue() / 2) && !spawnedCrystals.containsKey(c)) {
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
        /*
        There is no point in performing this more than once per tick - movement packets are only sent on tick anyways.
        So instead of performing this on place / on break, rotate onTick towards a position. This will also help to
        smoothen the rotation out somewhat while moving (which, in absence of yawsteps, will help somewhat) and by
        using a reset delay of 10 ticks (may need adjustment) we can avoid micro resets of the rotation of the player
        in between place/breaks which should help alleviate rubberbanding problems with NCP's fly check. -Makrennel
         */
        if(rotations != null && rotationTimer.passedTicks(10)) {
            rotatePos = null;
            rotations = null;
            rotationTimer.reset();
        } else if(rotatePos != null) {
            rotations = WorldUtils.calculateLookAt(rotatePos.getX() + 0.5, rotatePos.getY() + 0.5, rotatePos.getZ() + 0.5, MC.player);
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

    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        // rotation spoofing
        if(event.getPacket() instanceof PlayerMoveC2SPacket && rotations != null && rotateMode.getValue() == Rotations.PACKET) {
            ReflectionHelper.setPrivateValue(PlayerMoveC2SPacket.class, event.getPacket(), (float) rotations[1], "pitch", "field_12885");
            ReflectionHelper.setPrivateValue(PlayerMoveC2SPacket.class, event.getPacket(), (float) rotations[0], "yaw", "field_12887");
            MC.player.headYaw = (float) rotations[0];
            MC.player.bodyYaw = (float) rotations[0];
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
            Color fillColor = new Color(
                    colorRed.getValue(),
                    colorGreen.getValue(),
                    colorBlue.getValue(),
                    fillAlpha.getValue()
            );
            Color outlineColor = new Color(
                    colorRed.getValue(),
                    colorGreen.getValue(),
                    colorBlue.getValue(),
                    boxAlpha.getValue()
            );
            RenderUtils.renderBlock(
                    target,
                    fillColor,
                    outlineColor,
                    lineThickness.getValue(),
                    expandRender.getValue()
            );
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
                && !(MC.player.getHealth() - getDamage(crystal.getPos(), MC.player) <= 1 && preventSuicide.getValue()) // check suicide
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
                if(!targetsBlocks.contains(pos) || (double) getDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), targetedPlayer) < minDamage.getValue())
                    continue;

                double score = getScore(pos, targetedPlayer);
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

    // utils
    private double getScore(BlockPos pos, Entity player) {
        double score;
        if(placeMode.getValue() == Mode.DISTANCE) {
            score = Math.abs(player.getY() - pos.up().getY())
                    + Math.abs(player.getX() - pos.getX())
                    + Math.abs(player.getZ() - pos.getZ());

            if(rayTrace(
                    new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5),
                    new Vec3d(player.getPos().x,
                            player.getPos().y,
                            player.getPos().z))

                    == HitResult.Type.BLOCK) score = -1;
        } else {
            score = 200 - getDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), player);
        }

        return score;
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
        return WorldUtils.isValidTarget(entity, Math.max(placeRange.getValue(), breakRange.getValue()) + 8);
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
        if(!oneDotTwelve.getValue()) {
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
        if(lostCrystals.containsKey(entity)) {
            return entity.age >= lostCrystals.get(entity) + lostWindow.getValue();
        }
        return false;
    }

    // damage calculations
    public static float getDamage(Vec3d vec3d, Entity entity) {
        float f2 = 12.0f;
        double d7 = MathHelper.sqrt(entity.squaredDistanceTo(vec3d)) / f2;
        if(d7 <= 1.0D) {
            double d8 = entity.getX() - vec3d.x;
            double d9 = entity.getEyeY() - vec3d.y;
            double d10 = entity.getZ() - vec3d.z;
            double d11 = MathHelper.sqrt(d8 * d8 + d9 * d9 + d10 * d10);
            if(d11 != 0.0D) {
                double d12 = Explosion.getExposure(vec3d, entity);
                double d13 = (1.0D - d7) * d12;
                float damage = transformForDifficulty((float)((int)((d13 * d13 + d13) / 2.0D * 7.0D * (double)f2 + 1.0D)));
                if(entity instanceof PlayerEntity) {
                    damage = DamageUtil.getDamageLeft(damage, (float)((PlayerEntity)entity).getArmor(), (float)((PlayerEntity)entity).getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
                    damage = getReduction(((PlayerEntity)entity), damage, DamageSource.GENERIC);
                }
                return damage;
            }
        }
        return 0.0f;
    }

    private static float transformForDifficulty(float f) {
        if(MC.world.getDifficulty() == Difficulty.PEACEFUL) f = 0.0F;
        if(MC.world.getDifficulty() == Difficulty.EASY) f = Math.min(f / 2.0F + 1.0F, f);
        if(MC.world.getDifficulty() == Difficulty.HARD) f = f * 3.0F / 2.0F;
        return f;
    }

    // get blast reduction off armor and potions
    private static float getReduction(PlayerEntity player, float f, DamageSource damageSource) {
        if (player.hasStatusEffect(StatusEffects.RESISTANCE) && damageSource != DamageSource.OUT_OF_WORLD) {
            int i = (player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
            int j = 25 - i;
            float f1 = f * (float)j;
            float f2 = f;
            f = Math.max(f1 / 25.0F, 0.0F);
            float f3 = f2 - f;
            if (f3 > 0.0F && f3 < 3.4028235E37F) {
                if (player instanceof ServerPlayerEntity) {
                    player.increaseStat(Stats.DAMAGE_RESISTED, Math.round(f3 * 10.0F));
                } else if (damageSource.getAttacker() instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity)damageSource.getAttacker()).increaseStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f3 * 10.0F));
                }
            }
        }

        if (f <= 0.0F) {
            return 0.0F;
        } else {
            int k = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), damageSource);
            if (k > 0) {
                f = DamageUtil.getInflictedDamage(f, (float)k);
            }

            return f;
        }
    }

    // raytracing
    public static HitResult.Type rayTrace(Vec3d start, Vec3d end) {
        double minX = Math.min(start.x, end.x);
        double minY = Math.min(start.y, end.y);
        double minZ = Math.min(start.z, end.z);
        double maxX = Math.max(start.x, end.x);
        double maxY = Math.max(start.y, end.y);
        double maxZ = Math.max(start.z, end.z);

        for(double x = minX; x > maxX; x += 1) {
            for(double y = minY; y > maxY; y += 1) {
                for(double z = minZ; z > maxZ; z += 1) {
                    BlockState blockState = MC.world.getBlockState(new BlockPos(x, y, z));

                    if(blockState.getBlock() == Blocks.OBSIDIAN
                            || blockState.getBlock() == Blocks.BEDROCK
                            || blockState.getBlock() == Blocks.BARRIER)
                        return HitResult.Type.BLOCK;
                }
            }
        }

        return HitResult.Type.MISS;
    }
}
