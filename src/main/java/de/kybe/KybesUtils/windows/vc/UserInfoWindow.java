package de.kybe.KybesUtils.windows.vc;

import de.kybe.KybesUtils.KybesUtils;
import de.kybe.KybesUtils.VcAPI.model.ChatEntry;
import de.kybe.KybesUtils.VcAPI.model.ConnectionEntry;
import de.kybe.KybesUtils.VcAPI.model.DeathEntry;
import de.kybe.KybesUtils.VcAPI.model.KillEntry;
import de.kybe.KybesUtils.utils.ComponentUtils;
import de.kybe.KybesUtils.windows.util.ColumnComponent;
import de.kybe.KybesUtils.windows.util.SharedWidthTracker;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.rusherhack.client.api.feature.window.ResizeableWindow;
import org.rusherhack.client.api.ui.window.content.ComboContent;
import org.rusherhack.client.api.ui.window.content.WindowContent;
import org.rusherhack.client.api.ui.window.content.component.ButtonComponent;
import org.rusherhack.client.api.ui.window.content.component.TextFieldComponent;
import org.rusherhack.client.api.ui.window.context.ContextAction;
import org.rusherhack.client.api.ui.window.view.ScrollableView;
import org.rusherhack.client.api.ui.window.view.TabbedView;
import org.rusherhack.client.api.ui.window.view.WindowView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings({"ExtractMethodRecommender", "DuplicatedCode", "CallToPrintStackTrace"})
public class UserInfoWindow extends ResizeableWindow {
    public static UserInfoWindow INSTANCE;

    private final List<WindowContent> chats = new ArrayList<>();
    private final List<WindowContent> deaths = new ArrayList<>();
    private final List<WindowContent> connections = new ArrayList<>();
    private final List<WindowContent> kills = new ArrayList<>();

    private final ScrollableView chatView = new ScrollableView("Chats", this, chats);
    private final ScrollableView deathView = new ScrollableView("Deaths", this, deaths);
    private final ScrollableView connectionView = new ScrollableView("Connections", this, connections);
    private final ScrollableView killsView = new ScrollableView("Kills", this, kills);

    private final SharedWidthTracker chatWidths = new SharedWidthTracker();
    private final SharedWidthTracker deathWidths = new SharedWidthTracker();
    private final SharedWidthTracker connectionWidths = new SharedWidthTracker();
    private final SharedWidthTracker killsWidths = new SharedWidthTracker();
    private final ConcurrentLinkedQueue<Runnable> uiTasks = new ConcurrentLinkedQueue<>();
    private final List<WindowContent> content = new ArrayList<>();
    private final TabbedView tabbedView = new TabbedView(this, this.content);
    private final List<String> targetHistory = new ArrayList<>();
    private final TextFieldComponent targetName = new TextFieldComponent(this, 125);
    ButtonComponent buttonComponent;
    private LoadState chatState = LoadState.NOT_LOADED;
    private LoadState deathState = LoadState.NOT_LOADED;
    private LoadState connectionState = LoadState.NOT_LOADED;
    private LoadState killState = LoadState.NOT_LOADED;
    private int currentChatsPage = 0;
    private int currentDeathsPage = 0;
    private int currentConnectionsPage = 0;
    private int currentKillsPage = 0;
    private int historyIndex = -1;
    private String target = "";

    public UserInfoWindow() {
        super("2b2t", 250, 400);

        this.setMinHeight(250);

        // Chat View
        chatView.setContextMenu(List.of(
                new ContextAction("Load more", this::loadMoreChats),
                new ContextAction("Reload", this::reloadActiveTab)
        ));
        this.content.add(chatView);

        // Death View
        deathView.setContextMenu(List.of(
                new ContextAction("Load more", this::loadMoreDeaths),
                new ContextAction("Reload", this::reloadActiveTab)
        ));
        this.content.add(deathView);

        // Connection View
        connectionView.setContextMenu(List.of(
                new ContextAction("Load more", this::loadMoreConnections),
                new ContextAction("Reload", this::reloadActiveTab)
        ));
        this.content.add(connectionView);

        // Kills View
        killsView.setContextMenu(List.of(
                new ContextAction("Load more", this::loadMoreKills),
                new ContextAction("Reload", this::reloadActiveTab)
        ));

        this.content.add(killsView);

        final ComboContent targetNameComboContent = new ComboContent(this);
        targetNameComboContent.addContent(targetName);
        this.buttonComponent = new ButtonComponent(
                this,
                "Submit",
                50,
                getFontRenderer().getFontHeight() * 1.25,
                () -> setTarget(targetName.getValue().strip(), true)
        );
        targetNameComboContent.addContent(buttonComponent, ComboContent.AnchorSide.RIGHT);
        this.content.add(targetNameComboContent);

        INSTANCE = this;
    }

    public void setTarget(String target, boolean addToHistory) {
        if (!Objects.equals(targetName.getValue(), target)) targetName.setValue(target);
        this.target = target;

        if (addToHistory) {
            while (targetHistory.size() > historyIndex + 1) {
                targetHistory.removeLast();
            }
            targetHistory.add(target);
            historyIndex = targetHistory.size() - 1;
        }

        chatState = LoadState.NOT_LOADED;
        deathState = LoadState.NOT_LOADED;
        connectionState = LoadState.NOT_LOADED;
        killState = LoadState.NOT_LOADED;

        currentChatsPage = 0;
        currentConnectionsPage = 0;
        currentDeathsPage = 0;
        currentKillsPage = 0;
        chatWidths.reset();
        deathWidths.reset();
        connectionWidths.reset();
        killsWidths.reset();
        chats.clear();
        deaths.clear();
        connections.clear();
        kills.clear();
        reloadActiveTab();
    }

    @Override
    public void tick() {
        WindowView activeTab = tabbedView.getActiveTabView();
        if (activeTab == chatView && chatState == LoadState.NOT_LOADED) {
            loadMoreChats();
        } else if (activeTab == deathView && deathState == LoadState.NOT_LOADED) {
            loadMoreDeaths();
        } else if (activeTab == connectionView && connectionState == LoadState.NOT_LOADED) {
            loadMoreConnections();
        } else if (activeTab == killsView && killState == LoadState.NOT_LOADED) {
            loadMoreKills();
        }

        double connectionViewScrollOffset = connectionView.getScrollbar().getScrollOffset();
        double connectionViewMax = connectionView.getContentHeight() - connectionView.getHeight();
        if (connectionViewScrollOffset >= connectionViewMax && connectionViewMax > 0) {
            loadMoreConnections();
        }

        double deathViewScrollOffset = deathView.getScrollbar().getScrollOffset();
        double deathViewMax = deathView.getContentHeight() - deathView.getHeight();
        if (deathViewScrollOffset >= deathViewMax && deathViewMax > 0) {
            loadMoreDeaths();
        }

        double chatViewScrollOffset = chatView.getScrollbar().getScrollOffset();
        double chatViewMax = chatView.getContentHeight() - chatView.getHeight();
        if (chatViewScrollOffset >= chatViewMax && chatViewMax > 0) {
            loadMoreChats();
        }

        double killsViewScrollOffset = killsView.getScrollbar().getScrollOffset();
        double killsViewMax = killsView.getContentHeight() - killsView.getHeight();
        if (killsViewScrollOffset >= killsViewMax && killsViewMax > 0) {
            loadMoreKills();
        }

        Runnable task;
        while ((task = uiTasks.poll()) != null) {
            task.run();
        }
    }

    private void reloadActiveTab() {
        WindowView activeTab = tabbedView.getActiveTabView();
        if (activeTab == chatView) {
            chats.clear();
            currentChatsPage = 0;
            chatState = LoadState.NOT_LOADED;
            loadMoreChats();
        } else if (activeTab == deathView) {
            deaths.clear();
            currentDeathsPage = 0;
            deathState = LoadState.NOT_LOADED;
            loadMoreDeaths();
        } else if (activeTab == connectionView) {
            connections.clear();
            currentConnectionsPage = 0;
            connectionState = LoadState.NOT_LOADED;
            loadMoreConnections();
        } else if (activeTab == killsView) {
            kills.clear();
            currentKillsPage = 0;
            killState = LoadState.NOT_LOADED;
            loadMoreKills();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_4) { // Back
            if (historyIndex > 0) {
                historyIndex--;
                setTarget(targetHistory.get(historyIndex), false);
            }
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_5) { // Forward
            if (historyIndex < targetHistory.size() - 1) {
                historyIndex++;
                setTarget(targetHistory.get(historyIndex), false);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyTyped(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ENTER) {
            this.buttonComponent.onClick();
        }
        return super.keyTyped(key, scanCode, modifiers);
    }

    private void loadMoreChats() {
        if (chatState == LoadState.LOADING || chatState == LoadState.LOADED_NO_MORE) return;

        chatState = LoadState.LOADING;

        CompletableFuture
                .supplyAsync(() -> KybesUtils.getInstance().getVcApi().getChats(target, currentChatsPage))
                .thenAccept(optionalResponse -> optionalResponse.ifPresentOrElse(chatResponse -> uiTasks.add(() -> {
                    if (chatResponse.getChats().isEmpty()) {
                        chatState = LoadState.LOADED_NO_MORE;
                    } else {
                        for (ChatEntry entry : chatResponse.getChats()) {
                            List<Component> cols = List.of(
                                    ComponentUtils.formatTime(entry.getTime()),
                                    ComponentUtils.formatName(entry.getPlayerName().strip()),
                                    ComponentUtils.formatChat(entry.getChat().strip())
                            );
                            List<String> rawCols = List.of(
                                    ComponentUtils.formatTimeStringRaw(entry.getTime()),
                                    entry.getPlayerName().strip(),
                                    entry.getUuid().toString(),
                                    entry.getChat().strip()
                            );
                            ColumnComponent chatComponent = new ColumnComponent(this, cols, rawCols, chatWidths);
                            chatComponent.addCopyItem(0, "Copy Time");
                            chatComponent.addCopyItem(1, "Copy Player Name");
                            chatComponent.addCopyItem(2, "Copy UUID");
                            chatComponent.addCopyItem(3, "Copy Chat Message");
                            chatComponent.addTargetItem(2, "View UUID");
                            chatComponent.addTargetItem(1, "View Name");
                            chats.add(chatComponent);
                        }
                        currentChatsPage++;
                        chatState = LoadState.LOADED_MORE_AVAILABLE;
                    }
                }), () -> uiTasks.add(() -> chatState = LoadState.LOADED_NO_MORE)))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    uiTasks.add(() -> chatState = LoadState.NOT_LOADED);
                    return null;
                });
    }

    private void loadMoreDeaths() {
        if (target.isEmpty()) return;
        if (deathState == LoadState.LOADING || deathState == LoadState.LOADED_NO_MORE) return;

        deathState = LoadState.LOADING;

        CompletableFuture
                .supplyAsync(() -> KybesUtils.getInstance().getVcApi().getDeaths(target, currentDeathsPage))
                .thenAccept(optionalResponse -> optionalResponse.ifPresentOrElse(deathResponse -> uiTasks.add(() -> {
                    if (deathResponse.getDeaths().isEmpty()) {
                        deathState = LoadState.LOADED_NO_MORE;
                    } else {
                        for (DeathEntry entry : deathResponse.getDeaths()) {
                            List<Component> cols = List.of(
                                    ComponentUtils.formatTime(entry.getTime()),
                                    ComponentUtils.recolorFriends(entry.getDeathMessage().strip())
                            );
                            List<String> rawCols = List.of(
                                    ComponentUtils.formatTimeStringRaw(entry.getTime()),
                                    entry.getDeathMessage().strip(),
                                    entry.getVictimPlayerName() == null ? "" : entry.getVictimPlayerName().strip(),
                                    entry.getVictimPlayerUuid() == null ? "" : entry.getVictimPlayerUuid().strip(),
                                    entry.getKillerPlayerName() == null ? "" : entry.getKillerPlayerName().strip(),
                                    entry.getKillerPlayerUuid() == null ? "" : entry.getKillerPlayerUuid().strip(),
                                    entry.getWeaponName() == null ? "" : entry.getWeaponName().strip(),
                                    entry.getKillerMob() == null ? "" : entry.getKillerMob().strip()
                            );
                            ColumnComponent deathComponent = new ColumnComponent(this, cols, rawCols, deathWidths);
                            deathComponent.addCopyItem(0, "Copy Time");
                            deathComponent.addCopyItem(1, "Copy Death Message");
                            deathComponent.addCopyItem(2, "Copy Victim Name");
                            deathComponent.addCopyItem(3, "Copy Victim UUID");
                            deathComponent.addCopyItem(4, "Copy Killer Name");
                            deathComponent.addCopyItem(5, "Copy Killer UUID");
                            deathComponent.addCopyItem(6, "Copy Weapon Name");
                            deathComponent.addCopyItem(7, "Copy Killer Mob");
                            deathComponent.addTargetItem(2, "View Victim Name");
                            deathComponent.addTargetItem(3, "View Victim UUID");
                            deathComponent.addTargetItem(4, "View Killer Name");
                            deathComponent.addTargetItem(5, "View Killer UUID");
                            deaths.add(deathComponent);
                        }
                        currentDeathsPage++;
                        deathState = LoadState.LOADED_MORE_AVAILABLE;
                    }
                }), () -> uiTasks.add(() -> deathState = LoadState.LOADED_NO_MORE)))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    uiTasks.add(() -> deathState = LoadState.NOT_LOADED);
                    return null;
                });
    }

    private void loadMoreKills() {
        if (target.isEmpty()) return;
        if (killState == LoadState.LOADING || killState == LoadState.LOADED_NO_MORE) return;

        killState = LoadState.LOADING;

        CompletableFuture
                .supplyAsync(() -> KybesUtils.getInstance().getVcApi().getKills(target, currentKillsPage))
                .thenAccept(optionalResponse -> optionalResponse.ifPresentOrElse(killResponse -> uiTasks.add(() -> {
                    if (killResponse.getKills().isEmpty()) {
                        killState = LoadState.LOADED_NO_MORE;
                    } else {
                        for (KillEntry entry : killResponse.getKills()) {
                            List<Component> cols = List.of(
                                    ComponentUtils.formatTime(entry.getTime()),
                                    ComponentUtils.recolorFriends(entry.getDeathMessage().strip())
                            );
                            List<String> rawCols = List.of(
                                    ComponentUtils.formatTimeStringRaw(entry.getTime()),
                                    entry.getDeathMessage().strip(),
                                    entry.getVictimPlayerName() == null ? "" : entry.getVictimPlayerName().strip(),
                                    entry.getVictimPlayerUuid() == null ? "" : entry.getVictimPlayerUuid().strip(),
                                    entry.getKillerPlayerName() == null ? "" : entry.getKillerPlayerName().strip(),
                                    entry.getKillerPlayerUuid() == null ? "" : entry.getKillerPlayerUuid().strip(),
                                    entry.getWeaponName() == null ? "" : entry.getWeaponName().strip(),
                                    entry.getKillerMob() == null ? "" : entry.getKillerMob().strip()
                            );
                            ColumnComponent killComponent = new ColumnComponent(this, cols, rawCols, killsWidths);
                            killComponent.addCopyItem(0, "Copy Time");
                            killComponent.addCopyItem(1, "Copy Death Message");
                            killComponent.addCopyItem(2, "Copy Victim Name");
                            killComponent.addCopyItem(3, "Copy Victim UUID");
                            killComponent.addCopyItem(4, "Copy Killer Name");
                            killComponent.addCopyItem(5, "Copy Killer UUID");
                            killComponent.addCopyItem(6, "Copy Weapon Name");
                            killComponent.addCopyItem(7, "Copy Killer Mob");
                            killComponent.addTargetItem(2, "View Victim Name");
                            killComponent.addTargetItem(3, "View Victim UUID");
                            killComponent.addTargetItem(4, "View Killer Name");
                            killComponent.addTargetItem(5, "View Killer UUID");
                            kills.add(killComponent);
                        }
                        currentKillsPage++;
                        killState = LoadState.LOADED_MORE_AVAILABLE;
                    }
                }), () -> uiTasks.add(() -> killState = LoadState.LOADED_NO_MORE)))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    uiTasks.add(() -> killState = LoadState.NOT_LOADED);
                    return null;
                });
    }

    private void loadMoreConnections() {
        if (target.isEmpty()) return;
        if (connectionState == LoadState.LOADING || connectionState == LoadState.LOADED_NO_MORE) return;

        connectionState = LoadState.LOADING;

        CompletableFuture
                .supplyAsync(() -> KybesUtils.getInstance().getVcApi().getConnections(target, currentConnectionsPage))
                .thenAccept(optionalResponse -> optionalResponse.ifPresentOrElse(connectionResponse -> uiTasks.add(() -> {
                    if (connectionResponse.getConnections().isEmpty()) {
                        connectionState = LoadState.LOADED_NO_MORE;
                    } else {
                        for (ConnectionEntry entry : connectionResponse.getConnections()) {
                            ChatFormatting color = entry.getConnection().toString().equalsIgnoreCase("JOIN")
                                    ? ChatFormatting.GREEN
                                    : ChatFormatting.RED;

                            List<Component> cols = List.of(
                                    ComponentUtils.formatTime(entry.getTime()),
                                    Component.literal(entry.getConnection().toString()).withStyle(color)
                            );
                            List<String> rawCols = List.of(
                                    ComponentUtils.formatTimeStringRaw(entry.getTime()),
                                    entry.getConnection().toString()
                            );
                            ColumnComponent connectionComponent = new ColumnComponent(this, cols, rawCols, connectionWidths);
                            connectionComponent.addCopyItem(0, "Copy Time");
                            connectionComponent.addCopyItem(1, "Copy Connection Type");
                            connections.add(connectionComponent);
                        }
                        currentConnectionsPage++;
                        connectionState = LoadState.LOADED_MORE_AVAILABLE;
                    }
                }), () -> uiTasks.add(() -> connectionState = LoadState.LOADED_NO_MORE)))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    uiTasks.add(() -> connectionState = LoadState.NOT_LOADED);
                    return null;
                });
    }

    @Override
    public WindowView getRootView() {
        return this.tabbedView;
    }

    public enum LoadState {
        NOT_LOADED,
        LOADING,
        LOADED_MORE_AVAILABLE,
        LOADED_NO_MORE
    }
}