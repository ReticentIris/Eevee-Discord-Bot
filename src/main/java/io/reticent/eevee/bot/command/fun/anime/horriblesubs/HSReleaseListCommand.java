package io.reticent.eevee.bot.command.fun.anime.horriblesubs;

import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.parser.arguments.Argument;
import io.reticent.eevee.parser.arguments.Arguments;
import io.reticent.eevee.parser.arguments.LiteralArgument;
import io.reticent.eevee.session.Session;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class HSReleaseListCommand extends Command {
    @Override
    public String getShortLabel() {
        return "anime.hs.list";
    }

    @Override
    public String getLabel() {
        return "List HorribleSubs Release Subscriptions";
    }

    @Override
    public String getDescription() {
        return "Show all HorribleSubs release subscriptions for the current channel.";
    }

    @Override
    public Arguments<? extends CommandArguments> getArguments() {
        return new Arguments<>(new Argument[]{
            new LiteralArgument("hs"),
            new LiteralArgument("list")
        }, HSReleaseListCommandArguments.class);
    }

    @Override
    public void invoke(MessageReceivedEvent event, CommandArguments arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("HorribleSubs Release Subscriptions");

        Session.getHsReleaseAnnouncerDataRepository()
               .getAnnouncers()
               .stream()
               .filter(announcer -> announcer.getChannelId().equals(event.getChannel().getId()))
               .forEach(announcer -> {
                   embedBuilder.addField(
                       String.format("%s [%s]", announcer.getAnime(), announcer.getQuality()),
                       announcer.getAnnouncerId(),
                       false
                   );
               });

        embedBuilder.setColor(Session.getConfiguration().readInt("defaultEmbedColorDecimal"));

        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
