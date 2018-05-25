package io.reticent.eevee.bot.command.util.jisho;

import com.google.common.collect.ImmutableList;
import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.parser.arguments.Argument;
import io.reticent.eevee.parser.arguments.Arguments;
import io.reticent.eevee.parser.arguments.LiteralArgument;
import io.reticent.eevee.parser.arguments.StringArgument;
import io.reticent.eevee.provider.JishoSearchProvider;
import io.reticent.eevee.provider.model.jisho.Japanese;
import io.reticent.eevee.provider.model.JishoSearchResult;
import io.reticent.eevee.provider.model.jisho.ResultData;
import io.reticent.eevee.provider.model.jisho.Sense;
import io.reticent.eevee.session.Session;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JishoCommand extends Command {
    @Override
    public String getShortLabel() {
        return "jisho";
    }

    @Override
    public String getLabel() {
        return "Search Jisho.org";
    }

    @Override
    public String getDescription() {
        return "Search Jisho.org for definitions and translations of various words.";
    }

    @Override
    public Arguments<? extends CommandArguments> getArguments() {
        Argument[] arguments = {
            new LiteralArgument("jisho"),
            new StringArgument("searchQuery")
        };

        return new Arguments<>(ImmutableList.copyOf(arguments), JishoCommandArguments.class);
    }

    @Override
    public void invoke(MessageReceivedEvent event, CommandArguments arguments) {
        JishoCommandArguments args = (JishoCommandArguments) arguments;
        Optional<JishoSearchResult> searchResultOptional = JishoSearchProvider.getSearchResult(args.getSearchQuery());
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (!searchResultOptional.isPresent()) {
            embedBuilder.setTitle("Oops! An error occurred.");
            embedBuilder.setDescription("An unexpected error occurred while searching Jisho. Please try again later.");
            embedBuilder.setColor(Session.getSession().getConfiguration().readInt("errorEmbedColorDecimal"));
            event.getTextChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }

        JishoSearchResult searchResult = searchResultOptional.get();

        embedBuilder.setTitle(String.format("Jisho Search Results: %s", args.getSearchQuery()));
        embedBuilder.setColor(Session.getSession().getConfiguration().readInt("defaultEmbedColorDecimal"));

        if (searchResult.getData().isEmpty()) {
            embedBuilder.setDescription(String.format("No results found for query: %s.", args.getSearchQuery()));
            event.getTextChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }

        ResultData topResult = searchResult.getData().get(0);

        embedBuilder.addField("Word", formatJapanese(topResult.getJapanese()), false);

        int currentIndex = 0;

        for (Sense sense : topResult.getSenses()) {
            String partsOfSpeech = String.join("; ", sense.getPartsOfSpeech());

            if (!partsOfSpeech.isEmpty()) {
                embedBuilder.addField("Parts of Speech", partsOfSpeech, false);
                currentIndex = 1;
            }

            String fieldName = String.format("%s. %s", currentIndex, String.join("; ", sense.getEnglishDefinitions()));

            embedBuilder.addField(fieldName, formatNotes(sense), false);

            currentIndex++;
        }

        event.getTextChannel().sendMessage(embedBuilder.build()).queue();
    }

    private String formatJapanese(List<Japanese> japaneseList) {
        return japaneseList.stream()
                           .map(japanese -> {
                               if (japanese.getWord() == null) return japanese.getReading();
                               return String.format("%s (%s)", japanese.getWord(), japanese.getReading());
                           })
                           .collect(Collectors.joining(", "));
    }

    private String formatNotes(Sense sense) {
        String notes = String.join("; ", sense.getTags());

        if (!sense.getInfo().isEmpty()) {
            notes = String.format("%s%n*%s*", notes, String.join("; ", sense.getInfo()));
        }

        return notes;
    }
}
