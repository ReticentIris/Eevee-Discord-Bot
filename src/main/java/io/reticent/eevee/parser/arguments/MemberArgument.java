package io.reticent.eevee.parser.arguments;

import io.reticent.eevee.parser.Tokenizer;
import lombok.NonNull;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

public class MemberArgument extends Argument {
    public MemberArgument(@NonNull String name) {
        super(name);
    }

    @Override
    public boolean isValid(@NonNull Tokenizer tokens, @NonNull Message message) {
        String token = tokens.next();

        if (!token.matches("<@!?\\d+>")) {
            return false;
        }

        String memberId = token.replaceAll("(<@!?)|>", "");

        return message.getMentionedMembers()
                      .stream()
                      .anyMatch(member -> member.getUser().getId().equals(memberId));
    }

    @Override
    public Member parse(@NonNull Tokenizer tokens, @NonNull Message message) {
        String token = tokens.next();
        String memberId = token.replaceAll("(<@!?)|>", "");

        return message.getMentionedMembers()
                      .stream()
                      .filter(member -> member.getUser().getId().equals(memberId))
                      .findFirst().get();
    }

    @Override
    public String toString() {
        return String.format("<%s | member mention>", getName());
    }
}
