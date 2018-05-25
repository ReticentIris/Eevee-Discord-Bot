package io.reticent.eevee.parser.arguments;

import io.reticent.eevee.parser.Tokenizer;
import lombok.NonNull;
import net.dv8tion.jda.core.entities.Message;

public class BooleanArgument extends Argument {
    private static final String[] TRUE_BOOLEAN_STRING_VALUES = {"yes"};
    private static final String[] FALSE_BOOLEAN_STRING_VALUES = {"no"};

    public BooleanArgument(String name) {
        super(name);
    }

    @Override
    public boolean isValid(@NonNull Tokenizer tokens, @NonNull Message message) {
        String token = tokens.next();

        for (String str : BooleanArgument.TRUE_BOOLEAN_STRING_VALUES) {
            if (str.equalsIgnoreCase(token)) {
                return true;
            }
        }

        for (String str : BooleanArgument.FALSE_BOOLEAN_STRING_VALUES) {
            if (str.equalsIgnoreCase(token)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Boolean parse(@NonNull Tokenizer tokens, @NonNull Message message) {
        String token = tokens.next();

        for (String str : BooleanArgument.TRUE_BOOLEAN_STRING_VALUES) {
            if (str.equalsIgnoreCase(token)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format("<%s | boolean>", getName());
    }
}
