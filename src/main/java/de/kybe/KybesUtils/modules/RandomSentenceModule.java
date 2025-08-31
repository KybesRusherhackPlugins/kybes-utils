package de.kybe.KybesUtils.modules;

import de.kybe.KybesUtils.utils.RandomSentenceGenerator;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;

import java.util.Random;

public class RandomSentenceModule extends ToggleableModule {
    private final Random random = new Random();
    NumberSetting<Integer> minDelaySeconds = new NumberSetting<>("MinDelay", "(s)", 10, 0, 100);
    NumberSetting<Integer> maxDelaySeconds = new NumberSetting<>("MaxDelay", "(s)", 20, 0, 100);
    BooleanSetting randomWhisper = new BooleanSetting("WhisperRandomly", false);
    private long lastMessageTime = 0;
    private long nextDelayMs = 0;

    public RandomSentenceModule() {
        super("RandomSentence", "Gets random sentences and says them ingame", ModuleCategory.CHAT);
        this.registerSettings(minDelaySeconds, maxDelaySeconds, randomWhisper);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        resetTimer();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onTick(EventUpdate event) {
        if (mc.getConnection() == null) return;
        long now = System.currentTimeMillis();
        if (now - lastMessageTime >= nextDelayMs) {
            sendRandomSentence();
            resetTimer();
        }
    }

    private void resetTimer() {
        int min = minDelaySeconds.getValue();
        int max = maxDelaySeconds.getValue();
        nextDelayMs = (min + random.nextInt(Math.max(1, max - min + 1))) * 1000L;
        lastMessageTime = System.currentTimeMillis();
    }

    private void sendRandomSentence() {
        if (mc.getConnection() == null) return;

        RandomSentenceGenerator.getRandomSentenceAsync()
                .thenAccept(sentence -> {
                    if (sentence == null || sentence.isEmpty() || mc.getConnection() == null) return;

                    if (randomWhisper.getValue()) {
                        mc.getConnection().sendCommand("msg FriendName " + sentence);
                    } else {
                        mc.getConnection().sendChat(sentence);
                    }
                })
                .exceptionally(ex -> {
                    getLogger().error("Failed to fetch random sentence", ex);
                    return null;
                });
    }
}