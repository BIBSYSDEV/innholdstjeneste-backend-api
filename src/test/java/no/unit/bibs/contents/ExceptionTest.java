package no.unit.bibs.contents;

import no.unit.bibs.contents.exception.CommunicationException;
import no.unit.bibs.contents.exception.ParameterException;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionTest {

    public static final String ERROR = "error";

    @Test
    public void testStatusCode() {
        ParameterException parameterException = new ParameterException(ERROR);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, parameterException.getStatusCode());

        CommunicationException ex = new CommunicationException(ERROR, parameterException);
        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, ex.getStatusCode());
    }
}
