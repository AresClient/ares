package dev.tigr.ares.fabric.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.fabric.impl.modules.hud.HudElement;
import dev.tigr.ares.fabric.utils.WorldUtils;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * @author UberRipper
 */
@Module.Info(name = "StashFinder", description = "Finds and logs stashed in render distance", category = Category.HUD)
public class StashFinder extends HudElement {

    private static final String CHESTS = "Chests";
    private static final String MINECARTS = "Minecart Chests";
    private static final String SHULKERS = "Shulkers";
    private static final String FILE_PATH = "Ares/stashFinder.csv";
    private static final String FIRST_ROW = "server, x, z, chests, minecarts, shulkers";
    private static final Integer CHUNK_SIZE = 16;

    private final Setting<Boolean> rainbow = register(new BooleanSetting("Rainbow", false));
    private final Setting<Boolean> showInformationPanel
            = register(new BooleanSetting("Show information panel ", true));
    private final Setting<Integer> minimumMinecartNotificationNumber
            = register(new IntegerSetting("Min minecart count ", 5, 1, 30));
    private final Setting<Integer> minimumChestNotificationNumber
            = register(new IntegerSetting("Min chest count ", 5, 1, 30));
    private final Setting<Integer> minimumShulkerNotificationNumber
            = register(new IntegerSetting("Min shulker count ", 1, 1, 30));
    private final Setting<Boolean> logStash
            = register(new BooleanSetting("Log Stash ", true));
    private final Setting<Integer> updateRadius
            = register(new IntegerSetting("Chunk radius ", 3, 1, 10));

    Map<String, Integer> currentStashInfo = new HashMap<>(); // * flashbacks to the first year of uni intensifies *
    String displayText = "";
    final static List<int[]> history = new LinkedList<>();

    public StashFinder() {
        super(300, 300, 0, 18);
        currentStashInfo.put(CHESTS, 0);
        currentStashInfo.put(MINECARTS, 0);
        currentStashInfo.put(SHULKERS, 0);
    }

    int tickCount = 0;

    @Override
    public void onTick() {
        super.onTick();

        if (tickCount < 10) {
            tickCount++;
        } else {
            if (logStash.getValue() && !displayText.equals("")) {
                logStash(currentStashInfo);
            }
            tickCount = 0;
        }
    }

    @Override
    public void draw() {
        displayText = "";
        assert MC.world != null;

        final int chests = (int) WorldUtils.getBlockEntities().stream()
                .filter(tileEntity -> tileEntity instanceof ChestBlockEntity).count();
        if (minimumChestNotificationNumber.getValue() <= chests) {
            currentStashInfo.put(CHESTS, chests);
            displayText = appendString(displayText, chests + " " + CHESTS);
        }

        final int minecartChests = (int) StreamSupport.stream(MC.world.getEntities().spliterator(),false)
                .filter(entity -> entity instanceof ChestMinecartEntity).count();
        if (minimumMinecartNotificationNumber.getValue() <= minecartChests) {
            currentStashInfo.put(MINECARTS, minecartChests);
            displayText = appendString(displayText, minecartChests + " " + MINECARTS);
        }

        final int shulkers = (int) WorldUtils.getBlockEntities().stream()
                .filter(tileEntity -> tileEntity instanceof ShulkerBoxBlockEntity).count();
        if (minimumShulkerNotificationNumber.getValue() <= shulkers) {
            currentStashInfo.put(SHULKERS, shulkers);
            displayText = appendString(displayText, shulkers + " " + SHULKERS);
        }

        if (showInformationPanel.getValue()) {
            drawString(displayText, getX(), getY(), rainbow.getValue() ? IRenderer.rainbow() : Color.WHITE);
            setWidth((int) FONT_RENDERER.getStringWidth(displayText) + 1);
        }
    }

    private String appendString(final String initial, final String toAdd) {
        if (initial.isEmpty()) {
            return toAdd;
        } else {
            return initial + " - " + toAdd;
        }
    }

    private void logStash(final Map<String, Integer> stashInfo) {
        assert MC.player != null;
        int chunkX = getChunkCord((int) Math.round(MC.player.getPos().getX()));
        int chunkZ = getChunkCord((int) Math.round(MC.player.getPos().getZ()));

        if (!(isInHistory(chunkX, chunkZ))) {
            history.add(new int[]{
                    chunkX,
                    chunkZ
            });

            final String toLog =
                    String.format("[stashLogger]: %s, x: %s, z: %s, chests: %s, minecarts: %s, shulkers: %s",
                            MC.getCurrentServerEntry() == null ? "None" : MC.getCurrentServerEntry().name,
                            chunkX,
                            chunkZ,
                            stashInfo.get(CHESTS),
                            stashInfo.get(MINECARTS),
                            stashInfo.get(SHULKERS));
            final String toLogCsv =
                    String.format("%s,%s,%s,%s,%s,%s",
                            MC.getCurrentServerEntry() == null ? "None" : MC.getCurrentServerEntry().name,
                            chunkX,
                            chunkZ,
                            stashInfo.get(CHESTS),
                            stashInfo.get(MINECARTS),
                            stashInfo.get(SHULKERS));

            UTILS.printMessage(TextColor.BLUE + toLog);
            staveCoordinateToFile(toLogCsv);
            // TODO: play an orb sound
        }
    }

    private boolean isInHistory(final Integer x, final Integer z) {
        for (int[] entry : history) {
            if (Math.abs(x - entry[0]) < updateRadius.getValue() * CHUNK_SIZE
                    && Math.abs(z - entry[1]) < updateRadius.getValue() * CHUNK_SIZE) {
                return true;
            }
        }
        return false;
    }

    private int getChunkCord(int location) {
        return CHUNK_SIZE * (location / CHUNK_SIZE);
    }


    private void staveCoordinateToFile(final String row){
        final File f = new File(FILE_PATH);
        if(f.exists()){
            writeRow(row);
        }else{
            writeRow(FIRST_ROW);
            writeRow(row);
        }
    }

    private void writeRow(final String row){
        try (final PrintStream out = new PrintStream(new FileOutputStream(FILE_PATH, true))) {
            out.println(row);
        } catch (final Exception e){
            UTILS.printMessage(TextColor.RED + " File log failed.");
        }
    }
}
