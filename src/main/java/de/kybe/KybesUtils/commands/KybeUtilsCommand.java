package de.kybe.KybesUtils.commands;

import de.kybe.KybesUtils.mixins.IMixinMinecraft;
import org.rusherhack.client.api.feature.command.Command;
import org.rusherhack.core.command.annotations.CommandExecutor;

public class KybeUtilsCommand extends Command {
    public KybeUtilsCommand() {
        super("kybes-utils", "Kybe's Utils Command");

        this.addAliases("ku");
    }

    @CommandExecutor(subCommand = "demo")
    @CommandExecutor.Argument({"value"})
    @SuppressWarnings("unused")
    private String echo(boolean value) {
        ((IMixinMinecraft) mc).kybe$setDemo(value);
        if (mc.isDemo()) {
            return "Set Minecraft to demo mode.";
        } else {
            return "Set Minecraft to normal mode.";
        }
    }
}
