package io.reticent.eevee.bot.command.fun.anime.horriblesubs;

import io.reticent.eevee.bot.command.CommandArguments;
import lombok.Getter;

public class HSReleaseAnnounceCommandArguments extends CommandArguments {
    @Getter
    private String animeName;
    @Getter
    private String quality;
}
