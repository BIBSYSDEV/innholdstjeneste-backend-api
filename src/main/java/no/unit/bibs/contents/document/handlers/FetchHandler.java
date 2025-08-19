package no.unit.bibs.contents.document.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.bibs.contents.document.DocumentDto;
import no.unit.bibs.contents.document.DocumentService;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;

public class FetchHandler extends ApiGatewayHandler<Void, DocumentDto> {
    public static final String ISBN = "isbn";
    private final DocumentService documentService;

    @JacocoGenerated
    public FetchHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public FetchHandler(Environment environment) {
        this(new DocumentService(environment), environment);
    }

    public FetchHandler(DocumentService documentService, Environment environment) {
        super(Void.class, environment);
        this.documentService = documentService;
    }

    @Override
    protected void validateRequest(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        requestInfo.getQueryParameter(ISBN);
    }

    @Override
    protected DocumentDto processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        return documentService.fetch(requestInfo.getQueryParameter(ISBN));
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, DocumentDto output) {
        return HttpURLConnection.HTTP_OK;
    }
}
