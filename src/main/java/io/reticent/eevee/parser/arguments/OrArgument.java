package io.reticent.eevee.parser.arguments;

import io.reticent.eevee.parser.Tokenizer;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;

public class OrArgument extends Argument {
    private List<String> options;

    public OrArgument(String name, List<String> options) {
        super(name);
        this.options = options;
    }

    @Override
    public boolean isValid(Tokenizer tokens, Message message) {
        String token = tokens.next();

        for (String option : options) {
            if (option.equalsIgnoreCase(token)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Object parse(Tokenizer tokens, Message message) {
        return tokens.next();
    }

    @Override
    public String toString() {
        return String.format("<%s | [%s]>", getName(), String.join(", ", options));
    }
}
