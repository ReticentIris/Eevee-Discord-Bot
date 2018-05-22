package io.reticent.eevee.parser.arguments;

import io.reticent.eevee.parser.Tokenizer;
import lombok.*;
import net.dv8tion.jda.core.entities.Message;

@NoArgsConstructor
@RequiredArgsConstructor
public abstract class Argument {
    @NonNull
    @Getter
    private String name;
    @Getter
    @Setter
    private ArgumentOptions options = ArgumentOptions.builder()
                                                     .required(true)
                                                     .build();

    public Argument withOptions(ArgumentOptions options) {
        setOptions(options);
        return this;
    }

    public abstract boolean isValid(@NonNull Tokenizer tokens, @NonNull Message message);

    public abstract Object parse(@NonNull Tokenizer tokens, @NonNull Message message);
}
