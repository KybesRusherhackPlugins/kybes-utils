package de.kybe.KybesUtils.windows.vc;

import net.minecraft.network.chat.Component;
import org.rusherhack.client.api.feature.window.Window;
import org.rusherhack.client.api.ui.window.content.WindowContent;
import org.rusherhack.client.api.ui.window.context.ContextAction;
import org.rusherhack.client.api.ui.window.view.WindowView;

import java.util.ArrayList;
import java.util.List;

import static org.rusherhack.client.api.Globals.mc;

public class ColumnComponent extends WindowContent {
    private final List<Component> columns;
    private final List<String> rawColumns;
    private final SharedWidthTracker widthTracker;

    public ColumnComponent(Window window, List<Component> columns, List<String> rawColumns, SharedWidthTracker widthTracker) {
        super(window);
        this.columns = columns;
        this.widthTracker = widthTracker;
        this.rawColumns = rawColumns;

        this.widthTracker.updateWidths(this.columns, getFontRenderer());
    }

    @Override
    public void renderContent(double mouseX, double mouseY, WindowView parent) {
        double xOffset = 0;

        for (int i = 0; i < columns.size(); i++) {
            this.getFontRenderer().drawString(columns.get(i), this.x + xOffset, this.y, -1);
            xOffset += widthTracker.getWidth(i);
        }
    }

    @Override
    public double getWidth() {
        double totalWidth = 0;
        for (int i = 0; i < columns.size(); i++) {
            totalWidth += widthTracker.getWidth(i);
        }
        return totalWidth;
    }

    @Override
    public double getHeight() {
        return this.getFontRenderer().getFontHeight();
    }

    List<ItemReference> copyItems = new ArrayList<>();
    List<ItemReference> targetItems = new ArrayList<>();

    @Override
    public List<ContextAction> getContextMenu() {
        final List<ContextAction> contextMenu = new ArrayList<>();
        for (ItemReference item : copyItems) {
            if (this.rawColumns.get(item.index).isEmpty()) continue;
            contextMenu.add(new ContextAction(item.name, () -> {
                mc.keyboardHandler.setClipboard(this.rawColumns.get(item.index));
            }));
        }
        for (ItemReference item : targetItems) {
            if (this.rawColumns.get(item.index).isEmpty()) continue;
            contextMenu.add(new ContextAction(item.name, () -> {
                UserInfoWindow.INTANCE.setTarget(this.rawColumns.get(item.index), true);
            }));
        }
        return contextMenu;
    }

    public void addCopyItem(int index, String name) {
        copyItems.add(new ItemReference(index, name));
    }

    public void addTargetItem(int index, String name) {
        targetItems.add(new ItemReference(index, name));
    }

    public static class ItemReference {
        int index;
        String name;

        public ItemReference(int index, String name) {
            this.index = index;
            this.name = name;
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    @Override public boolean charTyped(char character) { return false; }
    @Override public boolean keyTyped(int key, int scanCode, int modifiers) { return false; }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double delta) { return false; }
}