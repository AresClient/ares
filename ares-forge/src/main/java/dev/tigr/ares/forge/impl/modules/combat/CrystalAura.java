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
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.forge.event.events.client.EntityEvent;
import dev.tigr.ares.forge.event.events.player.DestroyBlockEvent;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.ares.forge.utils.*;
import dev.tigr.ares.forge.utils.Timer;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import dev.tigr.simpleevents.listener.Priority;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "CrystalAura", description = "Automatically breaks and places crystals", category = Category.COMBAT)
public class CrystalAura extends Module {
    public static CrystalAura INSTANCE;

    private final Setting<Target> targetSetting = register(new EnumSetting<>("Target", Target.CLOSEST));
    private final Setting<Mode> placeMode = register(new EnumSetting<>("Place Mode", Mode.DAMAGE));
    private final Setting<Order> order = register(new EnumSetting<>("Order", Order.PLACE_BREAK));
    private final Setting<Boolean> preventSuicide = register(new BooleanSetting("Prevent Suicide", true));
    private final Setting<Boolean> doSwitch = register(new BooleanSetting("Do Switch", true));
    private final Setting<Boolean> noGappleSwitch = register(new BooleanSetting("No Gapple Switch", false)).setVisibility(doSwitch::getValue);
    private final Setting<Integer> placeDelay = register(new IntegerSetting("Place Delay", 2, 0, 15));
    private final Setting<Integer> breakDelay = register(new IntegerSetting("Break Delay", 2, 0, 15));
    private final Setting<Integer> placeOffhandDelay = register(new IntegerSetting("Offh. Place Delay", 2, 0, 15));
    private final Setting<Integer> breakOffhandDelay = register(new IntegerSetting("Offh. Break Delay", 2, 0, 15));
    private final Setting<Float> minDamage = register(new FloatSetting("Minimum Damage", 7.5f, 0, 15));
    private final Setting<Double> placeRange = register(new DoubleSetting("Place Range", 5, 0, 10));
    private final Setting<Double> breakRange = register(new DoubleSetting("Break Range", 5, 0, 10));
    private final Setting<Integer> maxBreakTries = register(new IntegerSetting("Break Attempts", 3, 1, 5));
    private final Setting<Integer> lostWindow = register(new IntegerSetting("Lost Window", 6, 0, 20));
    private final Setting<Boolean> sync = register(new BooleanSetting("Sync", true));
    private final Setting<Boolean> oneDotThirteen = register(new BooleanSetting("1.13+", false));
    private final Setting<Boolean> predictMovement = register(new BooleanSetting("Predict Movement", true));
    private final Setting<Boolean> antiSurround = register(new BooleanSetting("Anti-Surround", true));
    private final Setting<Rotations> rotateMode = register(new EnumSetting<>("Rotations", Rotations.PACKET));
    private final Setting<Boolean> pauseOnEat = register(new BooleanSetting("Pause On Eat", false));
    private final Setting<Boolean> pauseOnPot = register(new BooleanSetting("Pause On Pot", false));
    private final Setting<Boolean> pauseOnXP = register(new BooleanSetting("Pause On XP", false));
    private final Setting<Boolean> pauseOnMine = register(new BooleanSetting("Pause On Mine", false));

    private final Setting<Boolean> showColorSettings = register(new BooleanSetting("Color Settings", false));
    private final Setting<Integer> colorRed = register(new IntegerSetting("Red", 237, 0, 255)).setVisibility(showColorSettings::getValue);
    private final Setting<Integer> colorGreen = register(new IntegerSetting("Green", 0, 0, 255)).setVisibility(showColorSettings::getValue);
    private final Setting<Integer> colorBlue = register(new IntegerSetting("Blue", 0, 0, 255)).setVisibility(showColorSettings::getValue);
    private final Setting<Integer> fillAlpha = register(new IntegerSetting("Fill Alpha", 30, 0, 100)).setVisibility(showColorSettings::getValue);
    private final Setting<Integer> boxAlpha = register(new IntegerSetting("Box Alpha", 69, 0, 100)).setVisibility(showColorSettings::getValue);

    enum Mode { DAMAGE, DISTANCE }
    enum Order { PLACE_BREAK, BREAK_PLACE }
    enum Target { CLOSEST, MOST_DAMAGE }
    enum Rotations { PACKET, REAL, NONE }

    private final Timer renderTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private final Timer cleanupTimer = new Timer();
    private final Timer addingTimer = new Timer();
    private double[] rotations = null;
    public BlockPos target = null;
    private final LinkedHashMap<Vec3d, Long> placedCrystals = new LinkedHashMap<>();
    private final LinkedHashMap<EntityEnderCrystal, AtomicInteger> spawnedCrystals = new LinkedHashMap<>();
    private final LinkedHashMap<EntityEnderCrystal, AtomicInteger> waitingCrystals = new LinkedHashMap<>();
    private final List<EntityEnderCrystal> lostCrystals = new ArrayList<>();
    private EntityPlayer targetPlayer;

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
    public void onTick() {
        run();
    }

    private void run() {
        // pause with options
        if((pauseOnEat.getValue() && MC.player.isHandActive() && (MC.player.getHeldItemMainhand().getItem() instanceof ItemFood || MC.player.getHeldItemOffhand().getItem() instanceof ItemFood)) ||
                (pauseOnPot.getValue() && MC.player.isHandActive() && (MC.player.getHeldItemMainhand().getItem() instanceof ItemPotion || MC.player.getHeldItemOffhand().getItem() instanceof ItemPotion)) ||
                (pauseOnXP.getValue() && MC.player.isHandActive() && (MC.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE || MC.player.getHeldItemOffhand().getItem() == Items.EXPERIENCE_BOTTLE)) ||
                (pauseOnMine.getValue() && MC.playerController.getIsHittingBlock()))
            return;

        // reset rotations
        if(rotations != null) rotations = null;

        // cleanup render
        if(cleanupTimer.passedMillis(3000)) {
            target = null;
            renderTimer.reset();
        }

        // add crystal to lost list if waiting duration has expired since attempting to break so that the CA will place elsewhere
        for(Map.Entry<EntityEnderCrystal, AtomicInteger> entry: waitingCrystals.entrySet()) {
            if(entry.getKey().isDead) {
                waitingCrystals.remove(entry.getKey());
            } else if(entry.getValue().getAndIncrement() >= lostWindow.getValue()) {
                lostCrystals.add(entry.getKey());
                waitingCrystals.remove(entry.getKey());
            }
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
            lostCrystals.removeIf(crystal -> !MC.world.loadedEntityList.contains(crystal));

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
        rotations = WorldUtils.calculateLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, MC.player);

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
                waitingCrystals.put(entry.getKey(), new AtomicInteger(0));
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
                    if(shouldBreakCrystal(offhand) && canBreakCrystal(crystal)) {
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

    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        // rotation spoofing
        if(event.getPacket() instanceof CPacketPlayer && rotations != null && rotateMode.getValue() == Rotations.PACKET) {
            ReflectionHelper.setPrivateValue(CPacketPlayer.class, (CPacketPlayer) event.getPacket(), (float) rotations[1], "pitch", "field_149473_f");
            ReflectionHelper.setPrivateValue(CPacketPlayer.class, (CPacketPlayer) event.getPacket(), (float) rotations[0], "yaw", "field_149476_e");
        }
    });

    // draw target
    @Override
    public void onRender3d() {
        if(target != null) {
            float red = (float) colorRed.getValue() / 255;
            float green = (float) colorGreen.getValue() / 255;
            float blue = (float) colorBlue.getValue() / 255;
            float fAlpha = (float) fillAlpha.getValue() / 100;
            float bAlpha = (float) boxAlpha.getValue() / 100;
            RenderUtils.prepare3d();
            AxisAlignedBB bb = RenderUtils.getBoundingBox(target);
            if(bb != null) {
                RenderGlobal.renderFilledBox(bb, red, green, blue, fAlpha);
                RenderGlobal.drawSelectionBoundingBox(bb, red, green, blue, bAlpha);
            }
            RenderUtils.end3d();
        }

        // add crystals to spawned map to be broken - this is a workaround because putting this in any
        // of the functions that run on tick causes it to block modules like surround and scaffold tower,
        // if there's a better way that doesn't mess up the on tick function, don't hesitate to replace this. -Makrennel
        if(addingTimer.passedTicks(10)) {
            for (Entity entity : MC.world.getLoadedEntityList()) {
                if (entity instanceof EntityEnderCrystal) {
                    if (getDamage(entity.getPositionVector(), targetPlayer) >= (minDamage.getValue() /2) && !waitingCrystals.containsKey(entity) && !spawnedCrystals.containsKey(entity)) {
                        spawnedCrystals.putIfAbsent((EntityEnderCrystal) entity, new AtomicInteger(0));
                        lostCrystals.remove(entity);
                    }
                }
            }
            addingTimer.reset();
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
                for(Entity e : MC.world.getLoadedEntityList()) {
                    if(e instanceof EntityEnderCrystal) {
                        if(e.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= Math.max(breakRange.getValue(), placeRange.getValue()) + 2) {
                            //Remove from all these lists because we can be sure it has broken if the packet was received
                            spawnedCrystals.remove(e);
                            waitingCrystals.remove(e);
                            lostCrystals.remove(e);
                        }
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
                && !(MC.player.getHealth() - getDamage(crystal.getPositionVector(), MC.player) <= 1 && preventSuicide.getValue()); // check suicide
    }

    private void breakCrystal(EntityEnderCrystal crystal, boolean offhand) {
        // find hand
        EnumHand hand = offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;

        // break
        if(sync.getValue()) MC.player.connection.sendPacket(new CPacketUseEntity(crystal, hand));
        MC.playerController.attackEntity(MC.player, crystal);
        MC.player.swingArm(hand);

        //spoof rotations
        rotations = WorldUtils.calculateLookAt(crystal.posX + 0.5, crystal.posY + 0.5, crystal.posZ + 0.5, MC.player);

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
    private double getScore(BlockPos pos, EntityPlayer player) {
        double score;
        if(placeMode.getValue() == Mode.DISTANCE) {
            score = Math.abs(player.posY - pos.up().getY())
                    + Math.abs(player.posX - pos.getX())
                    + Math.abs(player.posZ - pos.getZ());

            if(rayTrace(
                    new Vec3d(pos.add(0.5,
                            1,
                            0.5)),
                    new Vec3d(player.getPositionVector().x,
                            player.getPositionVector().y,
                            player.getPositionVector().z))

                    == RayTraceResult.Type.BLOCK) score = -1;
        } else {
            score = 200 - getDamage(new Vec3d(pos.add(0.5, 1, 0.5)), player);
        }

        return score;
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
        if (!oneDotThirteen.getValue()) {
            BlockPos boost2 = pos.add(0, 2, 0);
            return (MC.world.getBlockState(pos).getBlock() == Blocks.BEDROCK
                    || MC.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN)
                    && MC.world.getBlockState(boost).getBlock() == Blocks.AIR
                    && MC.world.getBlockState(boost2).getBlock() == Blocks.AIR
                    && MC.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).stream().allMatch(entity -> entity instanceof EntityEnderCrystal && !lostCrystals.contains(entity))
                    && MC.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).stream().allMatch(entity -> entity instanceof EntityEnderCrystal && !lostCrystals.contains(entity));
        } else {
            return (MC.world.getBlockState(pos).getBlock() == Blocks.BEDROCK
                    || MC.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN)
                    && MC.world.getBlockState(boost).getBlock() == Blocks.AIR
                    && MC.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).stream().allMatch(entity -> entity instanceof EntityEnderCrystal && !lostCrystals.contains(entity));
        }
    }

    // damage calculations
    private static float getDamage(Vec3d pos, EntityPlayer entity) {
        double blockDensity = entity.world.getBlockDensity(pos, entity.getEntityBoundingBox());
        double power = (1.0D - (entity.getDistance(pos.x, pos.y, pos.z) / 12.0D)) * blockDensity;
        float damage = (float) ((int) ((power * power + power) / 2.0D * 7.0D * 12.0D + 1.0D));

        // world difficulty damage change
        int difficulty = MC.world.getDifficulty().getId();
        damage *= (difficulty == 0 ? 0 : (difficulty == 2 ? 1 : (difficulty == 1 ? 0.5f : 1.5f)));

        return getReduction(entity, damage, new Explosion(MC.world, null, pos.x, pos.y, pos.z, 6F, false, true));
    }

    // get blast reduction off armor and potions
    private static float getReduction(EntityPlayer player, float damage, Explosion explosion) {
        // armor
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) player.getTotalArmorValue(), (float) player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

        // enchantment
        damage *= (1.0F - (float) EnchantmentHelper.getEnchantmentModifierDamage(player.getArmorInventoryList(), DamageSource.causeExplosionDamage(explosion)) / 25.0F);

        // potions
        if(player.isPotionActive(Potion.getPotionById(11))) damage -= damage / 4;

        return damage;
    }

    // raytracing
    private static RayTraceResult.Type rayTrace(Vec3d start, Vec3d end) {
        double minX = Math.min(start.x, end.x);
        double minY = Math.min(start.y, end.y);
        double minZ = Math.min(start.z, end.z);
        double maxX = Math.max(start.x, end.x);
        double maxY = Math.max(start.y, end.y);
        double maxZ = Math.max(start.z, end.z);

        for(double x = minX; x > maxX; x += 1) {
            for(double y = minY; y > maxY; y += 1) {
                for(double z = minZ; z > maxZ; z += 1) {
                    IBlockState blockState = MC.world.getBlockState(new BlockPos(x, y, z));

                    if(blockState.getBlock() == Blocks.OBSIDIAN
                            || blockState.getBlock() == Blocks.BEDROCK
                            || blockState.getBlock() == Blocks.BARRIER)
                        return RayTraceResult.Type.BLOCK;
                }
            }
        }

        return RayTraceResult.Type.MISS;
    }
}
