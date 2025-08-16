package de.kybe.KybesUtils.windows.chat;

import org.lwjgl.glfw.GLFW;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.client.chat.EventAddChat;
import org.rusherhack.client.api.feature.window.ResizeableWindow;
import org.rusherhack.client.api.ui.window.content.ComboContent;
import org.rusherhack.client.api.ui.window.content.WindowContent;
import org.rusherhack.client.api.ui.window.content.component.ButtonComponent;
import org.rusherhack.client.api.ui.window.content.component.TextFieldComponent;
import org.rusherhack.client.api.ui.window.view.RichTextView;
import org.rusherhack.client.api.ui.window.view.TabbedView;
import org.rusherhack.client.api.ui.window.view.WindowView;
import org.rusherhack.core.event.subscribe.Subscribe;

import java.util.ArrayList;

public class ChatWindow extends ResizeableWindow {
    ArrayList<WindowContent> contents = new ArrayList<>();
    TabbedView simpleView = new TabbedView("Chat", this, this.contents);

    RichTextView scrollableView = new RichTextView("Chat View", this);

    TextFieldComponent textFieldComponent = new TextFieldComponent(this, (this.getWidth() / 4) * 3);

    ButtonComponent buttonComponent;

    public ChatWindow() {
        super("Chat", 300, 200);

        this.contents.add(scrollableView);

        ComboContent comboContent = new ComboContent(this);
        comboContent.addContent(textFieldComponent);
        buttonComponent = new ButtonComponent(this, "Send", () -> {
            String message = textFieldComponent.getValue();
            if (mc.getConnection() == null) return;
            if (!message.isEmpty()) {
                if (message.startsWith("/")) {
                    mc.getConnection().sendCommand(message.trim().substring(1));
                } else {
                    mc.getConnection().sendChat(message);
                }
                textFieldComponent.setValue("");
            }
        });

        comboContent.addContent(this.buttonComponent);

        this.contents.add(comboContent);

        RusherHackAPI.getEventBus().subscribe(this);
    }

    @Subscribe(priority = -1)
    @SuppressWarnings("unused")
    public void onChat(EventAddChat event) {
        scrollableView.add(event.getChatComponent(), -1);
    }

    @Override
    public boolean keyTyped(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ENTER) {
            this.buttonComponent.onClick();
        }
        return super.keyTyped(key, scanCode, modifiers);
    }

    @Override
    public WindowView getRootView() {
        return simpleView;
    }
}
