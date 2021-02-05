package no.unit.bibs.elasticsearch;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class InputException extends ApiGatewayException {

    public InputException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return HttpStatus.SC_BAD_REQUEST;
    }
}
