package io.reticent.eevee.bot.command;

import com.google.common.collect.ImmutableList;
import io.reticent.eevee.parser.arguments.Arguments;
import io.reticent.eevee.util.RateLimiter;
import lombok.NonNull;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

public abstract class Command {
    public void bootstrap() {}

    public boolean requiresBotOwner() {
        return false;
    }

    public List<Permission> getRequiredPermissions() {
        return ImmutableList.of();
    }

    public RateLimiter getRateLimiter() {
        return null;
    }

    public String toString() {
        return String.format("**__%s__**%n%n%s%n%n```%s```%n", getLabel(), getDescription(), getArguments());
    }

    public abstract String getShortLabel();
    public abstract String getLabel();
    public abstract String getDescription();
    public abstract Arguments<? extends CommandArguments> getArguments();
    public abstract void invoke(@NonNull MessageReceivedEvent event, @NonNull CommandArguments arguments);
}
