package io.reticent.eevee;

import io.reticent.eevee.bot.EeveeBot;
import io.reticent.eevee.configuration.GlobalConfiguration;
import io.reticent.eevee.exc.InvalidConfigurationException;
import io.reticent.eevee.configuration.Configuration;
import io.reticent.eevee.session.Session;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

import javax.security.auth.login.LoginException;

@Log4j2
public class Eevee {
    public static void main(String[] args) {
        Configuration configuration = Configuration.builder()
                                                   .filePath(GlobalConfiguration.CONFIGURATION_PATH)
                                                   .build();

        Session.setConfiguration(configuration);

        try {
            log.info(String.format("Using bot token: %s.", configuration.readString("botToken")));

            EeveeBot bot = EeveeBot.builder().build();
            bot.registerCommands();

            JDA jda = new JDABuilder(AccountType.BOT).setToken(configuration.readString("botToken"))
                                                     .addEventListener(bot)
                                                     .buildBlocking();
        } catch (LoginException | InterruptedException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}