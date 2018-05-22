package io.reticent.eevee.bot.command.util.remind;

import io.reticent.eevee.bot.command.CommandArguments;
import lombok.Getter;

import java.util.List;

public class RemindCommandArguments extends CommandArguments {
    @Getter
    private double days;
    @Getter
    private double hours;
    @Getter
    private double minutes;
    @Getter
    private double seconds;
    @Getter
    private List<String> action;

    private String daysLabel;
    private String hoursLabel;
    private String minutesLabel;
    private String secondsLabel;
}
