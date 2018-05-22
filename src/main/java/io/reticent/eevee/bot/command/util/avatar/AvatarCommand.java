package io.reticent.eevee.bot.command.util.avatar;

import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.parser.arguments.*;
import io.reticent.eevee.session.Session;
import io.reticent.eevee.util.Formatter;
import lombok.NonNull;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class AvatarCommand extends Command {
    @Override
    public String getShortLabel() {
        return "avatar";
    }

    @Override
    public String getLabel() {
        return "Get User Avatar";
    }

    @Override
    public String getDescription() {
        return "Returns a user's avatar. If no user is specified your own avatar is returned.";
    }

    @Override
    public Arguments<AvatarCommandArguments> getArguments() {
        Argument[] argsArray = {
            new LiteralArgument("avatar"),
            new MemberArgument("mentionedMember").withOptions(
                ArgumentOptions.builder()
                               .required(false)
                               .build()
            )
        };

        return new Arguments<>(argsArray, AvatarCommandArguments.class);
    }

    @Override
    public void invoke(@NonNull MessageReceivedEvent event, @NonNull CommandArguments arguments) {
        AvatarCommandArguments args = (AvatarCommandArguments) arguments;
        String userName;
        String avatarUrl;

        if (args.getMentionedMember() == null) {
            userName = Formatter.formatTag(event.getAuthor());
            avatarUrl = event.getAuthor().getEffectiveAvatarUrl();
        } else {
            userName = Formatter.formatTag(args.getMentionedMember().getUser());
            avatarUrl = args.getMentionedMember().getUser().getEffectiveAvatarUrl();
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(String.format("User Avatar for %s", userName));
        embedBuilder.setImage(avatarUrl);
        embedBuilder.setColor(Session.getConfiguration().readInt("defaultEmbedColorDecimal"));

        event.getTextChannel().sendMessage(embedBuilder.build()).queue();
    }
}
