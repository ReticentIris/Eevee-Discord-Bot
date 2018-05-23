package io.reticent.eevee.bot.command.fun.anime.horriblesubs;

import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.exc.InvalidConfigurationException;
import io.reticent.eevee.parser.arguments.*;
import io.reticent.eevee.provider.HSReleaseDataProvider;
import io.reticent.eevee.provider.UUIDProvider;
import io.reticent.eevee.provider.model.HSReleaseData;
import io.reticent.eevee.repository.model.HSReleaseAnnouncer;
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
public class HSReleaseSubscribeCommand extends Command {
    @Override
    public void bootstrap() {
        spawnCheckThread();
    }

    @Override
    public String getShortLabel() {
        return "anime.hs.subscribe";
    }

    @Override
    public String getLabel() {
        return "Subscribe to HorribleSubs Releases";
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

        return new Arguments<>(argsArray, HSReleaseSubscribeCommandArguments.class);
    }

    @Override
    public void invoke(MessageReceivedEvent event, CommandArguments arguments) throws InvalidConfigurationException {
        HSReleaseSubscribeCommandArguments args = (HSReleaseSubscribeCommandArguments) arguments;

        HSReleaseAnnouncer hsReleaseAnnouncer = HSReleaseAnnouncer.builder()
                                                                  .anime(args.getAnimeName())
                                                                  .quality(args.getQuality())
                                                                  .channelId(event.getTextChannel().getId())
                                                                  .lastEpisode(-1)
                                                                  .announcerId(UUIDProvider.genUUID4())
                                                                  .build();

        Session.getHsReleaseAnnouncerDataRepository().add(hsReleaseAnnouncer);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("HorribleSubs Release Announcer Added");
        embedBuilder.setColor(Session.getConfiguration().readInt("successEmbedColorDecimal"));
        embedBuilder.setDescription(
            String.format("Okay. I will announce when %s is released in %s.", args.getAnimeName(), args.getQuality())
        );

        event.getTextChannel().sendMessage(embedBuilder.build()).queue();
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
                List<HSReleaseAnnouncer> toNotifyList = Session.getHsReleaseAnnouncerDataRepository().getAnnouncers();
                Optional<List<HSReleaseData>> releaseDataOptional = HSReleaseDataProvider.getData();

                if (!releaseDataOptional.isPresent()) {
                    log.debug("No release data present. Skipping.");
                    return;
                }

                for (HSReleaseData releaseData : releaseDataOptional.get()) {
                    List<HSReleaseAnnouncer> test = toNotifyList.stream()
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

                        a.setLastEpisode(releaseData.getEpisode());

                        Session.getHsReleaseAnnouncerDataRepository().update(a);
                    });
                }
            }
        };

        thread.start();
    }
}