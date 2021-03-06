package io.reticent.eevee.bot.command.util.translate;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateException;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.common.collect.ImmutableList;
import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.configuration.GlobalConfiguration;
import io.reticent.eevee.exc.InvalidRuntimeEnvironmentException;
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
    public void bootstrap() {
        if (System.getenv(GlobalConfiguration.GOOGLE_API_CRED_ENV_VAR_NAME) == null) {
            throw new InvalidRuntimeEnvironmentException(
                String.format(
                    "Missing environment variable: %s. This command cannot function without it.",
                    GlobalConfiguration.GOOGLE_API_CRED_ENV_VAR_NAME
                )
            );
        }
    }

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
        return new Arguments<>(ImmutableList.of(
            new LiteralArgument("translate"),
            new StringArgument("targetLanguage"),
            new VariadicArgument<StringArgument,String>("sourceText", new StringArgument("foo"))
        ), TranslateCommandArguments.class);
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
            embedBuilder.setColor(Session.getSession().getConfiguration().readInt("defaultEmbedColorDecimal"));

            event.getChannel().sendMessage(embedBuilder.build()).queue();
        } catch (TranslateException e) {
            log.error("Failed to translate provided text.", e);

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Oops! An error occurred.");
            embedBuilder.appendDescription("Could not recognize target language code.");
            embedBuilder.setColor(Session.getSession().getConfiguration().readInt("errorEmbedColorDecimal"));

            event.getChannel().sendMessage(embedBuilder.build()).queue();
        }
    }
}
