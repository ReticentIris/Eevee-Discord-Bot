package io.reticent.eevee.service;

import io.reticent.eevee.repository.model.Reminder;
import io.reticent.eevee.session.Session;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.PrivateChannel;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Log4j2
public class ReminderService implements Service {
    private Reminder reminder;

    private ReminderService(@NonNull Reminder reminder) {
        this.reminder = reminder;
    }

    public static ReminderService getInstance(@NonNull Reminder reminder) {
        return new ReminderService(reminder);
    }

    @Override
    public void start() {
        Instant now = Instant.now();
        Instant remindAt = reminder.getRemindAt();
        Duration difference = Duration.between(now, remindAt);
        long milli = difference.getSeconds() < 0 ? 0 : difference.getSeconds() * 1000;

        Thread thread = new Thread("ReminderServiceThread"){
            public void run() {
                log.debug(String.format("Sleeping reminder thread for %s ms.", milli));

                try {
                    TimeUnit.MILLISECONDS.sleep(milli);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    log.error("Failed to sleep reminder thread.", e);
                }

                Session.getSession().getJdaClient().getUserById(reminder.getUserId()).openPrivateChannel().queue((channel) -> {
                    issueReminder(reminder.getReminder(), channel);

                    Session.getSession().getReminderDataRepository().remove(reminder);

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
        embedBuilder.setColor(Session.getSession().getConfiguration().readInt("defaultEmbedColorDecimal"));

        channel.sendMessage(embedBuilder.build()).queue();
    }
}
