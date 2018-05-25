package io.reticent.eevee.bot.command.util.twitter;

import io.reticent.eevee.bot.command.CommandArguments;
import lombok.Getter;

public class TwitterUnsubscribeCommandArguments extends CommandArguments {
    @Getter
    private String user;
}
