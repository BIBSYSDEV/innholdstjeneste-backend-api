package no.unit.bibs.contents.exception;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class SearchException extends ApiGatewayException {

    public SearchException(String message, Exception exception) {
        super(exception, message);
    }

    @Override
    protected Integer statusCode() {
        return HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }
}
