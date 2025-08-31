package de.kybe.KybesUtils.modules;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.client.api.utils.InventoryUtils;
import org.rusherhack.core.event.stage.Stage;
import org.rusherhack.core.event.subscribe.Subscribe;

public class NoFallElyModule extends ToggleableModule {

    private boolean allowLand = false;
    private boolean recoverChest = false;
    private double startedFallingY = 0;
    private int delay = 0;

    private static final int ROTATION_LAND = 89;
    private static final int ROTATION_FLY = -90;
    private static final int DELAY_AFTER_SWAP = 20;

    private ItemStack originalChest = ItemStack.EMPTY;

    public NoFallElyModule() {
        super("NoFallEly", "Stops fall damage by deploying elytra and rocketing", ModuleCategory.MOVEMENT);
    }

    @Subscribe(stage = Stage.ON)
    @SuppressWarnings("unused")
    public void onUpdate(EventUpdate ignored) {
        if (mc.player == null) return;

        if (!isPlayerReady()) return;

        Vec3 motion = mc.player.getDeltaMovement();
        double distanceToGround = mc.player.getY() - getHighestBlockYAtPlayer() - 1;

        if (allowLand) {
            handleLanding();
            return;
        }

        if (recoverChest) {
            handleChestRecovery();
            return;
        }

        if (motion.y >= 0) startedFallingY = 0;
        if (motion.y < 0 && startedFallingY == 0) startedFallingY = mc.player.getY();
        if (startedFallingY - mc.player.getY() < 4) return;

        if (delay > 0) {
            delay--;
            return;
        }

        ensureElytraEquipped(distanceToGround, motion);
        ensureRocketInHotbar(distanceToGround, motion);

        if (distanceToGround <= -motion.y * 4 && !mc.player.onGround()) {
            startFallFlyingAndRocket();
        }
    }

    private boolean isPlayerReady() {
        return mc.player != null && mc.level != null && mc.gameMode != null && mc.getConnection() != null;
    }

    private void handleLanding() {
        if (mc.player == null) return;

        if (delay > 0) {
            delay--;
            return;
        }
        RusherHackAPI.getRotationManager().updateRotation(mc.player.getXRot(), ROTATION_LAND);
        recoverChest = true;
        allowLand = false;
        delay = DELAY_AFTER_SWAP;
    }

    private void handleChestRecovery() {
        if (mc.player == null) return;

        if (delay > 0) {
            RusherHackAPI.getRotationManager().updateRotation(mc.player.getXRot(), ROTATION_LAND);
            delay--;
            return;
        }

        recoverChest = false;
        startedFallingY = mc.player.getY();

        ItemStack chestArmor = mc.player.getInventory().getArmor(2);
        if (chestArmor.getItem() == Items.ELYTRA && !originalChest.isEmpty()) {
            swapArmorBackToChestplate();
            originalChest = ItemStack.EMPTY;
        }
    }

    private void ensureRocketInHotbar(double distanceToGround, Vec3 motion) {
        if (distanceToGround >= -motion.y * 12) return;

        getRocket();
    }


    private void ensureElytraEquipped(double distanceToGround, Vec3 motion) {
        if (mc.player == null) return;

        if (distanceToGround >= -motion.y * 8) return;

        ItemStack chestArmor = mc.player.getInventory().getArmor(2);
        if (chestArmor.getItem() != Items.ELYTRA) {
            originalChest = chestArmor.copy();
            int elytraSlot = InventoryUtils.findItem(Items.ELYTRA, true, false);
            if (elytraSlot != -1) swapArmor(elytraSlot);
        }
    }


    private void startFallFlyingAndRocket() {
        if (mc.getConnection() == null || mc.player == null) return;

        RusherHackAPI.getRotationManager().updateRotation(mc.player.getXRot(), ROTATION_FLY);

        if (!mc.player.onGround() && !mc.player.isFallFlying()) {
            mc.player.tryToStartFallFlying();
            mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }

        ChatUtils.print("Rocketing...");
        useRocket();
    }

    private void swapArmorBackToChestplate() {
        if (originalChest.isEmpty()) return;

        int chestSlot = InventoryUtils.findItem(originalChest.getItem(), true, false);
        if (chestSlot == -1) return;

        ChatUtils.print("Recovering original chestplate on slot " + chestSlot);
        swapArmor(chestSlot);
    }


    private void swapArmor(int fromSlot) {
        if (mc.gameMode == null || mc.player == null) return;

        if (fromSlot == -1) return;
        if (fromSlot - 36 > 9) return;
        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 6, fromSlot - 36, ClickType.SWAP, mc.player);
        mc.gameMode.tick();
    }

    private void getRocket() {
        int rocketSlot = InventoryUtils.findItem(Items.FIREWORK_ROCKET, true, false);
        if (rocketSlot == -1) return;

        if (rocketSlot < 36) {
            InventoryUtils.clickSlot(rocketSlot, false);
            InventoryUtils.clickSlot(36 + 8, false);
            InventoryUtils.clickSlot(rocketSlot, false);
        }
    }

    private void useRocket() {
        if (mc.player == null || mc.gameMode == null) return;

        int rocketSlot = InventoryUtils.findItem(Items.FIREWORK_ROCKET, true, false);
        if (rocketSlot == -1) return;

        if (rocketSlot > 36) {
            mc.player.getInventory().selected = rocketSlot - 36;
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        } else {
            InventoryUtils.clickSlot(rocketSlot, false);
            InventoryUtils.clickSlot(36 + mc.player.getInventory().selected, false);
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }
        allowLand = true;
        delay = 5;
    }

    private int getHighestBlockYAtPlayer() {
        if (mc.player == null || mc.level == null) return 0;

        var bb = mc.player.getBoundingBox();
        int minX = (int) Math.floor(bb.minX);
        int maxX = (int) Math.floor(bb.maxX);
        int minZ = (int) Math.floor(bb.minZ);
        int maxZ = (int) Math.floor(bb.maxZ);

        int highestY = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = (int) Math.floor(bb.minY) - 1; y >= 0; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = mc.level.getBlockState(pos);
                    if (!state.isAir()) {
                        highestY = Math.max(highestY, y);
                        break;
                    }
                }
            }
        }
        return highestY;
    }
}