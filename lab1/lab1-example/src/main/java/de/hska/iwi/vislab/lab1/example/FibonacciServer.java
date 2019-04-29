package de.hska.iwi.vislab.lab1.example;

import de.hska.iwi.vislab.lab1.example.ws.FibonacciServiceImpl;

import javax.swing.JOptionPane;
import javax.xml.ws.Endpoint;

public class FibonacciServer {
    public static void main(final String[] args) {
        String url = (args.length > 0) ? args[0]
                : "http://localhost:4434/fibonacciservice";
        Endpoint ep = Endpoint.publish(url, new FibonacciServiceImpl());
        JOptionPane.showMessageDialog(null, "FibonacciServer beenden");
        ep.stop();
    }
}
