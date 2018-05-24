package io.reticent.eevee.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reticent.eevee.exc.DataRepositoryException;
import io.reticent.eevee.session.Session;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;

@Log4j2
public abstract class DataRepository {
    public static DataRepository getInstance() throws IOException {
        throw new UnsupportedOperationException();
    }

    void commitAndFlush(String path) throws DataRepositoryException {
        ObjectMapper objectMapper = Session.getSession().getObjectMapper();

        try {
            objectMapper.writeValue(new File(path), this);
            log.info("Successfully wrote to data repository file.");
        } catch (IOException e) {
            log.error("Failed to write to data repository file.", e);
            e.printStackTrace();

            throw new DataRepositoryException("Failed to write to data repository file.");
        }
    }
}
