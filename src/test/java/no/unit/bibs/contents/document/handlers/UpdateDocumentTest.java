package no.unit.bibs.contents.document.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.bibs.contents.document.DocumentDao;
import no.unit.bibs.contents.document.DocumentDto;
import no.unit.bibs.contents.document.DocumentRequest;
import no.unit.bibs.contents.document.DocumentTestBase;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;

import static no.unit.bibs.contents.CreateContentsApiHandlerTest.CREATE_CONTENTS_EVENT;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UpdateDocumentTest extends DocumentTestBase {

    private UpdateHandler testHandler;

    @BeforeEach
    void setUp() throws JsonProcessingException, BadRequestException {
        init();
        testHandler = new UpdateHandler(documentService, environment);
        var dto = dtoObjectMapper.readValue(RESOURCE_PATH, DocumentDto.class);
        when(mockedClient.update(any()))
            .thenReturn(dto.toDaoBuilder().modified(Instant.now()).build());
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        var statusCode = testHandler.getSuccessStatusCode(null, null);
        assertEquals(200, statusCode);
    }

    @Test
    void validateRequestThrowsBadRequestExceptionWhenInputIsNull() {
        assertThrows(BadRequestException.class, () -> testHandler.validateRequest(new DocumentRequest(null), mockedRequestInfo, mockedContext));
    }

    @Test
    void validateRequestThrowsExceptionWhenQueryIsInvalid() {
        var invalidDocumentDto =  DocumentDao.builder()
            .isbn("invalid-isbn")
            .title("Test Title")
            .author("Test Author")
            .build().toDto();

        assertThrows(BadRequestException.class, () ->
            testHandler.validateRequest(new DocumentRequest(invalidDocumentDto), mockedRequestInfo, mockedContext));
    }

    @Test
    void validateOKWhenQueryIsValid() throws JsonProcessingException, ApiGatewayException {
        var documentDto = dtoObjectMapper.readValue(
            stringFromResources(Path.of(CREATE_CONTENTS_EVENT)),
            DocumentDto.class
        );
        testHandler.validateRequest(new DocumentRequest(documentDto), mockedRequestInfo, mockedContext);
    }

    @Test
    void handlerReturnsUpdatedDocumentWhenQueryIsValid() throws JsonProcessingException, ApiGatewayException {
        var documentDto = dtoObjectMapper.readValue(
            stringFromResources(Path.of(CREATE_CONTENTS_EVENT)),
            DocumentDto.class
        );

        var actual = testHandler.processInput(new DocumentRequest(documentDto), mockedRequestInfo, mockedContext);
        assertEquals(documentDto.isbn(), actual.isbn());
        assertEquals(documentDto.title(), actual.title());
        assertEquals(documentDto.author(), actual.author());
        assertNotNull(actual.modified());
    }

}