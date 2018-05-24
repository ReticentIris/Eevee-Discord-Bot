package io.reticent.eevee.bot.command.fun.anime.horriblesubs;

import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.exc.InvalidConfigurationException;
import io.reticent.eevee.parser.arguments.*;
import io.reticent.eevee.provider.UUIDProvider;
import io.reticent.eevee.repository.model.HSReleaseAnnouncer;
import io.reticent.eevee.service.HSReleaseAnnouncerService;
import io.reticent.eevee.session.Session;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

@Log4j2
public class HSReleaseSubscribeCommand extends Command {
    @Override
    public void bootstrap() {
        HSReleaseAnnouncerService.getInstance().start();
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
        return "Sets release announcements to be posted in the current channel. Requires manage channel permission to use.";
    }

    @Override
    public Permission[] getRequiredPermissions() {
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
    public void invoke(@NonNull MessageReceivedEvent event, @NonNull CommandArguments arguments) {
        HSReleaseSubscribeCommandArguments args = (HSReleaseSubscribeCommandArguments) arguments;

        HSReleaseAnnouncer hsReleaseAnnouncer = HSReleaseAnnouncer.builder()
                                                                  .anime(args.getAnimeName())
                                                                  .quality(args.getQuality())
                                                                  .channelId(event.getChannel().getId())
                                                                  .lastEpisode(-1)
                                                                  .announcerId(UUIDProvider.getUUID4())
                                                                  .build();

        Session.getHsReleaseAnnouncerDataRepository().add(hsReleaseAnnouncer);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("HorribleSubs Release Subscription Added");
        embedBuilder.setColor(Session.getConfiguration().readInt("successEmbedColorDecimal"));
        embedBuilder.setDescription(
            String.format("Okay. I will announce when *%s* is released in %s.", args.getAnimeName(), args.getQuality())
        );

        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}