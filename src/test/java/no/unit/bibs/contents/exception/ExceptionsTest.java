package no.unit.bibs.contents.exception;

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


}