package de.kybe.KybesUtils.hud;

import org.rusherhack.client.api.feature.hud.ResizeableHudElement;
import org.rusherhack.client.api.render.RenderContext;

public class PlayerNameV2HUD extends ResizeableHudElement {
    double width = 0;
    double height = 0;

    public PlayerNameV2HUD() {
        super("PlayerNameV2");
    }

    @Override
    public void renderContent(RenderContext context, double mouseX, double mouseY) {
        if (mc.player == null) return;
        String name = mc.getUser().getName();;

        if (mc.getConnection() != null) {
            String proxy = mc.getConnection().getLocalGameProfile().getName();
            if (!name.equals(proxy)) {
                name = name + " (" + proxy + ")";
            }
        }

        height = getFontRenderer().getFontHeight();
        width = getFontRenderer().getStringWidth(name);

        getFontRenderer().drawString(name, 0, 0, -1);
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
