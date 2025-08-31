package de.kybe.KybesUtils.modules;

import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.rusherhack.client.api.events.network.EventPacket;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;

public class AntiIllegalDisconnectProxyModule extends ToggleableModule {
    public AntiIllegalDisconnectProxyModule() {
        super("AntiIllegalDisconnectProxy", ModuleCategory.MISC);
    }

    @Subscribe
    public void onDisconnect(EventPacket.Send event) {
        if (!(event.getPacket() instanceof ServerboundChatPacket packet) || isSameAccount()) return;
        if (packet.message().equals("\u0000")) event.setCancelled(true);
    }

    public boolean isSameAccount() {
        if (mc.getConnection() == null) return true;

        String local = mc.getUser().getName();
        String remote = mc.getConnection().getLocalGameProfile().getName();
        return local.equals(remote);
    }
}
