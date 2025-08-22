package de.kybe.KybesUtils.modules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HayBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.render.EventRender3D;
import org.rusherhack.client.api.events.world.EventChunk;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.render.IRenderer3D;
import org.rusherhack.client.api.setting.ColorSetting;
import org.rusherhack.client.api.utils.EntityUtils;
import org.rusherhack.core.event.stage.Stage;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.utils.ColorUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UnnaturalRotationModule extends ToggleableModule {
    ArrayList<BlockPos> unnaturalBlocks = new ArrayList<>();

    private final ColorSetting color = new ColorSetting("Color", Color.CYAN);

    public UnnaturalRotationModule() {
        super("UnnaturalRotation", "Lets you know if blocks are rotated unusual", ModuleCategory.MISC);

        this.registerSettings(color);
    }

    @Subscribe(stage = Stage.POST)
    public void onChunkLoad(EventChunk.Load event) {
        RusherHackAPI.getChunkProcessor().scanChunk(
                event.getChunkPos(),
                this::isUnnatural,
                this::handleBlocks
        );
    }

    @Subscribe
    public void onChunkUnload(EventChunk.Unload event) {
        ChunkPos unloaded = event.getChunkPos();

        unnaturalBlocks.removeIf(blockPos -> {
            ChunkPos posChunk = new ChunkPos(blockPos);
            return posChunk.equals(unloaded);
        });
    }

    public boolean isUnnatural(BlockState blockState) {
        if (
                blockState.getBlock() instanceof RotatedPillarBlock
                && !(blockState.getBlock() instanceof HayBlock)
                && blockState.getBlock() != Blocks.BONE_BLOCK
                && blockState.getBlock() != Blocks.MUDDY_MANGROVE_ROOTS
                && blockState.getBlock() != Blocks.PURPUR_PILLAR
                && !blockState.getBlock().getDescriptionId().endsWith("_log")
                && !blockState.getBlock().getDescriptionId().endsWith("_wood")
        ) {
            Direction.Axis axis = blockState.getValue(RotatedPillarBlock.AXIS);
            if (axis != Direction.Axis.Y) return true;
        }
        return false;
    }

    public void handleBlocks(HashMap<BlockPos, BlockState> blockPosBlockStateHashMap) {
        for (Map.Entry<BlockPos, BlockState> entry : blockPosBlockStateHashMap.entrySet()) {
            BlockPos pos = entry.getKey();

            if (unnaturalBlocks.contains(pos)) continue;
            unnaturalBlocks.add(pos);
        }
    }

    @Subscribe
    public void onRender3D(EventRender3D event) {
        if (mc.player == null) return;

        final IRenderer3D renderer = event.getRenderer();

        renderer.begin(event.getMatrixStack());

        Vec3 eyePos = EntityUtils.interpolateEntityVec(mc.player, event.getPartialTicks())
                .add(0, mc.player.getEyeHeight(), 0);

        Vec3 look = mc.player.getLookAngle();

        double distance = 0.1;
        Vec3 inFront = eyePos.add(look.scale(distance));

        final int color = ColorUtils.transparency(this.color.getValueRGB(), this.color.getAlpha());

        for (BlockPos blockPos : unnaturalBlocks) {
            renderer.drawBox(blockPos, false, true, color);
            renderer.drawLine(inFront, blockPos.getCenter(), color);
        }

        renderer.end();
    }

    @Override
    public void onEnable() {
        RusherHackAPI.getChunkProcessor().reloadChunks();
    }
}
