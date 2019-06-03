package tcc;

import tcc.flight.FlightReservationDoc;
import tcc.hotel.HotelReservationDoc;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

public class AutomatedTestClient {

    private static final Entity<String> EMPTY_ENTITY = Entity.entity("", MediaType.TEXT_PLAIN);

    private final GregorianCalendar tomorrow;

    private final WebTarget flightTarget;
    private final WebTarget hotelTarget;

    public AutomatedTestClient() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestServer.BASE_URI);

        flightTarget = target.path("flight");
        hotelTarget = target.path("hotel");

        tomorrow = new GregorianCalendar();
        tomorrow.setTime(new Date());
        tomorrow.add(GregorianCalendar.DAY_OF_YEAR, 1);
    }

    public static void main(String[] args) {
        AutomatedTestClient client = new AutomatedTestClient();
        client.reserveFlightAndHotel();
    }

    public void reserveFlightAndHotel() {
        FlightReservationDoc flightReservationDoc = null;
        HotelReservationDoc hotelReservationDoc = null;
        try {
            flightReservationDoc = bookFlight();
            hotelReservationDoc = bookHotel();
            System.out.println("Flight and hotel booked");
        } catch (Exception ex) {
            rollbackWithRetry(flightReservationDoc, hotelReservationDoc);
            System.exit(1);
            return;
        }
        confirmFlightWithRetry(flightReservationDoc);
        confirmHotelWithRetry(hotelReservationDoc);
        System.out.println("Flight and hotel confirmed");
    }

    private void rollbackWithRetry(FlightReservationDoc flightReservationDoc, HotelReservationDoc hotelReservationDoc) {
        System.out.println("Rolling back flight and hotel");
        if (flightReservationDoc != null) {
            cancelFlightWithRetry(flightReservationDoc);
        }
        if (hotelReservationDoc != null) {
            cancelHotelWithRetry(hotelReservationDoc);
        }
    }

    private FlightReservationDoc bookFlight() throws IOException {
        FlightReservationDoc docFlight = new FlightReservationDoc();
        docFlight.setName("Christian");
        docFlight.setFrom("Karlsruhe");
        docFlight.setTo("Berlin");
        docFlight.setAirline("airberlin");
        docFlight.setDate(tomorrow.getTimeInMillis());

        Response responseFlight = flightTarget.request().accept(MediaType.APPLICATION_XML)
                .post(Entity.xml(docFlight));

        if (responseFlight.getStatus() != 200) {
            throw new IOException("Failed : HTTP error code : " + responseFlight.getStatus());
        }

        FlightReservationDoc outputFlight = responseFlight.readEntity(FlightReservationDoc.class);
        System.out.println("Output from Server: " + outputFlight);
        return outputFlight;
    }

    private HotelReservationDoc bookHotel() throws IOException {
        HotelReservationDoc docHotel = new HotelReservationDoc();
        docHotel.setName("Christian");
        docHotel.setHotel("Interconti");
        docHotel.setDate(tomorrow.getTimeInMillis());

        Response responseHotel = hotelTarget.request().accept(MediaType.APPLICATION_XML)
                .post(Entity.xml(docHotel));

        if (responseHotel.getStatus() != 200) {
            throw new IOException("Failed : HTTP error code : " + responseHotel.getStatus());
        }

        HotelReservationDoc outputHotel = responseHotel.readEntity(HotelReservationDoc.class);
        System.out.println("Output from Server: " + outputHotel);
        return outputHotel;
    }

    private void confirmFlightWithRetry(FlightReservationDoc flightReservationDoc) {
        boolean confirmed = false;
        while (!confirmed) {
            try {
                tryConfirmFlight(flightReservationDoc);
                confirmed = true;
                System.out.println("Flight confirmed");
            } catch (IOException ex) {
                System.err.println("Confirming flight failed: " + ex.getMessage());
                sleep();
            }
        }
    }

    private void tryConfirmFlight(FlightReservationDoc flightReservationDoc) throws IOException {
        Response response = flightTarget.path(flightReservationDoc.getId())
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .put(EMPTY_ENTITY);
        if (response.getStatus() != 200) {
            throw new IOException("Failed : HTTP error code : " + response.getStatus());
        }
    }

    private void confirmHotelWithRetry(HotelReservationDoc hotelReservationDoc) {
        boolean confirmed = false;
        while (!confirmed) {
            try {
                tryConfirmHotel(hotelReservationDoc);
                confirmed = true;
                System.out.println("Hotel confirmed");
            } catch (IOException ex) {
                System.err.println("Confirming hotel failed: " + ex.getMessage());
                sleep();
            }
        }
    }

    private void tryConfirmHotel(HotelReservationDoc hotelReservationDoc) throws IOException {
        Response response = hotelTarget.path(hotelReservationDoc.getId())
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .put(EMPTY_ENTITY);
        if (response.getStatus() != 200) {
            throw new IOException("Failed : HTTP error code : " + response.getStatus());
        }
    }

    private void cancelFlightWithRetry(FlightReservationDoc flightReservationDoc) {
        boolean cancelled = false;
        while (!cancelled) {
            try {
                tryCancelFlight(flightReservationDoc);
                cancelled = true;
                System.out.println("Flight cancelled");
            } catch (IOException ex) {
                System.err.println("Cancelling flight failed: " + ex.getMessage());
                sleep();
            }
        }
    }

    private void tryCancelFlight(FlightReservationDoc flightReservationDoc) throws IOException {
        Response response = flightTarget
                .path(flightReservationDoc.getId())
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .delete();

        if (response.getStatus() != 200) {
            throw new IOException("Failed : HTTP error code : " + response.getStatus());
        }
    }

    private void cancelHotelWithRetry(HotelReservationDoc hotelReservationDoc) {
        boolean cancelled = false;
        while (!cancelled) {
            try {
                tryCancelHotel(hotelReservationDoc);
                cancelled = true;
                System.out.println("Hotel cancelled");
            } catch (IOException ex) {
                System.err.println("Cancelling hotel failed: " + ex.getMessage());
                sleep();
            }
        }
    }

    private void tryCancelHotel(HotelReservationDoc hotelReservationDoc) throws IOException {
        Response response = hotelTarget
                .path(hotelReservationDoc.getId())
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .delete();

        if (response.getStatus() != 200) {
            throw new IOException("Failed : HTTP error code : " + response.getStatus());
        }
    }

    private void sleep() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException ignored) {
        }
    }

}
