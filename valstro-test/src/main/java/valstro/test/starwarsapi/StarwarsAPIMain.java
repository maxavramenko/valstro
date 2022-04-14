package valstro.test.starwarsapi;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.WebSocket;

public class StarwarsAPIMain {
	static String DEFAULT_API_URL = "http://localhost:3000";
	
	interface SearchResultListener {
		void onConnect(String status);
		void onDisconnect(String status);
		void onError(String status);
		void onSearchResult(JSONObject result);
		void onSearchEnded();
	}
	
	static class StarwarsSearcher {
		final URI apiUri;
		Socket socket;
		final CopyOnWriteArrayList<SearchResultListener> listeners = new CopyOnWriteArrayList<>();
		final ConcurrentHashMap<String,AtomicInteger> expectedResults = new ConcurrentHashMap<>();
		
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
						if(commitResponse(jsonObject) < 0) {
							fireError(jsonObject.getString("error"));
							fireSearchEnded();
						}
						else {
							fireSearchResult(jsonObject);
							if(expectedResults.isEmpty()) {
								fireSearchEnded();
							}
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
		
		public void doSearch(String name) {

			JSONObject query = new JSONObject();
			query.put("query", name);
			registerSearch(name);
			
			connect();
			socket.emit("search", query);
		}
		
		private void registerSearch(final String name) {
			if(name != null) {
				expectedResults.put(name, new AtomicInteger());
			}
		}
		
		private int commitResponse(final JSONObject response) {
			final Integer resultCount = (Integer) response.get("resultCount");
			if(resultCount != null && resultCount.intValue() == -1) {
				return -1;
			}
			
			final Integer page = (Integer) response.get("page");
			final String name = (String) response.get("name");
			String searchCriteria = null;
			for(Map.Entry<String, AtomicInteger> entry: expectedResults.entrySet()) {
				if(name.contains(entry.getKey())) {
					entry.getValue().incrementAndGet();
					searchCriteria = entry.getKey();
					break;
				}
			}
			
			if(resultCount.intValue() - page.intValue() == 0 && searchCriteria != null) {
				expectedResults.remove(searchCriteria);
			}
			
			return resultCount.intValue() - page.intValue();
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
		
		protected void fireSearchResult(JSONObject result) {
			listeners.forEach(event->event.onSearchResult(result));
		}
		
		protected void fireSearchEnded() {
			listeners.forEach(event->event.onSearchEnded());
		}
	
	};
	
    public static void main(String[] args) {

        StarwarsSearcher search = new StarwarsSearcher(DEFAULT_API_URL);
        search.registerListener(new SearchResultListener() {

			@Override
			public void onConnect(String status) {
				System.out.println("Connected: " + status);
			}

			@Override
			public void onDisconnect(String status) {
				System.out.println("Disconnected: " + status);
			}

			@Override
			public void onError(String status) {
				System.out.println("Error: " + status);
			}

			@Override
			public void onSearchResult(JSONObject result) {
				printSearchResult(result);
			}

			@Override
			public void onSearchEnded() {
				System.out.println("Enter Search Criteria:");
			}
        	
        });

        
        Scanner input = new Scanner(System.in);
        System.out.println("Enter Search Criteria:");
        while(true) {
        	final String nextInputStr = input.nextLine();
        	if("exit".equalsIgnoreCase(nextInputStr)) {
        		System.out.println("Shutting down");
        		System.exit(0);
        	}

        	String searchCriteria[] = nextInputStr.split(",");
        	for(String nextName : searchCriteria) {
            	search.doSearch(nextName);
            }		
        	
        	
        }
    }
    
    private static void printSearchResult(JSONObject result) {
    	System.out.println("--------------------------------------------------------------");
    	System.out.println("Name: " + result.getString("name"));
    	System.out.println("Films: " + result.getString("films"));
    	System.out.println("--------------------------------------------------------------");
    }
}
