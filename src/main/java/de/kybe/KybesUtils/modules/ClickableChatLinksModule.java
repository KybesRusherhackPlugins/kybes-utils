package de.kybe.KybesUtils.modules;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.rusherhack.client.api.events.client.chat.EventAddChat;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClickableChatLinksModule extends ToggleableModule {
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)"
    );

    public ClickableChatLinksModule() {
        super("ClickableChatLinks", "Makes chat links clickable", ModuleCategory.CHAT);
    }

    @Subscribe
    public void onChatAdd(EventAddChat event) {
        Component original = event.getChatComponent();
        MutableComponent result = Component.empty();

        for (Component part : original.toFlatList()) {
            String text = part.getString();
            Matcher matcher = URL_PATTERN.matcher(text);
            int lastEnd = 0;

            while (matcher.find()) {
                if (matcher.start() > lastEnd) {
                    result.append(Component.literal(
                            text.substring(lastEnd, matcher.start())
                    ).setStyle(part.getStyle()));
                }

                String url = matcher.group(1);

                Style linkStyle = part.getStyle()
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

                result.append(Component.literal(url).setStyle(linkStyle));
                lastEnd = matcher.end();
            }

            if (lastEnd < text.length()) {
                result.append(Component.literal(
                        text.substring(lastEnd)
                ).setStyle(part.getStyle()));
            }
        }

        event.setChatComponent(result);
    }
}
