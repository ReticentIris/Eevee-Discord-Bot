package io.reticent.eevee;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reticent.eevee.bot.EeveeBot;
import io.reticent.eevee.configuration.GlobalConfiguration;
import io.reticent.eevee.exc.InvalidConfigurationException;
import io.reticent.eevee.configuration.Configuration;
import io.reticent.eevee.repository.HSReleaseAnnouncerDataRepository;
import io.reticent.eevee.repository.ReminderDataRepository;
import io.reticent.eevee.session.Session;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;

import javax.security.auth.login.LoginException;

@Log4j2
public class Eevee {
    public static void main(String[] args) {
        Configuration configuration = Configuration.builder()
                                                   .filePath(GlobalConfiguration.CONFIGURATION_PATH)
                                                   .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        Session.getSession().setConfiguration(configuration);
        Session.getSession().setObjectMapper(mapper);

        Session.getSession().setReminderDataRepository(ReminderDataRepository.getInstance());
        Session.getSession().setHsReleaseAnnouncerDataRepository(HSReleaseAnnouncerDataRepository.getInstance());

        try {
            log.info(String.format("Using bot token: %s.", configuration.readString("botToken")));

            EeveeBot bot = EeveeBot.builder().build();

            JDA jda = new JDABuilder(AccountType.BOT).setToken(configuration.readString("botToken"))
                                                     .setGame(Game.playing("ev help"))
                                                     .addEventListener(bot)
                                                     .buildBlocking();

            Session.getSession().setJdaClient(jda);

            bot.registerCommands();
        } catch (LoginException | InterruptedException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
