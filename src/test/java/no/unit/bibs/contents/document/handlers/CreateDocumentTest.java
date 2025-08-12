package no.unit.bibs.contents.document.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.bibs.contents.document.DocumentDao;
import no.unit.bibs.contents.document.DocumentDto;
import no.unit.bibs.contents.document.DocumentTestBase;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.time.Instant;

import static no.unit.bibs.contents.CreateContentsApiHandlerTest.CREATE_CONTENTS_EVENT;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CreateDocumentTest extends DocumentTestBase {

    private CreateHandler testHandler;

    @BeforeEach
    void setUp() throws JsonProcessingException, BadRequestException {
        init();
        testHandler = new CreateHandler(documentService, environment);
        var dto = dtoObjectMapper.readValue(RESOURCE_PATH, DocumentDto.class);
        when(mockedClient.create(any()))
            .thenReturn(dto.toDaoBuilder().created(Instant.now()).modified(Instant.now()).build());
    }


    @Test
    void getSuccessStatusCodeReturnsOK() {
        var statusCode = testHandler.getSuccessStatusCode(null, null);
        assertEquals(HttpURLConnection.HTTP_CREATED, statusCode);
    }

    @Test
    void handlerReturnsSearchResultsWhenQueryIsValid() throws ApiGatewayException, JsonProcessingException {
        var documentDto = dtoObjectMapper.readValue(
            stringFromResources(Path.of(CREATE_CONTENTS_EVENT)),
            DocumentDto.class
        );

        var actual = testHandler.processInput(documentDto, mockedRequestInfo, mockedContext);
        assertEquals(documentDto.isbn(), actual.isbn());
        assertEquals(documentDto.title(), actual.title());
        assertEquals(documentDto.author(), actual.author());
    }

    @Test
    void handlerReturnsSearchResultsWhenQueryIsValidAndCreatedAndModifiedAreNotNull() throws ApiGatewayException, JsonProcessingException {
        var documentDto = dtoObjectMapper.readValue(
            stringFromResources(Path.of(CREATE_CONTENTS_EVENT)),
            DocumentDto.class
        );

        var actual = testHandler.processInput(documentDto, mockedRequestInfo, mockedContext);
        assertNotEquals(null, actual.created());
        assertNotEquals(null, actual.modified());
    }

    @Test
    void handlerReturnsSearchResultsWhenQueryIsValidAndCreatedAndModifiedAreNotEqualToInput() throws ApiGatewayException, JsonProcessingException {
        var documentDto = dtoObjectMapper.readValue(
            stringFromResources(Path.of(CREATE_CONTENTS_EVENT)),
            DocumentDto.class
        );

        var actual = testHandler.processInput(documentDto, mockedRequestInfo, mockedContext);
        assertNotEquals(documentDto.created(), actual.created());
        assertNotEquals(documentDto.modified(), actual.modified());
    }

    @Test
    void validateRequestThrowsBadRequestExceptionWhenInputIsNull() {
        DocumentDto nullDocument = null;
        ApiGatewayException exception = null;
        try {
            testHandler.validateRequest(nullDocument, mockedRequestInfo, mockedContext);
        } catch (ApiGatewayException e) {
            exception = e;
        }
        assertNotEquals(null, exception);
    }

    @Test
    void validateRequestOK() throws JsonProcessingException {
        var documentDto = dtoObjectMapper.readValue(
            stringFromResources(Path.of(CREATE_CONTENTS_EVENT)),
            DocumentDto.class
        );
        assertDoesNotThrow(() -> testHandler.validateRequest(documentDto, mockedRequestInfo, mockedContext));
    }

    @Test
    void handlerReturnsSearchResultsWhenQueryIsSingleTerm() throws ApiGatewayException, JsonProcessingException {
        var documentDto = dtoObjectMapper.readValue(
            stringFromResources(Path.of(CREATE_CONTENTS_EVENT)),
            DocumentDto.class
        );

        var actual = testHandler.processInput(documentDto, mockedRequestInfo, mockedContext);
        assertNotEquals(documentDto.created(), actual.created());
        assertNotEquals(documentDto.modified(), actual.modified());
    }
}