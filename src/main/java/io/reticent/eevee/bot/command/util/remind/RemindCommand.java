package io.reticent.eevee.bot.command.util.remind;

import com.google.common.collect.ImmutableList;
import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.exc.DataRepositoryException;
import io.reticent.eevee.exc.InvalidConfigurationException;
import io.reticent.eevee.repository.model.Reminder;
import io.reticent.eevee.parser.arguments.*;
import io.reticent.eevee.service.ReminderService;
import io.reticent.eevee.session.Session;
import io.reticent.eevee.util.Formatter;
import io.reticent.eevee.util.TimeUtil;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
public class RemindCommand extends Command {
    @Override
    public void bootstrap() {
        Session.getSession()
               .getReminderDataRepository()
               .getReminders()
               .stream()
               .map(ReminderService::getInstance)
               .forEach(ReminderService::start);
    }

    @Override
    public String getShortLabel() {
        return "remind";
    }

    @Override
    public String getLabel() {
        return "Remind Me";
    }

    @Override
    public String getDescription() {
        return "Set a reminder to do something.";
    }

    @Override
    public Arguments<? extends CommandArguments> getArguments() {
        Argument[] argsArray = {
            new LiteralArgument("remind"),
            new LiteralArgument("me"),
            new LiteralArgument("in"),
            new Arguments<>(ImmutableList.of(
                new NumberArgument("days").withOptions(
                    ArgumentOptions.builder()
                                   .required(false)
                                   .defaultValue(0)
                                   .build()
                ),
                new OrArgument("daysLabel", ImmutableList.of("days", "day"))
            ), RemindCommandArguments.class).withOptions(
                ArgumentOptions.builder()
                               .required(false)
                               .build()
            ),
            new Arguments<>(ImmutableList.of(
                new NumberArgument("hours").withOptions(
                    ArgumentOptions.builder()
                                   .defaultValue(0)
                                   .build()
                ),
                new OrArgument("hoursLabel", ImmutableList.of("hours", "hour"))
            ), RemindCommandArguments.class).withOptions(
                ArgumentOptions.builder()
                               .required(false)
                               .build()
            ),
            new Arguments<>(ImmutableList.of(
                new NumberArgument("minutes").withOptions(
                    ArgumentOptions.builder()
                                   .defaultValue(0)
                                   .build()
                ),
                new OrArgument("minutesLabel", ImmutableList.of("minutes", "minute"))
            ), RemindCommandArguments.class).withOptions(
                ArgumentOptions.builder()
                               .required(false)
                               .build()
            ),
            new Arguments<>(ImmutableList.of(
                new NumberArgument("seconds").withOptions(
                    ArgumentOptions.builder()
                                   .defaultValue(0)
                                   .build()
                ),
                new OrArgument("secondsLabel", ImmutableList.of("seconds", "second"))
            ), RemindCommandArguments.class).withOptions(
                ArgumentOptions.builder()
                               .required(false)
                               .build()
            ),
            new LiteralArgument("to"),
            new VariadicArgument<StringArgument, String>("action", new StringArgument("foo"))
        };

        return new Arguments<>(ImmutableList.copyOf(argsArray), RemindCommandArguments.class);
    }

    @Override
    public void invoke(@NonNull MessageReceivedEvent event, @NonNull CommandArguments arguments) {
        RemindCommandArguments args = (RemindCommandArguments) arguments;
        String remindAction = args.getAction().stream().collect(Collectors.joining(" "));
        long milli = TimeUtil.dhmsToMilli(args.getDays(), args.getHours(), args.getMinutes(), args.getSeconds());
        Instant now = Instant.now();

        Reminder reminder = Reminder.builder()
                                    .userTag(Formatter.formatTag(event.getAuthor()))
                                    .userId(event.getAuthor().getId())
                                    .reminder(remindAction)
                                    .remindAt(now.plusMillis(milli))
                                    .build();

        log.info("Adding new reminder to reminder datastore.");

        Session.getSession().getReminderDataRepository().add(reminder);

        log.info("Successfully added new reminder to reminder datastore.");

        ReminderService.getInstance(reminder).start();

        log.debug(String.format("Spawned new reminder thread for %s.", Formatter.formatTag(event.getAuthor())));

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Okay. I will remind you to...");
        embedBuilder.setDescription(remindAction);
        embedBuilder.setColor(Session.getSession().getConfiguration().readInt("defaultEmbedColorDecimal"));

        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}
