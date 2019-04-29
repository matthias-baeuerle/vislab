package de.hska.iwi.vislab.lab2.example;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;

public class FibonacciTest {

    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() {
        server = Main.startServer();
        Client client = ClientBuilder.newClient();
        target = client.target(Main.BASE_URI);
    }

    @After
    public void tearDown() {
        server.shutdown();
    }

    @Test
    public void reset() {
        resetState();
        int current = requestCurrentFibonacci();
        assertEquals(1, current);
    }

    @Test
    public void increment() {
        resetState();

        int[] fibonaccis = new int[]{1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144};
        for (int fibonacci : fibonaccis) {
            assertEquals(fibonacci, requestNextFibonacci());
        }
    }

    private void resetState() {
        target.path("fibonacci").request().put(Entity.entity("", MediaType.TEXT_PLAIN));
    }

    private int requestCurrentFibonacci() {
        String fibonacciString = target.path("fibonacci").request().accept(MediaType.TEXT_PLAIN).get(String.class);
        return Integer.parseInt(fibonacciString);
    }

    private int requestNextFibonacci() {
        String fibonacciString = target.path("fibonacci").request().accept(MediaType.TEXT_PLAIN).post(null, String.class);
        return Integer.parseInt(fibonacciString);
    }

}
