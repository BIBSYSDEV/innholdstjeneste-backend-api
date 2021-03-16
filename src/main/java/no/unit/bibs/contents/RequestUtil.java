package no.unit.bibs.contents;

import nva.commons.handlers.RequestInfo;
import nva.commons.utils.JacocoGenerated;
import org.elasticsearch.search.sort.SortOrder;

@JacocoGenerated
public class RequestUtil {

    public static final String SEARCH_TERM_KEY = "query";
    public static final String RESULTS_KEY = "results";
    public static final String FROM_KEY = "from";
    public static final String ORDERBY_KEY = "orderBy";
    public static final String SORTORDER_KEY = "sortOrder";
    private static final String RESULTS_DEFAULT_SIZE = "10";
    public static final String SEARCH_ALL_PUBLICATIONS_DEFAULT_QUERY = "*";
    private static final String ORDERBY_DEFAULT_POSITION = "modifiedDate";
    private static final String DEFAULT_SORT_ORDER = SortOrder.DESC.name();
    private static final String FROM_DEFAULT_POSITION = "0";


    /**
     * Get searchTerm from request query parameters.
     *
     * @param requestInfo requestInfo
     * @return searchTerm given in query parameter
     */
    public static String getSearchTerm(RequestInfo requestInfo) {
        return requestInfo.getQueryParameters().getOrDefault(SEARCH_TERM_KEY, SEARCH_ALL_PUBLICATIONS_DEFAULT_QUERY);
    }

    public static int getResults(RequestInfo requestInfo) {
        return Integer.parseInt(requestInfo.getQueryParameters().getOrDefault(RESULTS_KEY, RESULTS_DEFAULT_SIZE));
    }

    public static int getFrom(RequestInfo requestInfo) {
        return Integer.parseInt(requestInfo.getQueryParameters().getOrDefault(FROM_KEY, FROM_DEFAULT_POSITION));
    }

    public static String getOrderBy(RequestInfo requestInfo) {
        return requestInfo.getQueryParameters().getOrDefault(ORDERBY_KEY, ORDERBY_DEFAULT_POSITION);
    }

    public static SortOrder getSortOrder(RequestInfo requestInfo) {
        return SortOrder.fromString(requestInfo.getQueryParameters().getOrDefault(SORTORDER_KEY, DEFAULT_SORT_ORDER));
    }

}
