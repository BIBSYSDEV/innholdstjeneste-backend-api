package no.unit.bibs.contents;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import nva.commons.exceptions.GatewayResponseSerializingException;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.StringUtils;
import org.apache.http.HttpStatus;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static nva.commons.handlers.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;

/**
 * POJO containing response object for API Gateway.
 */
public class GatewayResponse {

    public static final String CORS_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    public static final String EMPTY_JSON = "{}";
    public static final transient String ERROR_KEY = "error";
    private String body;
    private transient Map<String, String> headers;
    private int statusCode;

    /**
     * GatewayResponse contains response status, response headers and body with payload resp. error messages.
     */
    @JacocoGenerated
    public GatewayResponse() {
        this(new Environment(), EMPTY_JSON, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    public GatewayResponse(Environment environment) {
        this(environment, EMPTY_JSON, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    public GatewayResponse(Environment environment, String location) {
        this.body = EMPTY_JSON;
        this.generateDefaultHeadersWithLocation(environment, location);
    }

    /**
     * GatewayResponse convenience constructor to set response status and body with payload direct.
     *
     */
    public GatewayResponse(Environment environment, final String body, final int status) {
        this.statusCode = status;
        this.body = body;
        this.generateDefaultHeaders(environment);
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setStatusCode(int status) {
        this.statusCode = status;
    }

    /**
     * Set error message as a json string to body.
     *
     * @param message message from exception
     * @throws GatewayResponseSerializingException some parsing went wrong
     */
    public void setErrorBody(String message) throws GatewayResponseSerializingException {
        Map<String, String> map = new HashMap<>();
        map.put(ERROR_KEY, message);
        try {
            this.body = Jackson.getObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new GatewayResponseSerializingException(e);
        }
    }

    private void generateDefaultHeaders(Environment environment) {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        final String corsAllowDomain = environment.readEnv(ALLOWED_ORIGIN_ENV);
        if (StringUtils.isNotEmpty(corsAllowDomain)) {
            headers.put(CORS_ALLOW_ORIGIN_HEADER, corsAllowDomain);
        }
        headers.put("Access-Control-Allow-Methods", "OPTIONS,GET");
        headers.put("Access-Control-Allow-Credentials", "true");
        headers.put("Access-Control-Allow-Headers", HttpHeaders.CONTENT_TYPE);
        this.headers = Map.copyOf(headers);
    }

    private void generateDefaultHeadersWithLocation(Environment environment, String location) {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        final String corsAllowDomain = environment.readEnv(ALLOWED_ORIGIN_ENV);
        if (StringUtils.isNotEmpty(corsAllowDomain)) {
            headers.put(CORS_ALLOW_ORIGIN_HEADER, corsAllowDomain);
        }
        headers.put("Access-Control-Allow-Methods", "OPTIONS,GET");
        headers.put("Access-Control-Allow-Credentials", "true");
        headers.put("Access-Control-Allow-Headers", HttpHeaders.CONTENT_TYPE);
        headers.put(HttpHeaders.LOCATION, location);
        this.headers = Map.copyOf(headers);
    }

}
