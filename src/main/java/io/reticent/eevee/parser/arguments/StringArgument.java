package io.reticent.eevee.parser.arguments;

import io.reticent.eevee.parser.Tokenizer;
import lombok.NonNull;
import net.dv8tion.jda.core.entities.Message;

public class StringArgument extends Argument {
    public StringArgument(@NonNull String name) {
        super(name);
    }

    @Override
    public boolean isValid(@NonNull Tokenizer tokens, @NonNull Message message) {
        // Consume a token.
        tokens.next();
        return true;
    }

    @Override
    public Object parse(@NonNull Tokenizer tokens, @NonNull Message message) {
        return tokens.next();
    }

    @Override
    public String toString() {
        return String.format("<%s | string>", getName());
    }
}
