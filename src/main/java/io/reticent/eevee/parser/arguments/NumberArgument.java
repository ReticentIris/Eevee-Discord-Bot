package io.reticent.eevee.parser.arguments;

import io.reticent.eevee.parser.Tokenizer;
import lombok.NonNull;
import net.dv8tion.jda.core.entities.Message;

public class NumberArgument extends Argument {
    public NumberArgument(String name) {
        super(name);
    }

    @Override
    public boolean isValid(@NonNull Tokenizer tokens, @NonNull Message message) {
        String token = tokens.next();

        if (token == null) {
            return false;
        }

        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Double parse(@NonNull Tokenizer tokens, @NonNull Message message) {
        String token = tokens.next();

        try {
            double d = Double.parseDouble(token);
            return d;
        } catch (NumberFormatException e) {
            // This should never be reached.
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("<%s | number>", getName());
    }
}
