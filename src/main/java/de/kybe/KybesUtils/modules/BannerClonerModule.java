package de.kybe.KybesUtils.modules;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.NumberSetting;

import java.util.ArrayList;
import java.util.List;

public class BannerClonerModule extends ToggleableModule {
    public final NumberSetting<Integer> delay = new NumberSetting<>("Delay", "(ticks)", 10, 0, 20);
    int ticks = 0;

    public BannerClonerModule() {
        super("BannerCloner",  "Automatically dupes banners", ModuleCategory.MISC);

        this.registerSettings(delay);
    }

    public List<Integer> getBannerSlotsWithPattern(CraftingMenu menu) {
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < menu.slots.size(); slot++) {
            ItemStack itemStack = menu.getSlot(slot).getItem();
            if (itemStack.isEmpty() || !(itemStack.getItem() instanceof BannerItem)) continue;
            BannerPatternLayers bannerPatternLayers = itemStack.get(DataComponents.BANNER_PATTERNS);
            if (bannerPatternLayers == null) continue;
            if (bannerPatternLayers.layers().isEmpty()) continue;
            slots.add(slot);
        }
        return slots;
    }

    public List<Integer> getMatchingBannerItems(CraftingMenu menu, ItemStack banner) {
        ArrayList<Integer> slots = new ArrayList<>();
        if (banner.isEmpty() || !(banner.getItem() instanceof BannerItem otherBannerItem)) return slots;
        for (int slot = 0; slot < menu.slots.size(); slot++) {
            ItemStack itemStack = menu.getSlot(slot).getItem();
            if (itemStack.isEmpty() || !(itemStack.getItem() instanceof BannerItem bannerItem)) continue;
            {
                DyeColor color = bannerItem.getColor();
                DyeColor otherColor = otherBannerItem.getColor();
                if (color != otherColor) continue;
            }
            BannerPatternLayers bannerPatternLayers = itemStack.get(DataComponents.BANNER_PATTERNS);
            BannerPatternLayers otherBannerPatternLayers = banner.get(DataComponents.BANNER_PATTERNS);
            if (bannerPatternLayers == null || otherBannerPatternLayers == null) continue;
            if (bannerPatternLayers.layers().isEmpty() && otherBannerPatternLayers.layers().isEmpty()) continue;
            if (!bannerPatternLayers.layers().isEmpty() && !otherBannerPatternLayers.layers().isEmpty()) continue;
            slots.add(slot);
        }
        return slots;
    }

    @Subscribe
    @SuppressWarnings("unused")
    private void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (!(mc.player.containerMenu instanceof CraftingMenu menu)) return;

        List<Integer> bannerSlots;
        if (menu.getSlot(1).getItem().isEmpty()) {
            bannerSlots = getBannerSlotsWithPattern(menu);
        } else {
            bannerSlots = List.of(1);
        }

        if (ticks > 0) {
            ticks--;
            return;
        }

        ItemStack resultItem = menu.getSlot(0).getItem();
        if (!resultItem.isEmpty()) {
            mc.gameMode.handleInventoryMouseClick(menu.containerId, 0, 0, ClickType.QUICK_MOVE, mc.player);
            ticks = delay.getValue();
            return;
        }

        if (!menu.getSlot(2).getItem().isEmpty()) return;
        for (int bannerSlot : bannerSlots) {
            List<Integer> matching = getMatchingBannerItems(menu, menu.getSlot(bannerSlot).getItem());
            if (matching.isEmpty()) continue;

            if (bannerSlot > 9) {
                mc.gameMode.handleInventoryMouseClick(menu.containerId, bannerSlot, 0, ClickType.QUICK_MOVE, mc.player);
            }

            for (int slot : matching) {
                if (slot < 10) continue;
                if (!menu.getSlot(0).getItem().isEmpty()) break;
                if (!menu.getSlot(2).getItem().isEmpty()) return;
                mc.gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.QUICK_MOVE, mc.player);
                return;
            }

            ticks = delay.getValue();
        }
    }
}