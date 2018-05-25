package io.reticent.eevee.provider;

import lombok.extern.log4j.Log4j2;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.List;
import java.util.Optional;

@Log4j2
public class TwitterTweetProvider {
    public static Optional<Status> getLatestTweet(String user) {
        Twitter twitter = TwitterClientProvider.getInstance();

        try {
            List<Status> statuses = twitter.getUserTimeline(user, new Paging(1, 1));

            if (statuses.size() == 0) {
                return Optional.empty();
            }

            return Optional.of(statuses.get(0));
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}
