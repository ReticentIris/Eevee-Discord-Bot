package io.reticent.eevee.service;

import com.google.common.collect.ImmutableList;
import io.reticent.eevee.configuration.GlobalConfiguration;
import io.reticent.eevee.provider.TwitterTweetProvider;
import io.reticent.eevee.repository.TweetAnnouncerDataRepository;
import io.reticent.eevee.repository.model.TweetAnnouncer;
import io.reticent.eevee.session.Session;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.EmbedBuilder;
import twitter4j.Status;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Log4j2
public class TweetAnnouncerService implements Service {
    private static TweetAnnouncerService tweetAnnouncerService;

    public static TweetAnnouncerService getInstance() {
        if (tweetAnnouncerService == null) {
            tweetAnnouncerService = new TweetAnnouncerService();
        }

        return tweetAnnouncerService;
    }

    @Override
    public void start() {
        Thread thread = new Thread("TweetNotificationThread") {
            public void run() {
                while (true) {
                    checkOnce();

                    try {
                        TimeUnit.MILLISECONDS.sleep(Session.getSession().getConfiguration().readInt("tweetCheckDelay"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        log.error("Failed to sleep after checking for new tweets. Will try again.", e);
                    }
                }
            }
        };

        thread.start();
    }

    private void checkOnce() {
        log.debug("Checking for new tweets.");

        TweetAnnouncerDataRepository dataRepository = Session.getSession().getTweetAnnouncerDataRepository();
        List<TweetAnnouncer> announcerList = dataRepository.getAnnouncers();

        while (announcerList.size() > 0) {
            TweetAnnouncer announcer = announcerList.get(0);
            log.debug(String.format("Checking for new Tweet from %s.", announcer.getUser()));
            Optional<Status> latestTweetOptional = TwitterTweetProvider.getLatestTweet(announcer.getUser());

            if (!latestTweetOptional.isPresent()) {
                log.debug(String.format("No new tweets from %s found.", announcer.getUser()));
                continue;
            }

            log.debug(String.format("Found new tweet from %s.", announcer.getUser()));

            Status latestTweet = latestTweetOptional.get();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Session.getSession().getConfiguration().readInt("defaultEmbedColorDecimal"));
            embedBuilder.setTitle(
                String.format("New Tweet From %s", latestTweet.getUser().getName()),
                String.format(GlobalConfiguration.TWITTER_PERMALINK, latestTweet.getUser().getName(), latestTweet.getId())
            );
            embedBuilder.setDescription(latestTweet.getText());
            embedBuilder.setFooter(
                String.format("@%s", latestTweet.getUser().getScreenName()),
                latestTweet.getUser().getProfileImageURL()
            );

            if (latestTweet.getMediaEntities().length > 0) {
                String mediaEntityUrl = latestTweet.getMediaEntities()[0].getMediaURL();
                log.debug(String.format("Found media entity (most likely an image): %s", mediaEntityUrl));
                embedBuilder.setImage(latestTweet.getMediaEntities()[0].getMediaURL());
            }

            announcerList.stream()
                         .filter(a -> a.getUser().equalsIgnoreCase(announcer.getUser()))
                         .filter(a -> a.getLastTweetId() != latestTweet.getId())
                         .forEach(a -> {
                             a.setLastTweetId(latestTweet.getId());

                             Session.getSession().getTweetAnnouncerDataRepository().update(a);

                             Session.getSession()
                                    .getJdaClient()
                                    .getTextChannelById(a.getChannelId())
                                    .sendMessage(embedBuilder.build())
                                    .queue();

                             log.debug(String.format("Issued announcement for new tweet from %s to channel: %s.'", a.getUser(), a.getChannelId()));
                         });

            // Batch send to all channels subscribed to this same user.
            announcerList = announcerList.stream()
                                         .filter(a -> !a.getUser().equalsIgnoreCase(announcer.getUser()))
                                         .collect(ImmutableList.toImmutableList());
        }
    }
}
