package de.kybe.KybesUtils.commands;

import de.kybe.KybesUtils.modules.CryptoChatModule;
import org.rusherhack.client.api.feature.command.Command;
import org.rusherhack.client.api.feature.command.arg.PlayerReference;
import org.rusherhack.core.command.annotations.CommandExecutor;

public class CryptoChatCommand extends Command {
    public CryptoChatCommand() {
        super("crypto-chat", "Uses the CryptoChat encryption system");
        this.addAliases("cc", "crypto-say");
    }

    @CommandExecutor(subCommand = "say")
    @CommandExecutor.Argument({"string"})
    @SuppressWarnings("unused")
    private void say(String text) {
        if (CryptoChatModule.INSTANCE == null) {
            CryptoChatModule.log("CryptoChat module is not loaded.");
            return;
        }

        CryptoChatModule.INSTANCE.queueMessage(text);
    }

    @CommandExecutor(subCommand = "clear-queue")
    @SuppressWarnings("unused")
    private void clearQueue() {
        if (CryptoChatModule.INSTANCE == null) {
            CryptoChatModule.log("CryptoChat module is not loaded.");
            return;
        }

        CryptoChatModule.INSTANCE.clearSendQueue();
        CryptoChatModule.log("Cleared Send Queue");
    }

    @CommandExecutor(subCommand = "msg")
    @CommandExecutor.Argument({"target", "msg"})
    @SuppressWarnings("unused")
    private void msg(PlayerReference target, String msg) {
        if (CryptoChatModule.INSTANCE == null) {
            CryptoChatModule.log("CryptoChat module is not loaded.");
            return;
        }

        if (msg.isEmpty()) {
            CryptoChatModule.log("Usage: *crypto-chat msg <target> <message>");
            return;
        }

        CryptoChatModule.INSTANCE.queueDirectMessage(target.name(), msg);
        CryptoChatModule.log("Queued encrypted DM to " + target + ": " + msg);
    }

    @CommandExecutor(subCommand = "setkey")
    @CommandExecutor.Argument({"string"})
    @SuppressWarnings("unused")
    private void setKey(String key) {
        if (CryptoChatModule.INSTANCE == null) {
            CryptoChatModule.log("CryptoChat module is not loaded.");
            return;
        }

        CryptoChatModule.INSTANCE.setWriteKey(key);
        CryptoChatModule.log("Write key set.");
    }

    @CommandExecutor(subCommand = "addread")
    @CommandExecutor.Argument({"string"})
    @SuppressWarnings("unused")
    private void addRead(String key) {
        if (CryptoChatModule.INSTANCE == null) {
            CryptoChatModule.log("CryptoChat module is not loaded.");
            return;
        }

        CryptoChatModule.INSTANCE.addReadKey(key);
        CryptoChatModule.log("Read key added.");
    }

    @CommandExecutor(subCommand = "clearkeys")
    @SuppressWarnings("unused")
    private void clearKeys() {
        if (CryptoChatModule.INSTANCE == null) {
            CryptoChatModule.log("CryptoChat module is not loaded.");
            return;
        }

        CryptoChatModule.INSTANCE.clearReadKeys();
        CryptoChatModule.log("All read keys cleared.");
    }

    @CommandExecutor(subCommand = "show-keys")
    @SuppressWarnings("unused")
    private void showKeys() {
        if (CryptoChatModule.INSTANCE == null) {
            CryptoChatModule.log("CryptoChat module is not loaded.");
            return;
        }

        String writeKey = CryptoChatModule.INSTANCE.getWriteKey();
        CryptoChatModule.log("Write Key: " + (writeKey == null || writeKey.isEmpty() ? "<not set>" : writeKey));

        var readKeys = CryptoChatModule.INSTANCE.getReadKeys();
        if (readKeys.isEmpty()) {
            CryptoChatModule.log("No read keys set.");
        } else {
            for (int i = 0; i < readKeys.size(); i++) {
                CryptoChatModule.log("Read Key " + (i + 1) + ": " + readKeys.get(i));
            }
        }
    }
}