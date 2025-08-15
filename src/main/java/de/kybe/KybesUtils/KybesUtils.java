package de.kybe.KybesUtils;

import de.kybe.KybesUtils.VcAPI.VcApi;
import de.kybe.KybesUtils.commands.CryptoChatCommand;
import de.kybe.KybesUtils.commands.DeathMessageParserCommand;
import de.kybe.KybesUtils.commands.KybeUtilsCommand;
import de.kybe.KybesUtils.modules.*;
import de.kybe.KybesUtils.windows.vc.UserInfoWindow;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

import static org.rusherhack.client.api.Globals.mc;


public class KybesUtils extends Plugin {
    private static KybesUtils INSTANCE;
    private VcApi vcApi = new VcApi(getLogger(), mc.getVersionType());
    public VcApi getVcApi() {
        return vcApi;
    }

    public static KybesUtils getInstance() {
        return INSTANCE;
    }

    private static boolean TESTING = false;

    @Override
    public void onLoad() {
        INSTANCE = this;
        if (TESTING) {
            RusherHackAPI.getModuleManager().registerFeature(new BannerClonerModule());
            return;
        }

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
        RusherHackAPI.getModuleManager().registerFeature(new BannerClonerModule());

        RusherHackAPI.getCommandManager().registerFeature(new DeathMessageParserCommand());
        RusherHackAPI.getCommandManager().registerFeature(new CryptoChatCommand());
        RusherHackAPI.getCommandManager().registerFeature(new KybeUtilsCommand());

        RusherHackAPI.getWindowManager().registerFeature(new UserInfoWindow());
    }

    @Override
    public void onUnload() {
    }

}