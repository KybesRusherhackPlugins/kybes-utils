package de.kybe.KybesUtils.hud;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.hud.ResizeableHudElement;
import org.rusherhack.client.api.render.RenderContext;

import java.util.ArrayList;

public class SignHoverHUD extends ResizeableHudElement {
    double width = 200;
    double height = 50;

    ArrayList<Component> frontLines = new ArrayList<>();
    ArrayList<Component> backLines = new ArrayList<>();

    public SignHoverHUD() {
        super("SignHover");

        RusherHackAPI.getEventBus().subscribe(this);
    }

    @Override
    public void renderContent(RenderContext context, double mouseX, double mouseY) {
        if (mc.player == null || mc.level == null) return;

        frontLines.clear();
        backLines.clear();

        BlockHitResult res = getHit(mc.player, 10000);
        if (res == null) return;
        BlockState resBlockState = mc.level.getBlockState(res.getBlockPos());
        if (!(resBlockState.getBlock() instanceof SignBlock)) return;

        SignBlockEntity signBlockEntity = (SignBlockEntity) mc.level.getBlockEntity(res.getBlockPos());
        if (signBlockEntity == null) return;

        SignText front = signBlockEntity.getFrontText();
        SignText back = signBlockEntity.getBackText();

        for (int i = 0; i < SignText.LINES; i++) {
            Component frontLine = front.getMessage(i, false);
            Component backLine = back.getMessage(i, false);
            if (!frontLine.getString().isEmpty()) {
                frontLines.add(frontLine);
            } else {
                frontLines.add(Component.empty());
            }

            if (!backLine.getString().isEmpty()) {
                backLines.add(backLine);
            } else {
                backLines.add(Component.empty());
            }
        }

        width = 0;
        double heightOffset = 0;
        for (int i = 0; i < SignText.LINES; i++) {
            if (i != 0) heightOffset += getFontRenderer().getFontHeight();
            getFontRenderer().drawString(frontLines.get(i), 0, heightOffset, -1);
            width = Mth.absMax(width, getFontRenderer().getStringWidth(frontLines.get(i).getString()));
        }

        heightOffset += getFontRenderer().getFontHeight() * 0.5;
        for (int i = 0; i < SignText.LINES; i++) {
            heightOffset += getFontRenderer().getFontHeight();
            getFontRenderer().drawString(backLines.get(i), 0, heightOffset, -1);
            width = Mth.absMax(width, getFontRenderer().getStringWidth(backLines.get(i).getString()));
        }

        height = heightOffset;
    }

    private BlockHitResult getHit(Entity entity, double range) {
        HitResult hitResult = entity.pick(range, 0, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) return null;

        return (BlockHitResult) hitResult;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }
}
