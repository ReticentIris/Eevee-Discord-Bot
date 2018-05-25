package io.reticent.eevee.bot.command.util.twitter;

import com.google.common.collect.ImmutableList;
import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.parser.arguments.Argument;
import io.reticent.eevee.parser.arguments.Arguments;
import io.reticent.eevee.parser.arguments.LiteralArgument;
import io.reticent.eevee.parser.arguments.StringArgument;
import io.reticent.eevee.repository.model.TweetAnnouncer;
import io.reticent.eevee.session.Session;
import io.reticent.eevee.util.Formatter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Optional;

@Log4j2
public class TwitterUnsubscribeCommand extends Command {
    @Override
    public String getShortLabel() {
        return "twitter.unsubscribe";
    }

    @Override
    public String getLabel() {
        return "Unsubscribe from Somebody's Tweets";
    }

    @Override
    public String getDescription() {
        return "Unsubscribes from a user's tweets. New tweets will no longer be announced in this channel. This command requires manage channel permission.";
    }

    @Override
    public Permission[] getRequiredPermissions() {
        return new Permission[]{
            Permission.MANAGE_CHANNEL
        };
    }

    @Override
    public Arguments<? extends CommandArguments> getArguments() {
        return new Arguments<>(ImmutableList.of(
            new LiteralArgument("twitter"),
            new LiteralArgument("unsubscribe"),
            new StringArgument("user")
        ), TwitterUnsubscribeCommandArguments.class);
    }

    @Override
    public void invoke(MessageReceivedEvent event, CommandArguments arguments) {
        TwitterUnsubscribeCommandArguments args = (TwitterUnsubscribeCommandArguments) arguments;
        String user = Formatter.formatTwitterUser(args.getUser());

        Optional<TweetAnnouncer> tweetAnnouncerOptional = Session.getSession()
                                                                 .getTweetAnnouncerDataRepository()
                                                                 .getAnnouncer(user, event.getChannel().getId());

        if (!tweetAnnouncerOptional.isPresent()) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Oops! An error occurred.");
            embedBuilder.setColor(Session.getSession().getConfiguration().readInt("errorEmbedColorDecimal"));
            embedBuilder.setDescription(
                String.format("A subscription for the user `%s` does not exist.", user)
            );

            event.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }

        Session.getSession().getTweetAnnouncerDataRepository().remove(tweetAnnouncerOptional.get());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Twitter User Subscription Removed");
        embedBuilder.setColor(Session.getSession().getConfiguration().readInt("successEmbedColorDecimal"));
        embedBuilder.setDescription(
            String.format("Okay. This channel will no longer receive announcements when `%s` tweets something new.", user)
        );

        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
