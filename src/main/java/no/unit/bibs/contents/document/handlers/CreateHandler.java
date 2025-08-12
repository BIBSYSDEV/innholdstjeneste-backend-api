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

public class CreateHandler extends ApiGatewayHandler<DocumentDto, DocumentDto> {

    DocumentService documentService;

    @JacocoGenerated
    public CreateHandler() {
        this(new DocumentService());
    }

    @JacocoGenerated
    public CreateHandler(DocumentService documentService) {
        super(DocumentDto.class, new Environment());
        this.documentService = documentService;
    }

    public CreateHandler(DocumentService documentService, Environment environment) {
        super(DocumentDto.class, environment);
        this.documentService = documentService;
    }

    @Override
    protected void validateRequest(DocumentDto input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        documentService.validateDocument(input);
    }


    @Override
    protected DocumentDto processInput(DocumentDto input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        return documentService.create(input);
    }


    @Override
    protected Integer getSuccessStatusCode(DocumentDto input, DocumentDto output) {
        return HttpURLConnection.HTTP_CREATED;
    }
}
