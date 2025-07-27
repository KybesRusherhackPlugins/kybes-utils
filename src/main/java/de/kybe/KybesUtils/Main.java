package de.kybe.KybesUtils;

import de.kybe.KybesUtils.commands.CryptoChatCommand;
import de.kybe.KybesUtils.commands.DeathMessageParserCommand;
import de.kybe.KybesUtils.modules.*;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;


public class Main extends Plugin {
    @Override
    public void onLoad() {
        RusherHackAPI.getModuleManager().registerFeature(new MockerModule());
        RusherHackAPI.getModuleManager().registerFeature(new DeathMockerModule());
        RusherHackAPI.getModuleManager().registerFeature(new CodeOverShillerModule());
        RusherHackAPI.getModuleManager().registerFeature(new RandomSentenceModule());
        RusherHackAPI.getModuleManager().registerFeature(new CryptoChatModule());

        RusherHackAPI.getCommandManager().registerFeature(new DeathMessageParserCommand());
        RusherHackAPI.getCommandManager().registerFeature(new CryptoChatCommand());
    }

    @Override
    public void onUnload() {
    }

}