package io.reticent.eevee.bot.command.fun.anime.horriblesubs;

import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.exc.DataRepositoryException;
import io.reticent.eevee.exc.InvalidConfigurationException;
import io.reticent.eevee.parser.arguments.*;
import io.reticent.eevee.provider.UUIDProvider;
import io.reticent.eevee.repository.model.HSReleaseAnnouncement;
import io.reticent.eevee.rss.HorribleSubsReleaseReader;
import io.reticent.eevee.rss.model.HorribleSubsReleaseItem;
import io.reticent.eevee.session.Session;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
public class HSReleaseAnnounceCommand extends Command {
    private final Object mutex;

    public HSReleaseAnnounceCommand() {
        mutex = new Object();
    }

    @Override
    public void bootstrap() {
        spawnCheckThread();
    }

    @Override
    public String getShortLabel() {
        return "anime.hs.announce";
    }

    @Override
    public String getLabel() {
        return "Announce HorribleSubs Releases";
    }

    @Override
    public String getDescription() {
        return "Sets announcements to be announced in the current channel. Requires manage channel permission to use.";
    }

    @Override
    public Permission[] getPermissionsRequired() {
        return new Permission[]{
            Permission.MANAGE_CHANNEL
        };
    }

    @Override
    public Arguments<? extends CommandArguments> getArguments() {
        Argument[] argsArray = {
            new LiteralArgument("hs"),
            new LiteralArgument("subscribe"),
            new StringArgument("animeName"),
            new OrArgument("quality", new String[]{"480p", "720p", "1080p"})
        };

        return new Arguments<>(argsArray, HSReleaseAnnounceCommandArguments.class);
    }

    @Override
    public void invoke(MessageReceivedEvent event, CommandArguments arguments) throws InvalidConfigurationException {
        HSReleaseAnnounceCommandArguments args = (HSReleaseAnnounceCommandArguments) arguments;

        HSReleaseAnnouncement hsReleaseAnnouncement = HSReleaseAnnouncement.builder()
                                                                           .anime(args.getAnimeName())
                                                                           .quality(args.getQuality())
                                                                           .channelId(event.getTextChannel().getId())
                                                                           .lastEpisode(-1)
                                                                           .announcementId(UUIDProvider.genUUID4())
                                                                           .build();

        synchronized (mutex) {
            try {
                Session.getHsReleaseAnnouncementDataRepository().add(hsReleaseAnnouncement);

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("HorribleSubs Release Announcer Added");
                embedBuilder.setColor(Session.getConfiguration().readInt("successEmbedColorDecimal"));
                embedBuilder.setDescription(
                    String.format("Okay. I will announce when %s is released in %s", args.getAnimeName(), args.getQuality())
                );

                event.getTextChannel().sendMessage(embedBuilder.build()).queue();
            } catch (DataRepositoryException e) {
                e.printStackTrace();
                log.error("Failed to write new announcement to datastore.", e);

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Oops! An error occurred.");
                embedBuilder.setColor(Session.getConfiguration().readInt("errorEmbedColorDecimal"));
                embedBuilder.setDescription(
                    "Failed to persist announcer. Announcements may still be made however they will stop once the bot restarts."
                );

                event.getTextChannel().sendMessage(embedBuilder.build()).queue();
            }
        }
    }

    private void spawnCheckThread() {
        Thread thread = new Thread("HorribleSubsReleaseNotificationThread") {
            public void run() {
                while (true) {
                    doOnce();

                    try {
                        TimeUnit.MILLISECONDS.sleep(Session.getConfiguration().readInt("animeReleaseCheckDelay"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        log.error("Failed to sleep after checking for HS releases. Will try again.", e);
                    }
                }
            }

            private void doOnce() {
                log.debug("Checking for new HS releases.");

                Optional<List<HorribleSubsReleaseItem>> releasesOptional = new HorribleSubsReleaseReader().readFeed();

                if (!releasesOptional.isPresent()) {
                    log.debug("Found no HS release items.");
                    return;
                }

                List<HorribleSubsReleaseItem> releases = releasesOptional.get();

                log.debug(String.format("Found %s HS release items.", releases.size()));

                synchronized (mutex) {
                    List<HSReleaseAnnouncement> toNotifyList = Session.getHsReleaseAnnouncementDataRepository().getAnnouncements();

                    for (HorribleSubsReleaseItem release : releases) {
                        ReleaseData releaseData = ReleaseData.fromString(release.getTitle());

                        List<HSReleaseAnnouncement> test = toNotifyList.stream()
                                                                       .filter(a -> a.getAnime().equalsIgnoreCase(releaseData.getTitle()))
                                                                       .filter(a -> a.getLastEpisode() < releaseData.getEpisode())
                                                                       .filter(a -> a.getQuality().equalsIgnoreCase(releaseData.getQuality())).collect(Collectors.toList());

                        log.debug(String.format("Found %s matching announcers for release: %s", test.size(), releaseData));

                        test.forEach(a -> {
                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setTitle(
                                String.format("New Episode of *%s* Released!", releaseData.getTitle())
                            );
                            embedBuilder.addField("Episode", releaseData.getEpisode() + "", true);
                            embedBuilder.addField("Quality", releaseData.getQuality(), true);
                            embedBuilder.addField("Format", releaseData.getFormat(), true);
                            embedBuilder.setColor(Session.getConfiguration().readInt("defaultEmbedColorDecimal"));

                            Session.getJdaClient()
                                   .getTextChannelById(a.getChannelId())
                                   .sendMessage(embedBuilder.build())
                                   .queue();

                            log.debug(String.format("Issued announcement for new release to channel: %s.'", a.getChannelId()));

                            try {
                                Session.getHsReleaseAnnouncementDataRepository().remove(a);
                                HSReleaseAnnouncement updatedAnnouncement = HSReleaseAnnouncement.builder()
                                                                                                 .anime(a.getAnime())
                                                                                                 .channelId(a.getChannelId())
                                                                                                 .announcementId(a.getAnnouncementId())
                                                                                                 .lastEpisode(releaseData.getEpisode())
                                                                                                 .quality(a.getQuality())
                                                                                                 .build();
                                Session.getHsReleaseAnnouncementDataRepository().add(updatedAnnouncement);
                            } catch (DataRepositoryException e) {
                                e.printStackTrace();
                                log.error("Failed to update HS announcement data repository. Announcement may be sent again", e);
                            }
                        });
                    }
                }
            }
        };

        thread.start();
    }
}