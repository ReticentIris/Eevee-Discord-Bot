package io.reticent.eevee.bot.command.util.jisho;

import io.reticent.eevee.bot.command.CommandArguments;
import lombok.Getter;

public class JishoCommandArguments extends CommandArguments {
    @Getter
    private String searchQuery;
}
