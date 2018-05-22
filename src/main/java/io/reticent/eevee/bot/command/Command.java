package io.reticent.eevee.bot.command;

import io.reticent.eevee.exc.InvalidConfigurationException;
import io.reticent.eevee.parser.arguments.Arguments;
import io.reticent.eevee.util.RateLimiter;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class Command {
    @Getter
    @Setter
    private int permissionRequired;
    @Getter
    @Setter
    private RateLimiter rateLimiter;

    public void bootstrap() {}

    public Command withPermissionsRequired(int permissionRequired) {
        setPermissionRequired(permissionRequired);
        return this;
    }

    public Command withRateLimiter(@NonNull RateLimiter rateLimiter) {
        setRateLimiter(rateLimiter);
        return this;
    }

    public String toString() {
        return String.format("**__%s__**\n\n%s\n\n```%s```\n", getLabel(), getDescription(), getArguments());
    }

    public abstract String getShortLabel();
    public abstract String getLabel();
    public abstract String getDescription();
    public abstract Arguments<? extends CommandArguments> getArguments();
    public abstract void invoke(@NonNull MessageReceivedEvent event, @NonNull CommandArguments arguments) throws InvalidConfigurationException;
}
