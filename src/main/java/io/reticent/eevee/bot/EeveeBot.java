package io.reticent.eevee.bot;

import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.fun.anime.horriblesubs.HSReleaseListCommand;
import io.reticent.eevee.bot.command.fun.anime.horriblesubs.HSReleaseSubscribeCommand;
import io.reticent.eevee.bot.command.fun.anime.horriblesubs.HSReleaseUnsubscribeCommand;
import io.reticent.eevee.bot.command.fun.pokemon.BestPokemonCommand;
import io.reticent.eevee.bot.command.util.avatar.AvatarCommand;
import io.reticent.eevee.bot.command.util.help.HelpCommand;
import io.reticent.eevee.bot.command.util.jisho.JishoCommand;
import io.reticent.eevee.bot.command.util.remind.RemindCommand;
import io.reticent.eevee.bot.command.util.stats.StatsCommand;
import io.reticent.eevee.bot.command.util.translate.TranslateCommand;
import io.reticent.eevee.bot.command.util.f12.F12Command;
import io.reticent.eevee.bot.command.util.twitter.TwitterSubscribeCommand;
import io.reticent.eevee.bot.command.util.twitter.TwitterUnsubscribeCommand;
import io.reticent.eevee.session.Session;
import io.reticent.eevee.util.RateLimiter;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.Permission;
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
        commandMapper.add(new TranslateCommand());
        commandMapper.add(new HelpCommand(commandMapper));
        commandMapper.add(new StatsCommand());
        commandMapper.add(new BestPokemonCommand());
        commandMapper.add(new RemindCommand());
        commandMapper.add(new F12Command());
        commandMapper.add(new HSReleaseListCommand());
        commandMapper.add(new HSReleaseSubscribeCommand());
        commandMapper.add(new HSReleaseUnsubscribeCommand());
        commandMapper.add(new JishoCommand());
        commandMapper.add(new TwitterSubscribeCommand());
        commandMapper.add(new TwitterUnsubscribeCommand());

        log.info(String.format("Registered %s commands.", commandMapper.getBotCommands().size()));
    }

    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event) {
        String messageText = event.getMessage().getContentRaw().trim();
        String botPrefix = Session.getSession().getConfiguration().readString("botPrefix");

        if (!messageText.startsWith(botPrefix)) {
            return;
        }

        log.debug(String.format("Received potential command: %s.", messageText));

        messageText = messageText.substring(botPrefix.length());

        Optional<Command> commandOptional = commandMapper.get(messageText, event);

        if (!commandOptional.isPresent()) {
            return;
        }

        Command command = commandOptional.get();

        if (!isBotOwner(event.getAuthor().getId())) {
            if (command.requiresBotOwner() || !canInvoke(command, event)) {
                return;
            }
        }

        try {
            command.invoke(event, command.getArguments().parse(messageText, event.getMessage()));
        } catch (RuntimeException e) {
            log.error(String.format("Failed to execute command due to unhandled runtime exception.%nCommand:%s%n", command.getShortLabel()), e);
            // event.getChannel().sendMessage(String.format("Failed to execute command:\n%s", e.getMessage())).queue();
        }
    }

    private boolean isBotOwner(@NonNull String id) {
        return id.equals(Session.getSession().getConfiguration().readString("botOwnerId"));
    }

    private boolean canInvoke(@NonNull Command command, @NonNull MessageReceivedEvent event) {
        RateLimiter rateLimiter = command.getRateLimiter();

        if (rateLimiter != null && !rateLimiter.tryIncrement()) {
            return false;
        }

        Permission[] permissions = (Permission[]) command.getRequiredPermissions().toArray();
    
   
        log.debug(String.format("%s", event.getMember().hasPermission(permissions)));

        return event.getMember().hasPermission(permissions);
    }
}
