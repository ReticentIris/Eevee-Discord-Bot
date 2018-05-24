package io.reticent.eevee.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reticent.eevee.configuration.Configuration;
import io.reticent.eevee.repository.HSReleaseAnnouncerDataRepository;
import io.reticent.eevee.repository.ReminderDataRepository;
import lombok.*;
import net.dv8tion.jda.core.JDA;

@NoArgsConstructor
@Data
public class Session {
    @NonNull
    private Configuration configuration;
    @NonNull
    private JDA jdaClient;
    @NonNull
    private ReminderDataRepository reminderDataRepository;
    @NonNull
    private HSReleaseAnnouncerDataRepository hsReleaseAnnouncerDataRepository;
    @NonNull
    private ObjectMapper objectMapper;

    private static Session session;

    public static Session getSession() {
        if (session == null) {
            session = new Session();
        }

        return session;
    }
}
