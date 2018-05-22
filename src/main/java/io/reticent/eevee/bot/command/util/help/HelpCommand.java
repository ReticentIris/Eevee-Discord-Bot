package io.reticent.eevee.bot.command.util.help;

import io.reticent.eevee.bot.CommandMapper;
import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.exc.InvalidConfigurationException;
import io.reticent.eevee.parser.arguments.*;
import io.reticent.eevee.session.Session;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Argument[] argsArray = {
            new LiteralArgument("help"),
            new StringArgument("commandShortName").withOptions(
                ArgumentOptions.builder()
                               .required(false)
                               .build()
            )
        };

        return new Arguments<>(argsArray, HelpCommandArguments.class);
    }

    @Override
    public void invoke(MessageReceivedEvent event, CommandArguments arguments) throws InvalidConfigurationException {
        HelpCommandArguments args = (HelpCommandArguments) arguments;

        if (args.getCommandShortName() == null) {
            List<Command> commands = commandMapper.getBotCommands();
            String helpText = commands.stream().map(Object::toString).collect(Collectors.joining("\n"));

            event.getTextChannel().sendMessage(helpText).queue();
        } else {
            Optional<Command> commandOptional = commandMapper.getBotCommands()
                                                             .stream()
                                                             .filter(c -> c.getShortLabel().equals(args.getCommandShortName()))
                                                             .findFirst();

            if (!commandOptional.isPresent()) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Error");
                embedBuilder.appendDescription("Invalid command specified.");
                embedBuilder.setColor(Session.getConfiguration().readInt("errorEmbedColorDecimal"));

                event.getTextChannel().sendMessage(embedBuilder.build()).queue();
            } else {
                event.getTextChannel().sendMessage(commandOptional.get().toString()).queue();
            }
        }
    }
}
