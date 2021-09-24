package no.unit.bibs.contents.exception;

import nva.commons.apigateway.exceptions.ApiGatewayException;

import java.net.HttpURLConnection;

public class CommunicationException extends ApiGatewayException {

    public CommunicationException(String message, Exception exception) {
        super(exception, message);
    }

    @Override
    protected Integer statusCode() {
        return HttpURLConnection.HTTP_BAD_GATEWAY;
    }
}
