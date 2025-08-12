package no.unit.bibs.contents.document.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.bibs.contents.document.DocumentDto;
import no.unit.bibs.contents.document.DocumentService;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;

import static java.util.Objects.isNull;

public class FetchHandler extends ApiGatewayHandler<Void, DocumentDto> {
    public static final String ISBN = "isbn";
    public static final String MISSING_REQUIRED_QUERY_PARAMETER = "Missing required query parameter: ";
    private final DocumentService documentService;

    @JacocoGenerated
    public FetchHandler() {
        this(new DocumentService());
    }

    @JacocoGenerated
    public FetchHandler(DocumentService documentService) {
        super(Void.class, new Environment());
        this.documentService = documentService;
    }

    public FetchHandler(DocumentService documentService, Environment environment) {
        super(Void.class, environment);
        this.documentService = documentService;
    }


    @Override
    protected void validateRequest(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        if (isNull(requestInfo.getQueryParameter(ISBN))) {
            throw new BadRequestException(MISSING_REQUIRED_QUERY_PARAMETER + ISBN);
        }
    }

    @Override
    protected DocumentDto processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        return this.documentService.fetch(requestInfo.getQueryParameter(ISBN));
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, DocumentDto output) {
        return HttpURLConnection.HTTP_OK;
    }
}
