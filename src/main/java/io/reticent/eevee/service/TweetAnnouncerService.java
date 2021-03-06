package io.reticent.eevee.service;

import io.reticent.eevee.configuration.GlobalConfiguration;
import io.reticent.eevee.provider.TwitterTweetProvider;
import io.reticent.eevee.repository.TweetAnnouncerDataRepository;
import io.reticent.eevee.repository.model.TweetAnnouncer;
import io.reticent.eevee.session.Session;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
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
            log.debug(String.format("Checking for new tweet from %s.", announcer.getUser()));
            List<Status> latestTweets;

            if (!latestTweetMap.containsKey(announcer.getUser().toLowerCase())) {
                /*
                 This should be replaced with a more permanent solution where we build up a list of unseen tweets.

                 1. If the latest tweet seen is not in the fetch count: keep fetching until it is in the list
                 2. Save the new list to be used by other announcers for this single check

                 Questions:
                 How do we handle the case where a deleted tweet is the latest seen tweet? We would fetch all the
                 tweets and not know where to start announcing.

                 Look into tweet stream API to stream new tweets maybe? How would we handle downtime in this case?

                 Maybe we should switch to timestamps for indexing?
                  */

                Optional<List<Status>> latestTweetsOptional = TwitterTweetProvider.getLatestTweets(announcer.getUser(), GlobalConfiguration.TWEET_FETCH_COUNT);

                if (!latestTweetsOptional.isPresent()) {
                    log.debug(String.format("No new tweets from %s found.", announcer.getUser()));
                    return;
                }

                latestTweetMap.put(announcer.getUser().toLowerCase(), latestTweetsOptional.get());
                latestTweets = latestTweetsOptional.get();
                // Reverse the list so that we can send them in order of tweet creation.
                Collections.reverse(latestTweets);
            } else {
                latestTweets = latestTweetMap.get(announcer.getUser().toLowerCase());
            }

            log.debug(String.format("Found tweets from %s.", announcer.getUser()));

            log.debug(
                String.format(
                    "Latest tweet found: %s. Announcer last tweet seen: %s.",
                    latestTweets.get(0).getId(),
                    announcer.getLastTweetId()
                )
            );

            // No new tweets. Skip!
            if (latestTweets.get(latestTweets.size() - 1).getId() == announcer.getLastTweetId()) {
                log.debug("Found no new tweets for this announcer. Skipping.");
                return;
            }

            log.debug("Found new tweets for this announcer. Processing tweets.");

            // Need to handle multiple new tweets between check intervals.

            // Find the last tweet seen.
            int latestSeen = 0;

            while (latestSeen < latestTweets.size() && latestTweets.get(latestSeen).getId() != announcer.getLastTweetId()) {
                latestSeen++;
            }

            latestSeen++;

            List<Status> newTweets = latestTweets;

            if (latestSeen < latestTweets.size()) {
                newTweets = latestTweets.subList(latestSeen, latestTweets.size());
            }

            newTweets.forEach(tweet -> {
                TextChannel channel = Session.getSession()
                                             .getJdaClient()
                                             .getTextChannelById(announcer.getChannelId());

                if (channel != null) {
                    channel.sendMessage(createEmbed(tweet)).queue((message) -> {
                        announcer.setLastTweetId(tweet.getId());
                        Session.getSession().getTweetAnnouncerDataRepository().update(announcer);
                    }, error -> {
                        log.error(String.format("Failed to send tweet announcement to channel %s.", channel.getId()), error);
                    });

                    log.debug(
                        String.format(
                            "Issued announcement for new tweet from %s to channel: %s.",
                            announcer.getUser(),
                            announcer.getChannelId()
                        )
                    );
                } else {
                    Session.getSession().getTweetAnnouncerDataRepository().remove(announcer);
                    log.debug("Found announcer for channel that no longer exists. Removing announcer.");
                }
            });
        });
    }

    private MessageEmbed createEmbed(Status tweet) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Session.getSession().getConfiguration().readInt("defaultEmbedColorDecimal"));
        embedBuilder.setTitle(
            String.format("New Tweet From %s", tweet.getUser().getName()),
            String.format(GlobalConfiguration.TWITTER_PERMALINK, tweet.getUser().getScreenName(), tweet.getId())
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
