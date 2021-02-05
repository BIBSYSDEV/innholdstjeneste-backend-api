package no.unit.bibs.elasticsearch;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExceptionsTest {

    public static final String MESSAGE = "Message";

    @Test
    public void importExceptionHasStatusCode() {
        ApiGatewayException exception = new ImportException(MESSAGE);
        Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, exception.getStatusCode());
    }

    @Test
    public void inputExceptionHasStatusCode() {
        ApiGatewayException exception = new InputException(MESSAGE);
        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatusCode());
    }

}