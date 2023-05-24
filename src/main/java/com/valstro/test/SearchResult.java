package com.valstro.test;

public class SearchResult {
    final String name;
    final String films;
    final int index;
    final int totalCount;

    public SearchResult(final String name, final String films, final int index, final int totalCount) {
        this.name = name;
        this.films = films;
        this.index = index;
        this.totalCount = totalCount;
    }

    public String getName() {
        return name;
    }

    public String getFilms() {
        return films;
    }

    public int getIndex() {
        return index;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
