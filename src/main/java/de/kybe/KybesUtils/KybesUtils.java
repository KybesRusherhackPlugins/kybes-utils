package de.kybe.KybesUtils;

import de.kybe.KybesUtils.VcAPI.VcApi;
import de.kybe.KybesUtils.commands.*;
import de.kybe.KybesUtils.hud.*;
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

        if (TESTING) return;

        registerModules();
        registerCommands();
        registerWindows();
        registerHUDs();
    }

    private void registerModules() {
        var moduleManager = RusherHackAPI.getModuleManager();
        moduleManager.registerFeature(new MockerModule());
        moduleManager.registerFeature(new DeathMockerModule());
        moduleManager.registerFeature(new CodeOverShillerModule());
        moduleManager.registerFeature(new RandomSentenceModule());
        moduleManager.registerFeature(new CryptoChatModule());
        moduleManager.registerFeature(new Deadmau5Module());
        moduleManager.registerFeature(new BabyElytraModule());
        moduleManager.registerFeature(new FogParametersModule());
        moduleManager.registerFeature(new Rocket3Module());
        moduleManager.registerFeature(new DontLimitMyFuckingFpsModule());
        moduleManager.registerFeature(new BannerClonerModule());
        moduleManager.registerFeature(new ChatCopyModule());
        moduleManager.registerFeature(new IRLTimeModule());
        moduleManager.registerFeature(new AmbientLightModule());
        moduleManager.registerFeature(new FriendOnlyChatModule());
        moduleManager.registerFeature(new ClickableChatLinksModule());
        moduleManager.registerFeature(new UnnaturalRotationModule());
        moduleManager.registerFeature(new BellSpammerModule());
        moduleManager.registerFeature(new AntiIllegalDisconnectProxyModule());
    }

    private void registerCommands() {
        var commandManager = RusherHackAPI.getCommandManager();
        commandManager.registerFeature(new DeathMessageParserCommand());
        commandManager.registerFeature(new CryptoChatCommand());
        commandManager.registerFeature(new KybeUtilsCommand());
        commandManager.registerFeature(new StatDumpCommand());
    }

    private void registerWindows() {
        var windowManager = RusherHackAPI.getWindowManager();
        windowManager.registerFeature(new UserInfoWindow());
        windowManager.registerFeature(new ChatWindow());
    }

    private void registerHUDs() {
        var hudManager = RusherHackAPI.getHudManager();
        hudManager.registerFeature(new SignHoverHUD());
        hudManager.registerFeature(new PlayerNameV2HUD());
    }

    @Override
    public void onUnload() {}
}