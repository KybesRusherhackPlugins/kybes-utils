package de.kybe.KybesUtils.commands;

import de.kybe.KybesUtils.mixins.IMixinStatsCounter;
import de.kybe.KybesUtils.utils.TermbinUploader;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.stats.Stat;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import org.rusherhack.client.api.feature.command.Command;
import org.rusherhack.core.command.annotations.CommandExecutor;

public class StatDumpCommand extends Command {
    public StatDumpCommand() {
        super("stats-dump", "Dumps player stats to termbin");
    }

    @CommandExecutor(subCommand = "get-stats")
    @SuppressWarnings("unused")
    private String getStats() {
        if (mc.player == null || mc.getConnection() == null) return "No player or server available.";
        mc.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
        return "Requesting stats from server. Use *stats-dump upload-stats to upload them once received.";
    }

    @CommandExecutor(subCommand = "copy-stats")
    @CommandExecutor.Argument("show-zero")
    @SuppressWarnings("unused")
    private String copyStats(boolean showZero) {
        if (mc.player == null) return "Player not available.";

        IMixinStatsCounter statsCounter = (IMixinStatsCounter) mc.player.getStats();
        if (statsCounter.kybe$getStats().object2IntEntrySet().isEmpty()) {
            return "No stats available. Use *stats-dump get-stats first.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Stats Dump for ").append(mc.player.getGameProfile().getName()).append("\n\n");

        for (Object2IntMap.Entry<Stat<?>> entry : statsCounter.kybe$getStats().object2IntEntrySet()) {
            int value = entry.getIntValue();
            if (!showZero && value == 0) continue;

            Stat<?> stat = entry.getKey();
            sb.append(stat.getName())
                    .append(" = ")
                    .append(stat.format(value))
                    .append("\n");
        }

        mc.keyboardHandler.setClipboard(sb.toString());
        return "Player stats copied to clipboard!";
    }

    @CommandExecutor(subCommand = "upload-stats")
    @CommandExecutor.Argument("show-zero")
    @SuppressWarnings("unused")
    private String uploadStats(boolean showZero) {
        if (mc.player == null || mc.getConnection() == null || mc.getConnection().getServerData() == null)
            return "No player or server data available.";

        IMixinStatsCounter statsCounter = (IMixinStatsCounter) mc.player.getStats();
        StringBuilder sb = new StringBuilder();
        sb.append("Stats Dump for ").append(mc.player.getGameProfile().getName())
                .append(" on ").append(mc.getConnection().getServerData().ip).append("\n\n");

        if (statsCounter.kybe$getStats().object2IntEntrySet().isEmpty()) {
            return "No stats available. Use *stats-dump get-stats to get the stats first.";
        }

        for (Object2IntMap.Entry<Stat<?>> entry : statsCounter.kybe$getStats().object2IntEntrySet()) {
            int value = entry.getIntValue();
            if (!showZero && value == 0) continue;

            Stat<?> stat = entry.getKey();
            sb.append(stat.getName())
                    .append(" = ")
                    .append(stat.format(value))
                    .append("\n");
        }

        TermbinUploader.uploadAsyncChat(sb.toString());
        return "Uploading stats to termbin...";
    }
}