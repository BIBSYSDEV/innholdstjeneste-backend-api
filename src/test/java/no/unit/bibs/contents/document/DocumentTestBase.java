package no.unit.bibs.contents.document;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.bibs.contents.StorageClient;
import no.unit.bibs.contents.document.handlers.CreateHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;

import java.nio.file.Path;
import java.util.Map;

import static no.unit.bibs.contents.CreateContentsApiHandlerTest.CREATE_CONTENTS_EVENT;
import static no.unit.bibs.contents.CreateContentsApiHandlerTest.TEST_ISBN;
import static no.unit.bibs.contents.document.handlers.FetchHandler.ISBN;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DocumentTestBase {
    protected final String RESOURCE_PATH = stringFromResources(Path.of(CREATE_CONTENTS_EVENT));

    protected Context mockedContext;
    protected RequestInfo mockedRequestInfo;
    protected StorageClient mockedStorageClient;
    protected DocumentClient mockedClient;
    protected DocumentService documentService;
    protected Environment environment;

    protected void init() throws JsonProcessingException, BadRequestException {
        mockedContext = mock(Context.class);
        mockedRequestInfo = mock(RequestInfo.class);
        when(mockedRequestInfo.getQueryParameters()).thenReturn(Map.of(ISBN, TEST_ISBN));
        when(mockedRequestInfo.getQueryParameter(ISBN)).thenReturn(TEST_ISBN);

        var dto = dtoObjectMapper.readValue(RESOURCE_PATH, DocumentDto.class);

        mockedStorageClient = mock(StorageClient.class);
        when(mockedStorageClient.handleFiles(any(DocumentDto.class)))
            .thenReturn(dto.toDaoBuilder());

        mockedClient = mock(DocumentClient.class);
        documentService = new DocumentService(mockedClient, mockedStorageClient);

        environment = mock(Environment.class); // Mock environment if needed
        when(environment.readEnv(ALLOWED_ORIGIN_ENV))
            .thenReturn("*");
        when(environment.readEnv("TABLE_NAME"))
            .thenReturn("TEST_TABLE_NAME");
        when(environment.readEnv("S3_BUCKET_NAME_ENV"))
            .thenReturn("TEST_BUCKET_NAME");
        when(environment.readEnv("COGNITO_AUTHORIZER_URLS"))
            .thenReturn("https://test.cognito.auth.url");
        when(environment.readEnv("COGNITO_AUTHORIZER_NAME"))
            .thenReturn("testCognitoAuthorizerName");

    }
}
