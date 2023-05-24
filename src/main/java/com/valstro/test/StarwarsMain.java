package com.valstro.test;

import java.util.Calendar;
import java.util.Scanner;

public class StarwarsMain {
    static String DEFAULT_API_URL = "http://localhost:3000";

    public static void main(String[] args) {

        printUsage();

        StarwarsSearcher search = new StarwarsSearcher(DEFAULT_API_URL);
        search.registerListener(new SearchResultListener() {

            @Override
            public void onConnect(String status) {
                System.out.println("StarwarsAPI: Connected: " + status);
            }

            @Override
            public void onDisconnect(String status) {
                System.out.println("StarwarsAPI: Disconnected: " + status);
            }

            @Override
            public void onError(String status) {
                System.out.println(status);
            }

            @Override
            public void onNotFound(String status) {
                System.out.println(status);
            }

            @Override
            public void onSearchResult(SearchResult result) {
                printSearchResult(result);
            }

            @Override
            public void onSearchEnded() {
                printPrompt();
            }

        });


        Scanner input = new Scanner(System.in);
        printPrompt();

        while(true) {
            final String nextInputStr = input.nextLine();
            if("exit".equalsIgnoreCase(nextInputStr)) {
                System.out.println("StarwarsAPI: Shutting down");
                search.disconnect();
                System.exit(0);
            }

            String searchCriteria[] = nextInputStr.split(",");
            for(String nextName : searchCriteria) {
                if(nextName.trim().length() > 0) {
                    search.doSearch(nextName.trim());
                }
            }

        }
    }

    static void printPrompt(){
        System.out.println("\nEnter Starwars character search criteria:");
    }
    private static void printSearchResult(final SearchResult result) {
        System.out.println(String.format("(%d/%d) %s - [%s]",result.getIndex(),result.getTotalCount(),result.getName(),result.getFilms()));
    }

    private static void printUsage() {
        System.out.println("\nStarwars API started at " + Calendar.getInstance().getTime()+"\n");

        System.out.println("Usage:");
        System.out.println("Enter Starwars character names at the prompt");
        System.out.println("You can use partial name search");
        System.out.println("Use a comma to enter multiple names");
        System.out.println("Type 'exit' to quit the application\n\n");
    }
}