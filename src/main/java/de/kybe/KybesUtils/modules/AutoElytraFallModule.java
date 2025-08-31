package de.kybe.KybesUtils.modules;

import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.rusherhack.client.api.events.player.EventMove;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.utils.InventoryUtils;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.NumberSetting;

public class AutoElytraFallModule extends ToggleableModule {
    private final NumberSetting<Double> fallDistance = new NumberSetting<>("FallDistance", 5.0, 1.0, 30.0);

    private Double initialY = null;

    public AutoElytraFallModule() {
        super("AutoElytraFall", "Automatically deploys your Elytra when falling from a height.", ModuleCategory.MOVEMENT);
        this.registerSettings(fallDistance);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onMove(EventMove event) {
        if (mc.player == null || mc.getConnection() == null) return;

        Vec3 motion = event.getMotion();

        if (initialY == null && motion.y < 0) initialY = mc.player.getY();

        if (mc.player.onGround() || mc.player.isFallFlying() || motion.y > 0) {
            initialY = null;
            return;
        }

        if (initialY != null && initialY - mc.player.getY() > fallDistance.getValue()) {
            ensureElytraEquipped();
            if (mc.player.getInventory().getArmor(2).getItem() != Items.ELYTRA) return;
            mc.player.tryToStartFallFlying();
            mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }
    }

    private void ensureElytraEquipped() {
        if (mc.player == null) return;

        ItemStack chestArmor = mc.player.getInventory().getArmor(2);
        if (chestArmor.getItem() != Items.ELYTRA) {
            int elytraSlot = InventoryUtils.findItem(Items.ELYTRA, true, false);
            if (elytraSlot != -1) swapArmor(elytraSlot);
        }
    }

    private void swapArmor(int fromSlot) {
        if (mc.gameMode == null || mc.player == null) return;

        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 6, fromSlot - 36, ClickType.SWAP, mc.player);
        mc.gameMode.tick();
    }
}