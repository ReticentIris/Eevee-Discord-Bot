package io.reticent.eevee.provider;

import io.reticent.eevee.configuration.GlobalConfiguration;
import io.reticent.eevee.provider.model.JishoSearchResult;
import io.reticent.eevee.session.Session;
import io.reticent.eevee.util.NetUtil;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Optional;

@Log4j2
public class JishoSearchProvider {
    public static Optional<JishoSearchResult> getSearchResult(String query) {
        try {
            String json = NetUtil.getPage(String.format(GlobalConfiguration.JISHO_SEARCH_API_URL, query));
            return Optional.of(
                Session.getSession().getObjectMapper().readValue(json, JishoSearchResult.class)
            );
        } catch (IOException e) {
            e.printStackTrace();
            log.error(String.format("Failed to search Jisho for query: %s.", query), e);
        }

        return Optional.empty();
    }
}
