package de.kybe.KybesUtils.mixins;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.SignedMessageChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessor {
    @Accessor("lastSeenMessages")
    LastSeenMessagesTracker kybe$getLastSeenMessages();

    @Accessor("signedMessageEncoder")
    SignedMessageChain.Encoder kybe$getSignedMessageEncoder();
}
