package io.reticent.eevee.parser.arguments;

import io.reticent.eevee.exc.ArgumentMappingException;
import io.reticent.eevee.parser.Tokenizer;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.core.entities.Message;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Arguments<ObjectMap> extends Argument {
    @Getter
    private Argument[] arguments;
    private Class<ObjectMap> mapClass;

    public Arguments(Argument[] arguments, Class<ObjectMap> mapClass) {
        this.arguments = arguments;
        this.mapClass = mapClass;
    }

    public boolean isValid(@NonNull String str, @NonNull Message message) {
        Tokenizer tokenizer = new Tokenizer(str);
        return isValid(tokenizer, message);
    }

    @Override
    public boolean isValid(@NonNull Tokenizer tokens, @NonNull Message message) {
        if (!isPartialValid(tokens, message)) {
            return false;
        }

        if (tokens.hasNext()) {
            return false;
        }

        return true;
    }

    public ObjectMap parse(@NonNull String str, @NonNull Message message) {
        Tokenizer tokenizer = new Tokenizer(str);
        return parse(tokenizer, message);
    }

    @Override
    public ObjectMap parse(@NonNull Tokenizer tokens, @NonNull Message message) {
        try {
            ObjectMap obj = mapClass.newInstance();
            parsePartial(tokens, message, obj);
            return obj;
        } catch (InstantiationException|IllegalAccessException|NoSuchFieldException e) {
            e.printStackTrace();
            throw new ArgumentMappingException("Failed to parse and get command arguments.");
        }
    }

    public boolean isPartialValid(@NonNull Tokenizer tokens, @NonNull Message message) {
        for (Argument arg : arguments) {
            if (arg instanceof Arguments) {
                tokens.stash();

                boolean subArgumentsAreValid = ((Arguments) arg).isPartialValid(tokens, message);

                if (!subArgumentsAreValid) {
                    tokens.pop();

                    if (arg.getOptions().isRequired()) {
                        return false;
                    }
                }
            } else {
                if (!tokens.hasNext()) {
                    if (!arg.getOptions().isRequired()) {
                        continue;
                    }

                    return false;
                }

                if (!arg.isValid(tokens, message) && arg.getOptions().isRequired()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void parsePartial(@NonNull Tokenizer tokens, @NonNull Message message, @NonNull ObjectMap obj) throws NoSuchFieldException, IllegalAccessException {
        for (Argument arg : arguments) {
            if (!tokens.hasNext()) {
                return;
            }

            if (arg instanceof LiteralArgument) {
                tokens.next();
            } else if (arg instanceof Arguments) {
                tokens.stash();

                if (!((Arguments) arg).isPartialValid(tokens, message) && !arg.getOptions().isRequired()) {
                    tokens.pop();

                    for (Argument subArgument : ((Arguments) arg).getArguments()) {
                        Object defaultValue = subArgument.getOptions().getDefaultValue();

                        if (defaultValue == null) {
                            continue;
                        }

                        applyValue(obj, subArgument.getName(), subArgument.getOptions().getDefaultValue());
                    }
                } else {
                    tokens.pop();
                    ((Arguments) arg).parsePartial(tokens, message, obj);
                }
            } else {
                tokens.stash();

                if (arg.isValid(tokens, message)) {
                    tokens.pop();
                    applyValue(obj, arg.getName(), arg.parse(tokens, message));
                } else {
                    tokens.pop();

                    Object defaultValue = arg.getOptions().getDefaultValue();

                    if (defaultValue != null) {
                        applyValue(obj, arg.getName(), arg.getOptions().getDefaultValue());
                    }
                }
            }
        }
    }

    private void applyValue(@NonNull ObjectMap obj, @NonNull String name, @NonNull Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Override
    public String toString() {
        return Arrays.stream(arguments).map(Object::toString).collect(Collectors.joining(" "));
    }
}
