package io.reticent.eevee.bot;

import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.fun.pokemon.BestPokemonCommand;
import io.reticent.eevee.bot.command.util.avatar.AvatarCommand;
import io.reticent.eevee.bot.command.util.help.HelpCommand;
import io.reticent.eevee.bot.command.util.remind.RemindCommand;
import io.reticent.eevee.bot.command.util.stats.StatsCommand;
import io.reticent.eevee.bot.command.util.translate.TranslateCommand;
import io.reticent.eevee.session.Session;
import io.reticent.eevee.util.RateLimiter;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Optional;

@Log4j2
@Builder
public class EeveeBot extends ListenerAdapter {
    private CommandMapper commandMapper;

    public void registerCommands() {
        if (commandMapper != null) {
            log.info("Bot commands already registered. Skipping.");
            return;
        }

        log.info("Registering commands");

        commandMapper = new CommandMapper();

        commandMapper.add(new AvatarCommand());
        commandMapper.add(new TranslateCommand().withRateLimiter(
            RateLimiter.builder()
                       .maxHits(2)
                       .duration(15000) // 15 seconds
                       .build()
        ));
        commandMapper.add(new HelpCommand(commandMapper));
        commandMapper.add(new StatsCommand());
        commandMapper.add(new BestPokemonCommand());
        commandMapper.add(new RemindCommand());

        log.info(String.format("Registered %s commands.", commandMapper.getBotCommands().size()));
    }

    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event) {
        String messageText = event.getMessage().getContentRaw().trim();
        String botPrefix = Session.getConfiguration().readString("botPrefix");

        if (!messageText.startsWith(botPrefix)) {
            return;
        }

        messageText = messageText.substring(botPrefix.length());

        Optional<Command> commandOptional = commandMapper.map(messageText, event);

        if (!commandOptional.isPresent()) {
            return;
        }

        Command command = commandOptional.get();

        if (!isBotOwner(event.getAuthor().getId()) && !canInvoke(command, event)) {
            return;
        }

        try {
            command.invoke(event, command.getArguments().parse(messageText, event.getMessage()));
        } catch (RuntimeException e) {
            log.error(String.format("Failed to execute command due to unhandled runtime exception.\nCommand:%s\n", command.getShortLabel()), e);
            event.getTextChannel().sendMessage(String.format("Failed to execute command:\n%s", e.getMessage())).queue();
        }
    }

    private boolean isBotOwner(@NonNull String id) {
        return id.equals(Session.getConfiguration().readString("botOwnerId"));
    }

    private boolean canInvoke(@NonNull Command command, @NonNull MessageReceivedEvent event) {
        RateLimiter rateLimiter = command.getRateLimiter();

        if (rateLimiter != null && !rateLimiter.tryIncrement()) {
            return false;
        }

        return true;
    }
}
