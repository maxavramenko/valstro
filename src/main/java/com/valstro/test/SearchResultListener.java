package com.valstro.test;

public interface SearchResultListener {
    void onConnect(String status);
    void onDisconnect(String status);
    void onError(String status);
    void onSearchResult(SearchResult result);

    void onNotFound(String status);
    void onSearchEnded();
}
