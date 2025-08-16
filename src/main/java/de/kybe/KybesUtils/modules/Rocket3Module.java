package de.kybe.KybesUtils.modules;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.NumberSetting;

import java.util.ArrayList;
import java.util.List;

public class Rocket3Module extends ToggleableModule {

    public final NumberSetting<Integer> delay = new NumberSetting<>("Delay", "(ticks)", 10, 0, 20);
    int ticks = 0;

    public Rocket3Module() {
        super("Rocket3", "Auto-crafts flight duration 3 rockets", ModuleCategory.CLIENT);

        this.registerSettings(this.delay);
    }

    public int getCount(Item item, CraftingMenu menu, boolean slotCount) {
        int count = 0;
        for (int slot = 0; slot < menu.slots.size(); slot++) {
            if (menu.getSlot(slot).getItem().getItem() != item) continue;
            if (slotCount) count += 1;
            else count += menu.getSlot(slot).getItem().getCount();
        }
        return count;
    }

    public ArrayList<Integer> getSlots(Item item, CraftingMenu menu) {
        ArrayList<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < menu.slots.size(); slot++) {
            if (menu.getSlot(slot).getItem().getItem() != item) continue;
            slots.add(slot);
        }
        return slots;
    }

    @Subscribe
    @SuppressWarnings("unused")
    private void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (!(mc.player.containerMenu instanceof CraftingMenu menu)) return;

        int gunpowderCountSlots = getCount(Items.GUNPOWDER, menu, true);
        int sugarcaneCountSlots = getCount(Items.SUGAR_CANE, menu, true);
        int paperCountSlots = getCount(Items.PAPER, menu, true);
        boolean hasGunpowderInInv = gunpowderCountSlots > 0;
        boolean hasSugarCaneInInv = sugarcaneCountSlots > 0;
        boolean hasPaperInInv = paperCountSlots > 0;

        if (ticks > 0) {
            ticks--;
            return;
        }

        if (!hasGunpowderInInv && !hasSugarCaneInInv && !hasPaperInInv) return;
        if (sugarcaneCountSlots < 3 && sugarcaneCountSlots != 0) return;

        if (menu.getSlot(4).getItem().getItem() != Items.GUNPOWDER)
            mc.gameMode.handleInventoryMouseClick(menu.containerId, 4, 0, ClickType.QUICK_MOVE, mc.player);
        for (int slot = 5; slot < 9; slot++) {
            if (!menu.getSlot(slot).getItem().isEmpty())
                mc.gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.QUICK_MOVE, mc.player);
        }

        ItemStack resultItem = menu.getSlot(0).getItem();
        if (!resultItem.isEmpty()) {
            boolean move = false;
            if (resultItem.getItem() == Items.PAPER) move = true;
            else if (resultItem.getItem() == Items.FIREWORK_ROCKET) {
                Fireworks fireworks = resultItem.getComponents().get(DataComponents.FIREWORKS);
                if (fireworks != null && fireworks.flightDuration() == 3) move = true;
            }
            if (move) {
                mc.gameMode.handleInventoryMouseClick(menu.containerId, 0, 0, ClickType.QUICK_MOVE, mc.player);
                ticks = delay.getValue();
                return;
            }
        }

        if (sugarcaneCountSlots >= 3) {
            List<Integer> slots = getSlots(Items.SUGAR_CANE, menu);
            for (int slot : slots) {
                if (slot < 10) continue;
                if (!menu.getSlot(3).getItem().isEmpty()) break;
                mc.gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.QUICK_MOVE, mc.player);
            }
            ticks = delay.getValue();
            return;
        }

        if (gunpowderCountSlots >= 3 && paperCountSlots >= 1) {
            List<Integer> paperSlots = getSlots(Items.PAPER, menu);
            List<Integer> gunpowderSlots = getSlots(Items.GUNPOWDER, menu);
            for (int slot : paperSlots) {
                if (slot < 10) continue;
                if (!menu.getSlot(1).getItem().isEmpty()) break;
                mc.gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.QUICK_MOVE, mc.player);
            }
            for (int slot : gunpowderSlots) {
                if (slot < 10) continue;
                if (!menu.getSlot(4).getItem().isEmpty()) break;
                mc.gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.QUICK_MOVE, mc.player);
            }
            ticks = delay.getValue();
        }
    }
}
