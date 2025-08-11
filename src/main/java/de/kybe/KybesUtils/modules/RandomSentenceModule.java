package de.kybe.KybesUtils.modules;

import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;

import java.util.Random;

import static de.kybe.KybesUtils.utils.RandomSentenceGenerator.getRandomSentence;

public class RandomSentenceModule extends ToggleableModule {
    private final Random random = new Random();
    NumberSetting<Integer> minDelaySeconds = new NumberSetting<>("Min Delay (s)", 10, 0, 100);
    NumberSetting<Integer> maxDelaySeconds = new NumberSetting<>("Max Delay (s)", 20, 0, 100);
    BooleanSetting randomWhisper = new BooleanSetting("Whisper Randomly", false);
    private long lastMessageTime = 0;
    private long nextDelayMs = 0;

    public RandomSentenceModule() {
        super("RandomSentence", ModuleCategory.CHAT);
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
        String sentence = getRandomSentence();

        if (randomWhisper.getValue()) {
            mc.getConnection().sendCommand("msg FriendName " + sentence);
        } else {
            mc.getConnection().sendChat(sentence);
        }
    }
}
