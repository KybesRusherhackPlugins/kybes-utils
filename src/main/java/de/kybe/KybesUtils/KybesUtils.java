package de.kybe.KybesUtils;

import de.kybe.KybesUtils.VcAPI.VcApi;
import de.kybe.KybesUtils.commands.CryptoChatCommand;
import de.kybe.KybesUtils.commands.DeathMessageParserCommand;
import de.kybe.KybesUtils.commands.KybeUtilsCommand;
import de.kybe.KybesUtils.commands.StatDumpCommand;
import de.kybe.KybesUtils.hud.PlayerNameV2HUD;
import de.kybe.KybesUtils.hud.SignHoverHUD;
import de.kybe.KybesUtils.modules.*;
import de.kybe.KybesUtils.windows.chat.ChatWindow;
import de.kybe.KybesUtils.windows.vc.UserInfoWindow;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

import static org.rusherhack.client.api.Globals.mc;


public class KybesUtils extends Plugin {
    private static KybesUtils INSTANCE;
    private final VcApi vcApi = new VcApi(getLogger(), mc.getVersionType());

    public static KybesUtils getInstance() {
        return INSTANCE;
    }

    public VcApi getVcApi() {
        return vcApi;
    }

    public static boolean TESTING = false;

    @Override
    public void onLoad() {
        INSTANCE = this;

        if (TESTING) {
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
        RusherHackAPI.getModuleManager().registerFeature(new ChatCopyModule());
        RusherHackAPI.getModuleManager().registerFeature(new IRLTimeModule());
        RusherHackAPI.getModuleManager().registerFeature(new AmbientLightModule());
        RusherHackAPI.getModuleManager().registerFeature(new FriendOnlyChatModule());
        RusherHackAPI.getModuleManager().registerFeature(new ClickableChatLinksModule());
        RusherHackAPI.getModuleManager().registerFeature(new UnnaturalRotationModule());
        RusherHackAPI.getModuleManager().registerFeature(new BellSpammerModule());
        RusherHackAPI.getModuleManager().registerFeature(new AntiIllegalDisconnectProxyModule());

        RusherHackAPI.getCommandManager().registerFeature(new DeathMessageParserCommand());
        RusherHackAPI.getCommandManager().registerFeature(new CryptoChatCommand());
        RusherHackAPI.getCommandManager().registerFeature(new KybeUtilsCommand());
        RusherHackAPI.getCommandManager().registerFeature(new StatDumpCommand());

        RusherHackAPI.getWindowManager().registerFeature(new UserInfoWindow());
        RusherHackAPI.getWindowManager().registerFeature(new ChatWindow());

        RusherHackAPI.getHudManager().registerFeature(new SignHoverHUD());
        RusherHackAPI.getHudManager().registerFeature(new PlayerNameV2HUD());
    }

    @Override
    public void onUnload() {
    }
}