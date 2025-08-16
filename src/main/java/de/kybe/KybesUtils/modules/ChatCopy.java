package de.kybe.KybesUtils.modules;

import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;

public class ChatCopy extends ToggleableModule {
    public static ChatCopy INSTANCE;

    public ChatCopy() {
        super("ChatCopy", "Allows to right click a message to copy it", ModuleCategory.CHAT);

        INSTANCE = this;
    }
}
