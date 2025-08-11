package de.kybe.KybesUtils;

import de.kybe.KybesUtils.commands.CryptoChatCommand;
import de.kybe.KybesUtils.commands.DeathMessageParserCommand;
import de.kybe.KybesUtils.commands.MinecartCommands;
import de.kybe.KybesUtils.modules.*;
import de.kybe.KybesUtils.utils.ChatCrypto;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;


public class KybesUtils extends Plugin {
    private static KybesUtils INSTANCE;

    public static KybesUtils getInstance() {
        return INSTANCE;
    }

    private final ChatCrypto crypto = new ChatCrypto();
    public ChatCrypto getCrypto() {
        return crypto;
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
        RusherHackAPI.getModuleManager().registerFeature(new ChamsV2());
        RusherHackAPI.getModuleManager().registerFeature(new MinecartDupe());

        RusherHackAPI.getCommandManager().registerFeature(new DeathMessageParserCommand());
        RusherHackAPI.getCommandManager().registerFeature(new CryptoChatCommand());
        RusherHackAPI.getCommandManager().registerFeature(new MinecartCommands());

        INSTANCE = this;
    }

    @Override
    public void onUnload() {
    }

}