package io.reticent.eevee.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reticent.eevee.configuration.Configuration;
import io.reticent.eevee.repository.HSReleaseAnnouncementDataRepository;
import io.reticent.eevee.repository.ReminderDataRepository;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.core.JDA;

public class Session {
    @Getter
    @Setter
    @NonNull
    private static Configuration configuration;
    @Getter
    @Setter
    @NonNull
    private static JDA jdaClient;
    @Getter
    @Setter
    @NonNull
    private static ReminderDataRepository reminderDataRepository;
    @Getter
    @Setter
    @NonNull
    private static HSReleaseAnnouncementDataRepository hsReleaseAnnouncementDataRepository;
    @Getter
    @Setter
    @NonNull
    private static ObjectMapper objectMapper;
}
