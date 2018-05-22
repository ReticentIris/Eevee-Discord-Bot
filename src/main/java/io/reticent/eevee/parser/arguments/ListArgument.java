package io.reticent.eevee.parser.arguments;

import io.reticent.eevee.parser.Tokenizer;
import lombok.NonNull;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ListArgument<BaseType extends Argument, ReturnType> extends Argument {
    private BaseType[] values;

    public ListArgument(@NonNull String name, @NonNull BaseType[] arguments) {
        super(name);
        this.values = arguments;
    }

    @Override
    public boolean isValid(@NonNull Tokenizer tokens, @NonNull Message message) {
        for (BaseType argument : values) {
            if (!argument.isValid(tokens, message)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ReturnType[] parse(@NonNull Tokenizer tokens, @NonNull Message message) {
        List<ReturnType> resultsList = new LinkedList<>();

        for (BaseType argument : values) {
            resultsList.add((ReturnType) argument.parse(tokens, message));
        }

        return (ReturnType[]) resultsList.toArray();
    }

    @Override
    public String toString() {
        String returnString = String.format("<%s | [", getName());
        String types = String.join(" ", (String[]) Arrays.stream(values).map(Object::toString).toArray());
        return String.format("%s%s]>", returnString, types);
    }
}
