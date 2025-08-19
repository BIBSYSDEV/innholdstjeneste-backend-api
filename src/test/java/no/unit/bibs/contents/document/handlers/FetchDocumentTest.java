package no.unit.bibs.contents.document.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.bibs.contents.document.DocumentDto;
import no.unit.bibs.contents.document.DocumentTestBase;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.nio.file.Path;

import static no.unit.bibs.contents.StorageClientTest.CREATE_CONTENTS_EVENT;
import static no.unit.bibs.contents.document.handlers.FetchHandler.ISBN_PARAM_NAME;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FetchDocumentTest extends DocumentTestBase {

    private FetchHandler testHandler;

    @BeforeEach
    void setUp() throws JsonProcessingException, BadRequestException {
        init();

        testHandler = new FetchHandler(documentService, environment);
        when(mockedClient.getByIsbn(any()))
            .thenReturn(dtoObjectMapper.readValue(RESOURCE_PATH, DocumentDto.class).toDaoBuilder().build());
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        var statusCode = testHandler.getSuccessStatusCode(null, null);
        assertEquals(HttpURLConnection.HTTP_OK, statusCode);
    }

    @Test
    void validateRequestThrowsBadRequestExceptionWhenInputIsNull() throws BadRequestException {
        var mockedInfo = mock(RequestInfo.class);
        when(mockedInfo.getQueryParameter(ISBN_PARAM_NAME)).thenThrow(BadRequestException.class);
        assertThrows(BadRequestException.class,
            () -> testHandler.validateRequest(null, mockedInfo, mockedContext));
    }

    @Test
    void validateOKWhenQueryIsValid() {
        assertDoesNotThrow(
            () -> testHandler.validateRequest(null, mockedRequestInfo, mockedContext)
        );
    }

    @Test
    void handlerReturnsDocumentWhenQueryIsValid() throws JsonProcessingException, ApiGatewayException {
        var documentDto = dtoObjectMapper.readValue(
            stringFromResources(Path.of(CREATE_CONTENTS_EVENT)),
            DocumentDto.class
        );

        var actual = testHandler.processInput(null, mockedRequestInfo, mockedContext);
        assertEquals(documentDto.isbn(), actual.isbn());
        assertEquals(documentDto.title(), actual.title());
        assertEquals(documentDto.author(), actual.author());
    }

}