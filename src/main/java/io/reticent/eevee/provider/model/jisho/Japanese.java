package io.reticent.eevee.provider.model.jisho;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Japanese {
    @JsonProperty
    private String word;
    @JsonProperty
    private String reading;
}
