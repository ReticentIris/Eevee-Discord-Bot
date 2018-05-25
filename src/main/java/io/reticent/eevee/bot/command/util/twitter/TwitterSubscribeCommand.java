package io.reticent.eevee.bot.command.util.twitter;

import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.parser.arguments.Argument;
import io.reticent.eevee.parser.arguments.Arguments;
import io.reticent.eevee.parser.arguments.LiteralArgument;
import io.reticent.eevee.parser.arguments.StringArgument;
import io.reticent.eevee.provider.TwitterClientProvider;
import io.reticent.eevee.provider.TwitterTweetProvider;
import io.reticent.eevee.provider.UUIDProvider;
import io.reticent.eevee.repository.model.TweetAnnouncer;
import io.reticent.eevee.service.TweetAnnouncerService;
import io.reticent.eevee.session.Session;
import io.reticent.eevee.util.Formatter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.Optional;

@Log4j2
public class TwitterSubscribeCommand extends Command {
    @Override
    public void bootstrap() {
        TweetAnnouncerService.getInstance().start();
    }


    @Override
    public String getShortLabel() {
        return "twitter.subscribe";
    }

    @Override
    public String getLabel() {
        return "Subscribe to Somebody's Tweets";
    }

    @Override
    public String getDescription() {
        return "Subscribes to a user's tweets. New tweets will be announced in this channel. This command requires manage channel permission.";
    }

    @Override
    public Permission[] getRequiredPermissions() {
        return new Permission[]{
            Permission.MANAGE_CHANNEL
        };
    }

    @Override
    public Arguments<? extends CommandArguments> getArguments() {
        Argument[] args = new Argument[]{
            new LiteralArgument("twitter"),
            new LiteralArgument("subscribe"),
            new StringArgument("user")
        };

        return new Arguments<>(args, TwitterSubscribeCommandArguments.class);
    }

    @Override
    public void invoke(MessageReceivedEvent event, CommandArguments arguments) {
        TwitterSubscribeCommandArguments args = (TwitterSubscribeCommandArguments) arguments;
        String user = Formatter.formatTwitterUser(args.getUser());

        if (
            Session.getSession()
                   .getTweetAnnouncerDataRepository()
                   .getAnnouncer(user, event.getChannel().getId())
                   .isPresent()
            ) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Oops! An error occurred.");
            embedBuilder.setColor(Session.getSession().getConfiguration().readInt("errorEmbedColorDecimal"));
            embedBuilder.setDescription(
                String.format("A subscription for the user `%s` already exists.", user)
            );

            event.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }

        Twitter twitterClient = TwitterClientProvider.getInstance();

        try {
            twitterClient.getUserTimeline(user);
        } catch (TwitterException e) {
            e.printStackTrace();
            log.error(String.format("Failed to fetch twitter timeline for user: %s.", user));

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Oops! An error occurred.");
            embedBuilder.setColor(Session.getSession().getConfiguration().readInt("errorEmbedColorDecimal"));
            embedBuilder.setDescription(
                String.format("Failed to fetch Twitter timeline for user: `%s`.", user)
            );

            event.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }

        Optional<Status> latestTweetOptional = TwitterTweetProvider.getLatestTweet(user);
        long latestTweetId = latestTweetOptional.map(Status::getId).orElse((long) 0);

        TweetAnnouncer announcer = TweetAnnouncer.builder()
                                                 .announcerId(UUIDProvider.getUUID4())
                                                 .channelId(event.getChannel().getId())
                                                 .lastTweetId(latestTweetId)
                                                 .user(user)
                                                 .build();

        Session.getSession().getTweetAnnouncerDataRepository().add(announcer);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Twitter User Subscription Added");
        embedBuilder.setColor(Session.getSession().getConfiguration().readInt("successEmbedColorDecimal"));
        embedBuilder.setDescription(
            String.format("Okay. I will announce when `%s` tweets something new.", user)
        );

        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
