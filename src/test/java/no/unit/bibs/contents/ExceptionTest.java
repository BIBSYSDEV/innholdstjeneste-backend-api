package no.unit.bibs.contents;

import no.unit.bibs.contents.exception.CommunicationException;
import no.unit.bibs.contents.exception.ParameterException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionTest {

    public static final String ERROR = "error";

    @Test
    public void testStatusCode() {
        ParameterException parameterException = new ParameterException(ERROR);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, parameterException.getStatusCode());

        CommunicationException ex = new CommunicationException(ERROR, parameterException);
        assertEquals(HttpStatus.SC_BAD_GATEWAY, ex.getStatusCode());
    }
}
