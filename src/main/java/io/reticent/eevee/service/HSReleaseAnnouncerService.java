package io.reticent.eevee.service;

import com.google.common.collect.ImmutableList;
import io.reticent.eevee.provider.HSReleaseDataProvider;
import io.reticent.eevee.provider.model.HSReleaseData;
import io.reticent.eevee.repository.HSReleaseAnnouncerDataRepository;
import io.reticent.eevee.repository.model.HSReleaseAnnouncer;
import io.reticent.eevee.session.Session;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Log4j2
public class HSReleaseAnnouncerService implements Service {
    private static HSReleaseAnnouncerService hsReleaseAnnouncerService;

    public static HSReleaseAnnouncerService getInstance() {
        if (hsReleaseAnnouncerService == null) {
            hsReleaseAnnouncerService = new HSReleaseAnnouncerService();
        }

        return hsReleaseAnnouncerService;
    }

    @Override
    public void start() {
        Thread thread = new Thread("HorribleSubsReleaseNotificationThread") {
            public void run() {
                while (true) {
                    checkOnce();

                    try {
                        TimeUnit.MILLISECONDS.sleep(Session.getSession().getConfiguration().readInt("animeReleaseCheckDelay"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        log.error("Failed to sleep after checking for HS releases. Will try again.", e);
                    }
                }
            }
        };

        thread.start();
    }

    private void checkOnce() {
        log.debug("Checking for new HS release data.");

        List<HSReleaseAnnouncer> toNotifyList = Session.getSession().getHsReleaseAnnouncerDataRepository().getAnnouncers();
        Optional<List<HSReleaseData>> releaseDataOptional = HSReleaseDataProvider.getData();

        if (!releaseDataOptional.isPresent()) {
            log.debug("No HS release data present. Skipping.");
            return;
        }

        for (HSReleaseData releaseData : releaseDataOptional.get()) {
            List<HSReleaseAnnouncer> test = toNotifyList.stream()
                                                        .filter(a -> a.getAnime().equalsIgnoreCase(releaseData.getTitle()))
                                                        .filter(a -> a.getLastEpisode() < releaseData.getEpisode())
                                                        .filter(a -> a.getQuality().equalsIgnoreCase(releaseData.getQuality())).collect(ImmutableList.toImmutableList());

            log.debug(String.format("Found %s matching announcers for release: %s", test.size(), releaseData));

            assert test != null;

            test.forEach(a -> {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(
                    String.format("New Episode of *%s* Released!", releaseData.getTitle())
                );
                embedBuilder.addField("Episode", releaseData.getEpisode() + "", true);
                embedBuilder.addField("Quality", releaseData.getQuality(), true);
                embedBuilder.addField("Format", releaseData.getFormat(), true);
                embedBuilder.setColor(Session.getSession().getConfiguration().readInt("defaultEmbedColorDecimal"));

                TextChannel channel = Session.getSession()
                                                     .getJdaClient()
                                                     .getTextChannelById(a.getChannelId());

                if (channel != null) {
                    channel.sendMessage(embedBuilder.build()).queue();
                    log.debug(String.format("Issued announcement for new release to channel: %s.'", a.getChannelId()));
                    a.setLastEpisode(releaseData.getEpisode());
                    Session.getSession().getHsReleaseAnnouncerDataRepository().update(a);
                } else {
                    log.debug("Found announcer for channel that no longer exists. Removing announcer.");
                    Session.getSession().getHsReleaseAnnouncerDataRepository().remove(a);
                }
            });
        }
    }
}
