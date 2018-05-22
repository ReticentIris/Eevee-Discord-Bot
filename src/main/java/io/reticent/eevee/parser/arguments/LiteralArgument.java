package io.reticent.eevee.parser.arguments;

import io.reticent.eevee.parser.Tokenizer;
import lombok.NonNull;
import net.dv8tion.jda.core.entities.Message;

public class LiteralArgument extends Argument {
    private String value;

    public LiteralArgument(@NonNull String value) {
        super("FOO");
        this.value = value;
    }

    @Override
    public boolean isValid(@NonNull Tokenizer tokens, @NonNull Message message) {
        String token = tokens.next();
        return token != null && token.equalsIgnoreCase(value);
    }

    @Override
    public Object parse(@NonNull Tokenizer tokens, @NonNull Message message) {
        return tokens.next();
    }

    @Override
    public String toString() {
        return value;
    }
}
