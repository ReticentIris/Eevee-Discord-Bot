package io.reticent.eevee.provider;

import io.reticent.eevee.session.Session;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterClientProvider {
    private static Twitter twitterClient;

    public static Twitter getInstance() {
        if (twitterClient == null) {
            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
            configurationBuilder.setDebugEnabled(false)
                                .setOAuthConsumerKey(Session.getSession().getConfiguration().readString("twitterApiConsumerKey"))
                                .setOAuthConsumerSecret(Session.getSession().getConfiguration().readString("twitterApiConsumerSecret"))
                                .setOAuthAccessToken(Session.getSession().getConfiguration().readString("twitterApiAccessToken"))
                                .setOAuthAccessTokenSecret(Session.getSession().getConfiguration().readString("twitterApiAccessSecret"));

            TwitterFactory twitterFactory = new TwitterFactory(configurationBuilder.build());
            twitterClient = twitterFactory.getInstance();
        }

        return twitterClient;
    }
}
