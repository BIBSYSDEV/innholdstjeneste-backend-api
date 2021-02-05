package no.unit.bibs.elasticsearch;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class ImportException extends ApiGatewayException {

    public ImportException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }
}
