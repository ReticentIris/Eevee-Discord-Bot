package io.reticent.eevee.bot.command.util.stats;

import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.parser.arguments.*;
import io.reticent.eevee.session.Session;
import io.reticent.eevee.util.TimeUtil;
import lombok.NonNull;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.time.Duration;
import java.time.Instant;

public class StatsCommand extends Command {
    private Instant startTime;

    @Override
    public void bootstrap() {
        startTime = Instant.now();
    }

    @Override
    public String getShortLabel() {
        return "stats";
    }

    @Override
    public String getLabel() {
        return "Bot Stats";
    }

    @Override
    public String getDescription() {
        return "Displays bot stats.";
    }

    @Override
    public Arguments<? extends CommandArguments> getArguments() {
        Argument[] argsArray = {
            new LiteralArgument("stats")
        };

        return new Arguments<>(argsArray, StatsCommandArguments.class);
    }

    @Override
    public void invoke(@NonNull MessageReceivedEvent event, @NonNull CommandArguments arguments) {
        Instant now = Instant.now();
        Duration uptime = Duration.between(startTime, now);
        String uptimeString = TimeUtil.durationToDDHHMMSS(uptime);

        JDA jdaClient = Session.getJdaClient();

        int numberOfServers = jdaClient.getGuilds().size();
        int numberOfMembers = jdaClient.getGuilds().stream().map(g -> g.getMembers().size()).reduce(0, (a, b) -> a + b);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Eevee Stats");
        embedBuilder.addField("Uptime", uptimeString, false);
        embedBuilder.addField("Guild Count", numberOfServers + "", false);
        embedBuilder.addField("Member Count", numberOfMembers + "", false);
        embedBuilder.setColor(Session.getConfiguration().readInt("defaultEmbedColorDecimal"));

        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
