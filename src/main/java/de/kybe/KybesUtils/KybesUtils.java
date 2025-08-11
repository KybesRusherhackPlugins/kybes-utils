package de.kybe.KybesUtils;

import de.kybe.KybesUtils.commands.CryptoChatCommand;
import de.kybe.KybesUtils.commands.DeathMessageParserCommand;
import de.kybe.KybesUtils.commands.KybeUtilsCommand;
import de.kybe.KybesUtils.modules.*;
import de.kybe.KybesUtils.utils.ChatCrypto;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;


public class KybesUtils extends Plugin {
    private static KybesUtils INSTANCE;

    public static KybesUtils getInstance() {
        return INSTANCE;
    }

    @Override
    public void onLoad() {
        RusherHackAPI.getModuleManager().registerFeature(new MockerModule());
        RusherHackAPI.getModuleManager().registerFeature(new DeathMockerModule());
        RusherHackAPI.getModuleManager().registerFeature(new CodeOverShillerModule());
        RusherHackAPI.getModuleManager().registerFeature(new RandomSentenceModule());
        RusherHackAPI.getModuleManager().registerFeature(new CryptoChatModule());
        RusherHackAPI.getModuleManager().registerFeature(new Deadmau5Module());
        RusherHackAPI.getModuleManager().registerFeature(new BabyElytraModule());
        RusherHackAPI.getModuleManager().registerFeature(new FogParametersModule());
        RusherHackAPI.getModuleManager().registerFeature(new Rocket3Module());
        RusherHackAPI.getModuleManager().registerFeature(new DontLimitMyFuckingFpsModule());

        RusherHackAPI.getCommandManager().registerFeature(new DeathMessageParserCommand());
        RusherHackAPI.getCommandManager().registerFeature(new CryptoChatCommand());
        RusherHackAPI.getCommandManager().registerFeature(new KybeUtilsCommand());

        INSTANCE = this;
    }

    @Override
    public void onUnload() {
    }

}