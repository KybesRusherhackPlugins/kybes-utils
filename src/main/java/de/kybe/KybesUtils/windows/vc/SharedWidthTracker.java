package de.kybe.KybesUtils.windows.vc;

import net.minecraft.network.chat.Component;
import org.rusherhack.client.api.render.font.IFontRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SharedWidthTracker {
    private final List<Double> widths = new ArrayList<>();
    private final double padding;

    public SharedWidthTracker() {
        this(2);
    }

    public SharedWidthTracker(double padding) {
        this.padding = padding;
    }

    public void updateWidths(List<Component> columns, IFontRenderer font) {
        ensureSize(columns.size());

        for (int i = 0; i < columns.size(); i++) {
            double newWidth = font.getStringWidth(columns.get(i).getString()) + padding;
            if (newWidth > widths.get(i)) {
                widths.set(i, newWidth);
            }
        }
    }

    public double getWidth(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= widths.size()) {
            return 0;
        }
        return widths.get(columnIndex);
    }

    public void reset() {
        Collections.fill(widths, 0.0);
    }

    private void ensureSize(int size) {
        while (widths.size() < size) {
            widths.add(0.0);
        }
    }

    public SharedWidthTracker copy() {
        SharedWidthTracker copy = new SharedWidthTracker(this.padding);
        copy.widths.addAll(this.widths);
        return copy;
    }

    public void setFrom(SharedWidthTracker other) {
        widths.clear();
        widths.addAll(other.widths);
    }
}
