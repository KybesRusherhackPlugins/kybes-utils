package de.kybe.KybesUtils.utils;

import de.kybe.KybesUtils.mixins.IMixinClientPacketListener;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.util.Crypt;

import java.time.Instant;

import static org.rusherhack.client.api.Globals.mc;

public class MessageUtils {
    public static void say(String message) {
        if (mc.getConnection() == null) return;
        Instant instant = Instant.now();
        long l = Crypt.SaltSupplier.getLong();
        LastSeenMessagesTracker.Update update = ((IMixinClientPacketListener) mc.getConnection()).kybe$getLastSeenMessages().generateAndApplyUpdate();
        MessageSignature messageSignature = ((IMixinClientPacketListener) mc.getConnection()).kybe$getSignedMessageEncoder().pack(new SignedMessageBody(message, instant, l, update.lastSeen()));
        mc.getConnection().send(new ServerboundChatPacket(message, instant, l, messageSignature, update.update()));
    }
}
