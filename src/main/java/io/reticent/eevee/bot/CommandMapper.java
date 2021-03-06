package io.reticent.eevee.bot;

import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.parser.arguments.Arguments;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Log4j2
public class CommandMapper {
    @Getter
    private List<Command> botCommands;

    public void add(@NonNull Command command) {
        if (botCommands == null) {
            botCommands = new LinkedList<>();
        }

        try {
            command.bootstrap();
        } catch (RuntimeException e) {
            log.error(String.format("Failed to bootstrap command: %s.", command.getShortLabel()), e);
            log.info(String.format("Skipping command: %s.", command.getShortLabel()));
            return;
        }

        botCommands.add(command);

        log.info(String.format("Registered command: %s.", command.getShortLabel()));
    }

    public Optional<Command> get(@NonNull String messageText, @NonNull MessageReceivedEvent event) {
        for (Command command : botCommands) {
            Arguments<? extends CommandArguments> commandArguments = command.getArguments();

            if (commandArguments.isValid(messageText, event.getMessage())) {
                return Optional.of(command);
            }
        }

        return Optional.empty();
    }
}
