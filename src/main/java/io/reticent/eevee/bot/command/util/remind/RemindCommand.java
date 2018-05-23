package io.reticent.eevee.bot.command.util.remind;

import io.reticent.eevee.bot.command.Command;
import io.reticent.eevee.bot.command.CommandArguments;
import io.reticent.eevee.exc.DataRepositoryException;
import io.reticent.eevee.exc.InvalidConfigurationException;
import io.reticent.eevee.repository.model.Reminder;
import io.reticent.eevee.parser.arguments.*;
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
        Session.getReminderDataRepository().getReminders().forEach(this::spawnReminderThread);
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
            new Arguments<>(new Argument[]{
                new NumberArgument("days").withOptions(
                    ArgumentOptions.builder()
                                   .required(false)
                                   .defaultValue(0)
                                   .build()
                ),
                new OrArgument("daysLabel", new String[]{"days", "day"})
            }, RemindCommandArguments.class).withOptions(
                ArgumentOptions.builder()
                               .required(false)
                               .build()
            ),
            new Arguments<>(new Argument[]{
                new NumberArgument("hours").withOptions(
                    ArgumentOptions.builder()
                                   .defaultValue(0)
                                   .build()
                ),
                new OrArgument("hoursLabel", new String[]{"hours", "hour"})
            }, RemindCommandArguments.class).withOptions(
                ArgumentOptions.builder()
                               .required(false)
                               .build()
            ),
            new Arguments<>(new Argument[]{
                new NumberArgument("minutes").withOptions(
                    ArgumentOptions.builder()
                                   .defaultValue(0)
                                   .build()
                ),
                new OrArgument("minutesLabel", new String[]{"minutes", "minute"})
            }, RemindCommandArguments.class).withOptions(
                ArgumentOptions.builder()
                               .required(false)
                               .build()
            ),
            new Arguments<>(new Argument[]{
                new NumberArgument("seconds").withOptions(
                    ArgumentOptions.builder()
                                   .defaultValue(0)
                                   .build()
                ),
                new OrArgument("secondsLabel", new String[]{"seconds", "second"})
            }, RemindCommandArguments.class).withOptions(
                ArgumentOptions.builder()
                               .required(false)
                               .build()
            ),
            new LiteralArgument("to"),
            new VariadicArgument<StringArgument, String>("action", new StringArgument("foo"))
        };

        return new Arguments<>(argsArray, RemindCommandArguments.class);
    }

    @Override
    public void invoke(@NonNull MessageReceivedEvent event, @NonNull CommandArguments arguments) throws InvalidConfigurationException {
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

        Session.getReminderDataRepository().add(reminder);
        log.info("Successfully added new reminder to reminder datastore.");

        spawnReminderThread(reminder);

        log.debug(String.format("Spawned new reminder thread for %s.", Formatter.formatTag(event.getAuthor())));

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Okay. I will remind you to...");
        embedBuilder.setDescription(remindAction);
        embedBuilder.setColor(Session.getConfiguration().readInt("defaultEmbedColorDecimal"));

        event.getTextChannel().sendMessage(embedBuilder.build()).queue();
    }

    private void spawnReminderThread(@NonNull Reminder reminder) {
        Instant now = Instant.now();
        Instant remindAt = reminder.getRemindAt();
        Duration difference = Duration.between(now, remindAt);
        long milli = difference.getSeconds() < 0 ? 0 : difference.getSeconds() * 1000;

        Thread thread = new Thread("ReminderThread"){
            public void run() {
                log.debug(String.format("Sleeping reminder thread for %s ms.", milli));

                try {
                    TimeUnit.MILLISECONDS.sleep(milli);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    log.error("Failed to sleep reminder thread.", e);
                }

                Session.getJdaClient().getUserById(reminder.getUserId()).openPrivateChannel().queue((channel) -> {
                    issueReminder(reminder.getReminder(), channel);

                    Session.getReminderDataRepository().remove(reminder);

                    log.debug(String.format(
                        "Issued reminder to %s. Reminder thread will die", reminder.getUserTag()
                    ));
                }, (error) -> {
                    log.error("Failed to open private text channel to issue reminder.", error);
                    error.printStackTrace();
                });
            }
        };

        thread.start();
    }

    private void issueReminder(@NonNull String reminder, @NonNull PrivateChannel channel) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Reminder");
        embedBuilder.setDescription(reminder);
        embedBuilder.setColor(Session.getConfiguration().readInt("defaultEmbedColorDecimal"));

        channel.sendMessage(embedBuilder.build()).queue();
    }
}
