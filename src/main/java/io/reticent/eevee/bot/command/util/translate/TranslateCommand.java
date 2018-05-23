package io.reticent.eevee.bot.command.util.translate;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateException;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.parser.arguments.*;
import io.reticent.eevee.session.Session;
import io.reticent.eevee.util.RateLimiter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.stream.Collectors;

@Log4j2
public class TranslateCommand extends Command {
    @Override
    public String getShortLabel() {
        return "translate";
    }

    @Override
    public String getLabel() {
        return "Translate Text";
    }

    @Override
    public String getDescription() {
        return "Translates a given source text into a given language. Translations are rate-limited to 2 uses every 15 seconds.";
    }

    @Override
    public RateLimiter getRateLimiter() {
        return RateLimiter.builder()
                       .maxHits(2)
                       .duration(15000) // 15 seconds
                       .build();
    }

    @Override
    public Arguments<TranslateCommandArguments> getArguments() {
        Argument[] argsArray = {
            new LiteralArgument("translate"),
            new StringArgument("targetLanguage"),
            new VariadicArgument<StringArgument,String>("sourceText", new StringArgument("foo"))
        };

        return new Arguments<>(argsArray, TranslateCommandArguments.class);
    }

    @Override
    public void invoke(@NonNull MessageReceivedEvent event, @NonNull CommandArguments arguments) {
        TranslateCommandArguments args = (TranslateCommandArguments) arguments;
        Translate translate = TranslateOptions.getDefaultInstance().getService();

        String sourceText = args.getSourceText().stream().collect(Collectors.joining(" "));

        try {
            Translation translation = translate.translate(
                sourceText,
                TranslateOption.targetLanguage(args.getTargetLanguage())
            );

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(
                String.format(
                    "Translation (%s ⟶ %s)",
                    translation.getSourceLanguage().toUpperCase(),
                    args.getTargetLanguage().toUpperCase()
                )
            );
            embedBuilder.addField("Source:", sourceText, false);
            embedBuilder.addField("Translation:", translation.getTranslatedText(), false);
            embedBuilder.setColor(Session.getConfiguration().readInt("defaultEmbedColorDecimal"));

            event.getTextChannel().sendMessage(embedBuilder.build()).queue();
        } catch (TranslateException e) {
            log.error("Failed to translate provided text.", e);

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Oops! An error occurred.");
            embedBuilder.appendDescription("Could not recognize target language code.");
            embedBuilder.setColor(Session.getConfiguration().readInt("errorEmbedColorDecimal"));

            event.getTextChannel().sendMessage(embedBuilder.build()).queue();
        }
    }
}
