package de.hska.iwi.vislab.lab2.example;

import org.glassfish.grizzly.http.server.Request;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Path("fibonacci")
public class FibonacciResource {

    private static final ConcurrentMap<String, ClientState> clientStateMap = new ConcurrentHashMap<>();

    @Context private javax.inject.Provider<Request> grizzlyRequestProvider;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getCurrent() {
        Request grizzlyRequest = grizzlyRequestProvider.get();
        String remoteAddress = grizzlyRequest.getRemoteAddr();
        int current = clientStateMap.computeIfAbsent(remoteAddress, ClientState::new).getCurrent();
        return String.valueOf(current);
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String getAndUpdate() {
        Request grizzlyRequest = grizzlyRequestProvider.get();
        String remoteAddress = grizzlyRequest.getRemoteAddr();
        int nextFibonacci = clientStateMap.computeIfAbsent(remoteAddress, ClientState::new).getAndUpdate();
        return String.valueOf(nextFibonacci);
    }

    @PUT
    public void resetState() {
        Request grizzlyRequest = grizzlyRequestProvider.get();
        String remoteAddress = grizzlyRequest.getRemoteAddr();
        clientStateMap.computeIfAbsent(remoteAddress, ClientState::new).reset();
    }

    private static class ClientState {

        private int previous;
        private int current;

        public ClientState(String remoteAddress) {
            reset();
        }

        public int getCurrent() {
            return current;
        }

        public void reset() {
            previous = 0;
            current = 1;
        }

        public synchronized int getAndUpdate() {
            int returnValue = current;
            int next = previous + current;
            previous = current;
            current = next;
            return returnValue;
        }

    }

}
