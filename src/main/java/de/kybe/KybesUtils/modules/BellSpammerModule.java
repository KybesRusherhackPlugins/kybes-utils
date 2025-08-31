package de.kybe.KybesUtils.modules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;

import java.util.ArrayList;
import java.util.List;

public class BellSpammerModule extends ToggleableModule {
    private final NumberSetting<Integer> maxPerTick = new NumberSetting<>("MaxPerTick", 20, 1, 50);
    private final NumberSetting<Integer> multi = new NumberSetting<>("Multi", 1, 1, 5);
    private final BooleanSetting swing = new BooleanSetting("Swing", true);

    private int count = 0;

    public BellSpammerModule() {
        super("BellSpammer", ModuleCategory.MISC);
        this.registerSettings(maxPerTick, multi, swing);
    }

    @Subscribe
    public void onTick(EventUpdate event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        int perTickCount = 0;

        for (BlockPos pos : findNearbyBells()) {
            if (perTickCount >= maxPerTick.getValue()) break;
            if (ringBell(pos)) {
                perTickCount++;
                count++;
            }
        }
    }

    private List<BlockPos> findNearbyBells() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return new ArrayList<>();

        List<BlockPos> bells = new ArrayList<>();
        BlockPos playerPos = mc.player.blockPosition();
        int range = (int) mc.player.blockInteractionRange();
        double maxDistSq = mc.player.blockInteractionRange() * mc.player.blockInteractionRange();

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    if (!(mc.level.getBlockState(pos).getBlock() instanceof BellBlock)) continue;
                    if (mc.player.distanceToSqr(Vec3.atCenterOf(pos)) > maxDistSq) continue;
                    bells.add(pos);
                }
            }
        }
        return bells;
    }

    private boolean ringBell(BlockPos pos) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return false;

        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof BellBlock)) return false;

        Direction hitDir = switch (state.getValue(BellBlock.ATTACHMENT)) {
            case FLOOR -> state.getValue(BellBlock.FACING);
            case SINGLE_WALL, DOUBLE_WALL -> state.getValue(BellBlock.FACING).getClockWise();
            default -> Direction.NORTH;
        };

        BlockHitResult hit = new BlockHitResult(pos.getCenter(), hitDir, pos, true);

        for (int i = 0; i < multi.getValue(); i++) {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
            if (swing.getValue()) mc.player.swing(InteractionHand.MAIN_HAND);
        }
        return true;
    }
}