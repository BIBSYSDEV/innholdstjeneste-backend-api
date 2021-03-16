package no.unit.bibs.contents.exception;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SearchExceptionsTest {

    public static final String MESSAGE = "Message";

    @Test
    public void searchExceptionHasStatusCode() {
        ApiGatewayException exception = new SearchException(MESSAGE, new RuntimeException());
        Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, exception.getStatusCode());
    }



}