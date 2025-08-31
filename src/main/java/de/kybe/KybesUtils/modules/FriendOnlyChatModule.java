package de.kybe.KybesUtils.modules;

import de.kybe.KybesUtils.utils.ChatMessage;
import de.kybe.KybesUtils.utils.ChatMessageParser;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.client.chat.EventAddChat;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.StringSetting;

public class FriendOnlyChatModule extends ToggleableModule {
    private final StringSetting chatRegex = new StringSetting("ChatRegex", "(Group 1 = name, 2 = msg)",
            "^<([a-zA-Z0-9_]+)>\\s*>*\\s*(.+)$");

    public FriendOnlyChatModule() {
        super("FriendOnlyChat", "Makes it so you only see friends in chat", ModuleCategory.CHAT);

        this.registerSettings(chatRegex);
    }

    @Subscribe
    public void onChatAdd(EventAddChat event) {

        String content = event.getChatComponent().getString();
        ChatMessageParser parser = new ChatMessageParser(chatRegex.getValue());
        ChatMessage chatMessage = parser.parse(content);
        if (chatMessage == null) return;
        boolean isFriend = RusherHackAPI.getRelationManager().isFriend(chatMessage.name());
        if (!isFriend) event.setCancelled(true);
    }
}
