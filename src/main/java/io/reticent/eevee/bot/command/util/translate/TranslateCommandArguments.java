package io.reticent.eevee.bot.command.util.translate;

import io.reticent.eevee.bot.command.CommandArguments;
import lombok.Getter;

import java.util.List;

public class TranslateCommandArguments extends CommandArguments {
    @Getter
    String targetLanguage;
    @Getter
    private List<String> sourceText;
}
