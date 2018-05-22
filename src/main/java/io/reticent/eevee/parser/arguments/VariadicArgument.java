package io.reticent.eevee.parser.arguments;

import io.reticent.eevee.parser.Tokenizer;
import lombok.NonNull;
import net.dv8tion.jda.core.entities.Message;

import java.util.LinkedList;
import java.util.List;

public class VariadicArgument<BaseType extends Argument, ReturnType> extends Argument {
    private BaseType dummy;

    public VariadicArgument(@NonNull String name, @NonNull BaseType dummy) {
        super(name);
        this.dummy = dummy;
    }

    @Override
    public boolean isValid(@NonNull Tokenizer tokens, @NonNull Message message) {
        while (tokens.hasNext()) {
            if (!dummy.isValid(tokens, message)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<ReturnType> parse(@NonNull Tokenizer tokens, @NonNull Message message) {
        List<ReturnType> arguments = new LinkedList<>();

        while (tokens.hasNext()) {
            arguments.add((ReturnType) dummy.parse(tokens, message));
        }

        return arguments;
    }

    @Override
    public String toString() {
        return String.format("<%s | variadic (%s)>", getName(), dummy.getClass().getSimpleName());
    }
}
