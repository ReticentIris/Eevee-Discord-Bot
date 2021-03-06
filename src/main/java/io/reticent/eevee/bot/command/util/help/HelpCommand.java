package io.reticent.eevee.bot.command.util.help;

import com.google.common.collect.ImmutableList;
import io.reticent.eevee.bot.CommandMapper;
import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.parser.arguments.*;
import io.reticent.eevee.session.Session;
import io.reticent.eevee.util.Formatter;
import lombok.NonNull;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;

public class HelpCommand extends Command {
    private CommandMapper commandMapper;

    public HelpCommand(CommandMapper commandMapper) {
        this.commandMapper = commandMapper;
    }

    @Override
    public String getShortLabel() {
        return "help";
    }

    @Override
    public String getLabel() {
        return "Show Help Information";
    }

    @Override
    public String getDescription() {
        return "Shows help information for all commands.";
    }

    @Override
    public Arguments<? extends CommandArguments> getArguments() {
        return new Arguments<>(ImmutableList.of(
            new LiteralArgument("help"),
            new StringArgument("commandShortName").withOptions(
                ArgumentOptions.builder()
                               .required(false)
                               .build()
            )
        ), HelpCommandArguments.class);
    }

    @Override
    public void invoke(@NonNull MessageReceivedEvent event, @NonNull CommandArguments arguments) {
        HelpCommandArguments args = (HelpCommandArguments) arguments;

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (args.getCommandShortName() == null) {
            List<Command> commands = commandMapper.getBotCommands();

            embedBuilder.setTitle("Eevee Help Directory");
            embedBuilder.setDescription(String.format(
                "Type one of the commands below to see help information for a command.%n" +
                    "The bot prefix is `%s`.",
                Session.getSession().getConfiguration().readString("botPrefix")
            ));
            embedBuilder.setColor(Session.getSession().getConfiguration().readInt("defaultEmbedColorDecimal"));

            commands.forEach(command -> {
                embedBuilder.addField(command.getLabel(), String.format("help %s", command.getShortLabel()), false);
            });

            event.getChannel().sendMessage(embedBuilder.build()).queue();
        } else {
            Optional<Command> commandOptional = commandMapper.getBotCommands()
                                                             .stream()
                                                             .filter(c -> c.getShortLabel().equals(args.getCommandShortName()))
                                                             .findFirst();

            if (!commandOptional.isPresent()) {
                embedBuilder.setTitle("Oops! An error occurred.");
                embedBuilder.setDescription("Invalid command specified.");
                embedBuilder.setColor(Session.getSession().getConfiguration().readInt("errorEmbedColorDecimal"));

                event.getChannel().sendMessage(embedBuilder.build()).queue();
            } else {
                Command command = commandOptional.get();

                embedBuilder.setTitle(String.format("Eevee Help: %s", command.getLabel()));
                embedBuilder.setDescription(command.getDescription());
                embedBuilder.addField("Usage", String.format("```%s```", command.getArguments().toString()), false);
                embedBuilder.addField("Requires Bot Owner", Formatter.formatBoolean(command.requiresBotOwner()), true);
                embedBuilder.addField("Required Permissions", Formatter.formatPermissions(command.getRequiredPermissions()), true);
                embedBuilder.addField("Rate Limit", Formatter.formatRateLimit(command.getRateLimiter()), true);
                embedBuilder.setColor(Session.getSession().getConfiguration().readInt("defaultEmbedColorDecimal"));

                event.getChannel().sendMessage(embedBuilder.build()).queue();
            }
        }
    }
}
