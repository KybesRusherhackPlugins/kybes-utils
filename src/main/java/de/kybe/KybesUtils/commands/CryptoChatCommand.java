package de.kybe.KybesUtils.commands;

import de.kybe.KybesUtils.modules.CryptoChatModule;
import org.rusherhack.client.api.feature.command.Command;
import org.rusherhack.core.command.annotations.CommandExecutor;

public class CryptoChatCommand extends Command {
    public CryptoChatCommand() {
        super("crypto-chat", "Uses the CryptoChat encryption system");
        this.addAliases("cc", "crypto-say");
    }

    @CommandExecutor(subCommand = "say")
    @CommandExecutor.Argument({"string"})
    private void say(String text) {
        if (CryptoChatModule.INSTANCE == null) {
            CryptoChatModule.log("CryptoChat module is not loaded.");
            return;
        }

        CryptoChatModule.INSTANCE.queueMessage(text);
    }

    @CommandExecutor(subCommand = "clear-queue")
    private void say() {
        if (CryptoChatModule.INSTANCE == null) {
            CryptoChatModule.log("CryptoChat module is not loaded.");
            return;
        }

        CryptoChatModule.INSTANCE.clearSendQueue();
        CryptoChatModule.log("Cleared Send Queue");
    }

    @CommandExecutor(subCommand = "msg")
    @CommandExecutor.Argument({"string"})
    private void msg(String msg) {
        if (CryptoChatModule.INSTANCE == null) {
            CryptoChatModule.log("CryptoChat module is not loaded.");
            return;
        }

        int spaceIndex = msg.indexOf(' ');
        if (spaceIndex == -1) {
            CryptoChatModule.log("Usage: .crypto-chat msg <target> <message>");
            return;
        }

        String target = msg.substring(0, spaceIndex).trim();
        String text = msg.substring(spaceIndex + 1).trim();

        if (target.isEmpty() || text.isEmpty()) {
            CryptoChatModule.log("Usage: *crypto-chat msg \"<target> <message>\"");
            return;
        }

        CryptoChatModule.INSTANCE.queueDirectMessage(target, text);
        CryptoChatModule.log("Queued encrypted DM to " + target + ": " + text);
    }

    @CommandExecutor(subCommand = "setkey")
    @CommandExecutor.Argument({"string"})
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
    private void addRead(String key) {
        if (CryptoChatModule.INSTANCE == null) {
            CryptoChatModule.log("CryptoChat module is not loaded.");
            return;
        }

        CryptoChatModule.INSTANCE.addReadKey(key);
        CryptoChatModule.log("Read key added.");
    }

    @CommandExecutor(subCommand = "clearkeys")
    private void clearKeys() {
        if (CryptoChatModule.INSTANCE == null) {
            CryptoChatModule.log("CryptoChat module is not loaded.");
            return;
        }

        CryptoChatModule.INSTANCE.clearReadKeys();
        CryptoChatModule.log("All read keys cleared.");
    }

    @CommandExecutor(subCommand = "showkeys")
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