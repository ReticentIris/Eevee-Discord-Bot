package io.reticent.eevee.bot.command.util.avatar;

import io.reticent.eevee.bot.command.CommandArguments;
import lombok.Getter;
import net.dv8tion.jda.core.entities.Member;

public class AvatarCommandArguments extends CommandArguments {
    @Getter
    private Member mentionedMember;
}
