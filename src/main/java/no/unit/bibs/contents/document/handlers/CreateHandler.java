package no.unit.bibs.contents.document.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.bibs.contents.document.DocumentDto;
import no.unit.bibs.contents.document.DocumentRequest;
import no.unit.bibs.contents.document.DocumentService;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;

public class CreateHandler extends ApiGatewayHandler<DocumentRequest, DocumentDto> {

    DocumentService documentService;

    @JacocoGenerated
    public CreateHandler() {
        this(new DocumentService(), new Environment());
    }

    public CreateHandler(DocumentService documentService, Environment environment) {
        super(DocumentRequest.class, environment);
        this.documentService = documentService;
    }

    @Override
    protected void validateRequest(DocumentRequest input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        documentService.validateDocument(input.contents());
    }

    @Override
    protected DocumentDto processInput(DocumentRequest input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        return documentService.create(input.contents());
    }

    @Override
    protected Integer getSuccessStatusCode(DocumentRequest input, DocumentDto output) {
        return HttpURLConnection.HTTP_CREATED;
    }
}
