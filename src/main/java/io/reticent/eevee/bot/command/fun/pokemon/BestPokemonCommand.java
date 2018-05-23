package io.reticent.eevee.bot.command.fun.pokemon;

import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.parser.arguments.*;
import io.reticent.eevee.session.Session;
import lombok.NonNull;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Random;

public class BestPokemonCommand extends Command {
    @Override
    public String getShortLabel() {
        return "pokemon.best";
    }

    @Override
    public String getLabel() {
        return "What's the best Pokemon?";
    }

    @Override
    public String getDescription() {
        return "Returns a random picture of Eevee!";
    }

    @Override
    public Arguments<? extends CommandArguments> getArguments() {
        Argument[] argsArray = {
            new LiteralArgument("best"),
            new LiteralArgument("pokemon?")
        };

        return new Arguments<>(argsArray, BestPokemonCommandArguments.class);
    }

    @Override
    public void invoke(@NonNull MessageReceivedEvent event, @NonNull CommandArguments arguments) {
        List<String> eevees = Session.getConfiguration().readStringList("eevees");

        Random random = new Random();
        int index = random.nextInt(eevees.size());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("It's Eevee!");
        embedBuilder.setImage(eevees.get(index));
        embedBuilder.setColor(Session.getConfiguration().readInt("defaultEmbedColorDecimal"));

        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
