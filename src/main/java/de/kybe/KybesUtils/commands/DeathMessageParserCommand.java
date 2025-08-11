package de.kybe.KybesUtils.commands;

import de.kybe.KybesUtils.utils.DeathMessageParser;
import org.rusherhack.client.api.feature.command.Command;
import org.rusherhack.core.command.annotations.CommandExecutor;

public class DeathMessageParserCommand extends Command {
    public DeathMessageParserCommand() {
        super("death-message-parser", "uses the death message parser");
    }

    @CommandExecutor
    @CommandExecutor.Argument({"string"})
    @SuppressWarnings("unused")
    private String parse(String parse) {
        return DeathMessageParser.parse(parse).toString();
    }
}
