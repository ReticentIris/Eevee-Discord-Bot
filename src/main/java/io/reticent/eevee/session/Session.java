package io.reticent.eevee.session;

import io.reticent.eevee.configuration.Configuration;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.JDA;

public class Session {
    @Getter
    @Setter
    private static Configuration configuration;
    @Getter
    @Setter
    private static JDA jdaClient;
}
