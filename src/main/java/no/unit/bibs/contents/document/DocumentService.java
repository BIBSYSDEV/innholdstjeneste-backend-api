package no.unit.bibs.contents.document;

import no.unit.bibs.contents.StorageClient;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.time.Instant;

import static java.util.Objects.isNull;


public class DocumentService {
    private final DocumentClient client;
    private final StorageClient storageClient;

    @JacocoGenerated
    public DocumentService() {
        this(new Environment());
    }

    @JacocoGenerated
    public DocumentService(Environment environment) {
        this(new DocumentClient(environment), new StorageClient(environment));
    }

    /**
     * Constructor for DocumentService.
     *
     * @param dbClient      the DocumentClient to interact with the database
     * @param storageClient the StorageClient to handle file storage operations
     */
    public DocumentService(DocumentClient dbClient, StorageClient storageClient) {
        this.client = dbClient;
        this.storageClient = storageClient;
    }

    public DocumentDto fetch(String isbn) {
        return client
            .getByIsbn(isbn)
            .toDto();
    }

    public DocumentDto update(DocumentDto dto) throws BadRequestException {
        validateDocument(dto);      // this is typically called separately before update, but we cannot assume it.
        var dao = storageClient
            .handleFiles(dto)
            .modified(Instant.now())
            .build();
        return client
            .update(dao)
            .toDto();
    }

    public DocumentDto create(DocumentDto dto) throws BadRequestException {
        validateDocument(dto);      // this is typically called separately before update, but we cannot assume it.
        var timeStamp = Instant.now();
        var dao = storageClient
            .handleFiles(dto)
            .created(timeStamp)
            .modified(timeStamp)
            .build();
        return client
            .create(dao)
            .toDto();
    }

    public void validateDocument(DocumentDto document) throws BadRequestException {
        if (isNull(document) || !document.isValid()) {
            throw new BadRequestException("Document is not valid: " + document);
        }
    }

}
