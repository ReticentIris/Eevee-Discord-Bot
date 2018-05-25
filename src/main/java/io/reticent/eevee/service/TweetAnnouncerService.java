package io.reticent.eevee.service;

import io.reticent.eevee.configuration.GlobalConfiguration;
import io.reticent.eevee.provider.TwitterTweetProvider;
import io.reticent.eevee.repository.TweetAnnouncerDataRepository;
import io.reticent.eevee.repository.model.TweetAnnouncer;
import io.reticent.eevee.session.Session;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import twitter4j.Status;

import java.util.*;
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

        // Cache tweets to avoid having to fetch multiple times if a user
        // is subscribed to in multiple channels.
        Map<String, List<Status>> latestTweetMap = new HashMap<>();

        announcerList.forEach(announcer -> {
            log.debug(String.format("Checking for new Tweet from %s.", announcer.getUser()));
            List<Status> latestTweets;

            if (!latestTweetMap.containsKey(announcer.getUser().toLowerCase())) {
                Optional<List<Status>> latestTweetsOptional = TwitterTweetProvider.getLatestTweets(announcer.getUser(), GlobalConfiguration.TWEET_FETCH_COUNT);

                if (!latestTweetsOptional.isPresent()) {
                    log.debug(String.format("No new tweets from %s found.", announcer.getUser()));
                    return;
                }

                latestTweetMap.put(announcer.getUser().toLowerCase(), latestTweetsOptional.get());
                latestTweets = latestTweetsOptional.get();
            } else {
                latestTweets = latestTweetMap.get(announcer.getUser().toLowerCase());
            }

            log.debug(String.format("Found tweets from %s.", announcer.getUser()));

            // No new tweets. Skip!
            if (latestTweets.get(0).getId() == announcer.getLastTweetId()) {
                return;
            }

            // Need to handle multiple new tweets between check intervals.
            // Reverse the list so that we can send them in order of tweet creation.
            Collections.reverse(latestTweets);

            // Find the last tweet seen.
            int latestSeen = 0;

            while (latestSeen < latestTweets.size() && latestTweets.get(latestSeen).getId() != announcer.getLastTweetId()) {
                latestSeen++;
            }

            latestSeen++;

            List<Status> newTweets = latestTweets.subList(latestSeen, latestTweets.size());

            newTweets.forEach(tweet -> {
                announcer.setLastTweetId(tweet.getId());

                Session.getSession().getTweetAnnouncerDataRepository().update(announcer);

                Session.getSession()
                       .getJdaClient()
                       .getTextChannelById(announcer.getChannelId())
                       .sendMessage(createEmbed(tweet))
                       .queue();

                log.debug(
                    String.format(
                        "Issued announcement for new tweet from %s to channel: %s.'",
                        announcer.getUser(),
                        announcer.getChannelId()
                    )
                );
            });
        });
    }

    private MessageEmbed createEmbed(Status tweet) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Session.getSession().getConfiguration().readInt("defaultEmbedColorDecimal"));
        embedBuilder.setTitle(
            String.format("New Tweet From %s", tweet.getUser().getName()),
            String.format(GlobalConfiguration.TWITTER_PERMALINK, tweet.getUser().getName(), tweet.getId())
        );
        embedBuilder.setDescription(tweet.getText());
        embedBuilder.setFooter(
            String.format("@%s", tweet.getUser().getScreenName()),
            tweet.getUser().getProfileImageURL()
        );

        if (tweet.getMediaEntities().length > 0) {
            String mediaEntityUrl = tweet.getMediaEntities()[0].getMediaURL();
            log.debug(String.format("Found media entity (most likely an image): %s", mediaEntityUrl));
            embedBuilder.setImage(tweet.getMediaEntities()[0].getMediaURL());
        }

        return embedBuilder.build();
    }
}
