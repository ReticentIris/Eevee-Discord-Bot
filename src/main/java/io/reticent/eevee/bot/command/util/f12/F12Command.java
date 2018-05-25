package io.reticent.eevee.bot.command.util.f12;

import com.google.common.collect.ImmutableList;
import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.exc.InvalidConfigurationException;
import io.reticent.eevee.parser.arguments.*;
import io.reticent.eevee.session.Session;
import io.reticent.eevee.util.Formatter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class F12Command extends Command {
    private final ScriptEngineManager ENGINE_MANAGER;
    private final Map<String,ScriptEngine> ENGINE_SESSIONS;

    public F12Command() {
        ENGINE_MANAGER = new ScriptEngineManager();
        ENGINE_SESSIONS = new HashMap<>();
    }

    @Override
    public String getShortLabel() {
        return "f12";
    }

    @Override
    public String getLabel() {
        return "~~Chrome F12~~ Interactive ~~Console~~ Shell";
    }

    @Override
    public String getDescription() {
        return "Starts an interactive shell for mucking around with Eevee's internals. Can only be used by the bot owner.";
    }

    @Override
    public boolean requiresBotOwner() {
        return true;
    }

    @Override
    public Arguments<? extends CommandArguments> getArguments() {
        return new Arguments<>(ImmutableList.of(
            new LiteralArgument("f12"),
            new VariadicArgument<StringArgument,String>("commandTokens", new StringArgument("foo"))
        ), F12CommandArguments.class);
    }

    @Override
    public void invoke(@NonNull MessageReceivedEvent event, @NonNull CommandArguments arguments) {
        F12CommandArguments args = (F12CommandArguments) arguments;
        String commandString = args.getCommandTokens().stream().collect(Collectors.joining(" "));
        commandString = commandString.replaceAll("^```|```$", "");

        ScriptEngine jsEngine;

        String userId = Formatter.formatTag(event.getAuthor());

        log.info(String.format("User %s has invoked: %s", userId, commandString));

        if (commandString.equalsIgnoreCase("exit")) {
            if (ENGINE_SESSIONS.containsKey(event.getAuthor().getId())) {
                ENGINE_SESSIONS.remove(event.getAuthor().getId());
            }

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Interactive Shell");
            embedBuilder.setDescription("_Exited interactive shell._");
            embedBuilder.setColor(Session.getSession().getConfiguration().readInt("defaultEmbedColorDecimal"));

            event.getChannel().sendMessage(embedBuilder.build()).queue();

            return;
        }

        if (ENGINE_SESSIONS.containsKey(event.getAuthor().getId())) {
            log.info(String.format("Found existing engine session for user %s.", userId));
            jsEngine = ENGINE_SESSIONS.get(event.getAuthor().getId());
        } else {
            log.info(String.format("Could not find existing engine session for user %s. Creating new engine.", userId));
            jsEngine = ENGINE_MANAGER.getEngineByName("nashorn");
            jsEngine.put("session", Session.getSession());

            ENGINE_SESSIONS.put(event.getAuthor().getId(), jsEngine);
        }

        jsEngine.put("event", event);

        try {
            Object result = jsEngine.eval(commandString);

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Interactive Shell");
            embedBuilder.setDescription(result == null ? "_No Output_" : result.toString());
            embedBuilder.setColor(Session.getSession().getConfiguration().readInt("defaultEmbedColorDecimal"));

            event.getChannel().sendMessage(embedBuilder.build()).queue();
        } catch (ScriptException e) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Interactive Shell");
            embedBuilder.setDescription(e.getMessage());
            embedBuilder.setColor(Session.getSession().getConfiguration().readInt("errorEmbedColorDecimal"));

            event.getChannel().sendMessage(embedBuilder.build()).queue();
        }
    }
}
