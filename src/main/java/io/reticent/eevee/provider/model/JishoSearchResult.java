package io.reticent.eevee.provider.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.reticent.eevee.provider.model.jisho.Meta;
import io.reticent.eevee.provider.model.jisho.ResultData;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JishoSearchResult {
    @JsonProperty
    private Meta meta;
    @JsonProperty
    private List<ResultData> data;
}
