package io.reticent.eevee.bot.command.fun.anime.horriblesubs;

import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.parser.arguments.Argument;
import io.reticent.eevee.parser.arguments.Arguments;
import io.reticent.eevee.parser.arguments.LiteralArgument;
import io.reticent.eevee.parser.arguments.StringArgument;
import io.reticent.eevee.repository.model.HSReleaseAnnouncer;
import io.reticent.eevee.session.Session;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Optional;

public class HSReleaseUnsubscribeCommand extends Command {
    @Override
    public String getShortLabel() {
        return "anime.hs.unsubscribe";
    }

    @Override
    public String getLabel() {
        return "Unsubscribe from HorribleSubs Releases";
    }

    @Override
    public String getDescription() {
        return "Unsubscribe from HorribleSubs releases. Release announcements will no longer be posted in the current channel." +
            "This command requires manage channel permission.";
    }

    @Override
    public Permission[] getRequiredPermissions() {
        return new Permission[] {
            Permission.MANAGE_CHANNEL
        };
    }

    @Override
    public Arguments<? extends CommandArguments> getArguments() {
        return new Arguments<>(new Argument[]{
            new LiteralArgument("hs"),
            new LiteralArgument("unsubscribe"),
            new StringArgument("subscriptionId")
        }, HSReleaseUnsubscribeCommandArguments.class);
    }

    @Override
    public void invoke(MessageReceivedEvent event, CommandArguments arguments) {
        HSReleaseUnsubscribeCommandArguments args = (HSReleaseUnsubscribeCommandArguments) arguments;
        Optional<HSReleaseAnnouncer> announcerOptional = Session.getHsReleaseAnnouncerDataRepository().getAnnouncer(args.getSubscriptionId());

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (!announcerOptional.isPresent()) {
            embedBuilder.setTitle("Oops! An error occurred.");
            embedBuilder.setDescription("The requested subscription ID does not exist.");
            embedBuilder.setColor(Session.getConfiguration().readInt("errorEmbedColorDecimal"));
            event.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }

        HSReleaseAnnouncer announcer = announcerOptional.get();

        Session.getHsReleaseAnnouncerDataRepository().remove(args.getSubscriptionId());

        embedBuilder.setTitle("HorribleSubs Release Subscription Cancelled");
        embedBuilder.setColor(Session.getConfiguration().readInt("successEmbedColorDecimal"));
        embedBuilder.setDescription(
            String.format("Okay. This channel will no longer receive announcements when *%s* is released in %s.", announcer.getAnime(), announcer.getQuality())
        );

        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}