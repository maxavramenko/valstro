package com.valstro.test;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.WebSocket;
import org.json.JSONObject;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

class StarwarsSearcher {
    final static String NOT_FOUND_PREFIX = "No valid matches retrieved for query '";
    final URI apiUri;
    Socket socket;
    final CopyOnWriteArrayList<SearchResultListener> listeners = new CopyOnWriteArrayList<>();
    final HashSet<String> expectedResults = new HashSet<>();

    StarwarsSearcher(final String apiUriStr) {
        this.apiUri = URI.create(apiUriStr);
    }

    public void registerListener(SearchResultListener listener) {
        if(listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(SearchResultListener listener) {
        if(listener != null) {
            listeners.remove(listener);
        }
    }

    public synchronized StarwarsSearcher connect() {
        if(socket == null) {
            IO.Options options = IO.Options.builder()
                    .setAuth(null)
                    .setTransports(new String[] {WebSocket.NAME})
                    .build();

            socket = IO.socket(apiUri, options);
            socket.on("connect", status -> {
                fireConnected(apiUri.toString());
            });

            socket.on("disconnect", status -> {
                fireDisconnected(Arrays.toString(status));
            });

            socket.on("search", results -> {
                Object [] jsonResults = (Object []) results;

                for(Object nextResult: jsonResults) {
                    final JSONObject jsonObject = (JSONObject) nextResult;
                    commitResponse(jsonObject);
                    if(expectedResults.isEmpty()) {
                        fireSearchEnded();
                    }
                }

            });

            socket.connect();
        }

        return this;
    }

    public synchronized StarwarsSearcher disconnect() {
        if(socket != null) {
            socket.close();
            socket = null;
        }

        return this;
    }

    public void doSearch(final String name) {

        JSONObject query = new JSONObject();
        query.put("query", name);
        registerSearch(name);

        connect();
        socket.emit("search", query);
    }

    private void registerSearch(final String name) {
        if(name != null) {
            expectedResults.add(name);
        }
    }

    private int commitResponse(final JSONObject response) {
        final int resultCount = response.getInt("resultCount");
        if(resultCount == -1) {
            commitError(response);
            return -1;
        }

        final int page = response.getInt("page");
        final String name = (String) response.get("name");

        commitSearchResult(resultCount - page,name);
        fireSearchResult(response);
        return resultCount - page;
    }

    private void commitError(final JSONObject response) {
        final String error = response.getString("error");
        int notFoundIdx = error.indexOf(NOT_FOUND_PREFIX);
        if(notFoundIdx >=0) {
            final String name = error.substring(notFoundIdx+NOT_FOUND_PREFIX.length(),error.lastIndexOf("'"));
            commitSearchResult(-1,name);
            fireNotFound(error);
        }
        else {
            fireError(error);
        }
    }

    private boolean commitSearchResult(int remaining, final String name) {
        if(remaining>0) {
            return false;
        }

        String searchCriteria = null;
        for(String entry: expectedResults) {
            if(name.toLowerCase().contains(entry.toLowerCase())) {
                searchCriteria = entry;
                break;
            }
        }

        if(searchCriteria != null) {
            expectedResults.remove(searchCriteria);
            return true;
        }

        return false;
    }
    protected void fireConnected(final String status) {
        listeners.forEach(event->event.onConnect(status));
    }

    protected void fireDisconnected(final String status) {
        listeners.forEach(event->event.onDisconnect(status));
    }

    protected void fireError(final String status) {
        listeners.forEach(event->event.onError(status));
    }

    protected void fireNotFound(final String status) {
        listeners.forEach(event->event.onNotFound(status));
    }

    protected void fireSearchResult(JSONObject result) {
        listeners.forEach(event->event.onSearchResult(
                new SearchResult(result.getString("name"),
                        result.getString("films"),
                        result.getInt("page"),
                        result.getInt("resultCount"))));
    }

    protected void fireSearchEnded() {
        listeners.forEach(event->event.onSearchEnded());
    }

};
