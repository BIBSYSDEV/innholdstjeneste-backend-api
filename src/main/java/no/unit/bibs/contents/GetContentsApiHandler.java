package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.GatewayResponseSerializingException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;

public class GetContentsApiHandler extends ApiGatewayHandler<Void, ContentsDocument> {

    public static final String ISBN = "isbn";
    private final DynamoDBClient dynamoDBClient;

    @JacocoGenerated
    public GetContentsApiHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public GetContentsApiHandler(Environment environment) {
        this(environment, new DynamoDBClient(environment));
    }

    public GetContentsApiHandler(Environment environment, DynamoDBClient dynamoDBClient) {
        super(Void.class, environment);
        this.dynamoDBClient = dynamoDBClient;
    }

    /**
     * Implements the main logic of the handler. Any exception thrown by this method will be handled by method.
     *
     * @param input       The input object to the method. Usually a deserialized json.
     * @param requestInfo Request headers and path.
     * @param context     the ApiGateway context.
     * @return the Response body that is going to be serialized in json
     */
    @Override
    protected ContentsDocument processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        String isbn = requestInfo.getQueryParameter(ISBN);
        System.out.println("ISBN from queryParam: " + isbn);
        String contents = dynamoDBClient.getContents(isbn);
        System.out.println("contents from DynamoDB: " + contents);
        try {
            ContentsDocument response = dtoObjectMapper.readValue(contents, ContentsDocument.class);
            System.out.println("Laget en ResponseObject :" + response.toString());
            return response;
        } catch (JsonProcessingException e) {
            System.out.println("final exception: " + e.getMessage());
            throw new GatewayResponseSerializingException(e);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, ContentsDocument output) {
        return HttpURLConnection.HTTP_OK;
    }

}
