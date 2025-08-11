package de.kybe.KybesUtils.mixins;

import de.kybe.KybesUtils.modules.CryptoChatModule;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "sendChat", at = @At("HEAD"), order = 20000, cancellable = true)
    private void onSendChat(String message, CallbackInfo ci) {
        if (CryptoChatModule.INSTANCE == null || !CryptoChatModule.INSTANCE.isToggled()) return;
        if (CryptoChatModule.INSTANCE.allChatMessages.getValue()) {
            ci.cancel();
            CryptoChatModule.INSTANCE.queueMessage(message);
        } else if (CryptoChatModule.INSTANCE.useChatPrefix.getValue() && message.startsWith(CryptoChatModule.INSTANCE.chatPrefix.getDisplayValue())) {
            ci.cancel();
            CryptoChatModule.INSTANCE.queueMessage(message.substring(1));
        }
    }
}
