package de.kybe.KybesUtils.modules;

import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;

public class ChatCopyModule extends ToggleableModule {
    public static ChatCopyModule INSTANCE;

    public ChatCopyModule() {
        super("ChatCopy", "Allows to right click a message to copy it", ModuleCategory.CHAT);

        INSTANCE = this;
    }
}
