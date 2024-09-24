package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;

public class BookingTest {
    private static final String BASE_URL = "https://restful-booker.herokuapp.com/booking";
    private static final List<Integer> bookingIds = new ArrayList<>();
    private static String AUTH_TOKEN;

    @BeforeAll
    public static void setUp() {
        AUTH_TOKEN = getAuthToken();
    }

    @Test
    void testCreateBooking() {
        for (int i = 0; i < 10; i++) {
            createBooking("Jim" + i, "Brown" + i, 111 + i, "2018-01-01", "2019-01-01");
        }
    }

    @Test
    void testGetBooking() {
        for (int bookingId : bookingIds) {
            getBookingById(bookingId);
        }
    }

    @Test
    void testUpdateBooking() {
        for (int bookingId : bookingIds) {
            updateBooking(bookingId);
        }
    }

    @Test
    void testDeleteBooking() {
        for (int bookingId : bookingIds) {
            deleteBooking(bookingId);
        }
    }

    private static String getAuthToken() {
        String authUrl = "https://restful-booker.herokuapp.com/auth";

        String body = "{\"username\" : \"admin\", \"password\" : \"password123\"}";

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(body)
                .when()
                .post(authUrl);

        Assertions.assertEquals(200, response.statusCode(), "Failed to authenticate");
        return response.jsonPath().getString("token");
    }

    private void createBooking(String firstName, String lastName, int totalPrice, String checkin, String checkout) {
        Booking booking = createBookingObject(firstName, lastName, totalPrice, checkin, checkout);

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(booking)
                .when()
                .post(BASE_URL);

        Assertions.assertEquals(200, response.statusCode(), "Booking creation failed");

        // Extract bookingId from the response directly
        int bookingId = response.jsonPath().getInt("bookingid");
        Assertions.assertNotNull(bookingId, "Booking ID should not be null");

        Assertions.assertEquals(firstName, response.jsonPath().getString("booking.firstname"), "First name does not match");
        Assertions.assertEquals(lastName, response.jsonPath().getString("booking.lastname"), "Last name does not match");
        Assertions.assertEquals(totalPrice, response.jsonPath().getInt("booking.totalprice"), "Total price does not match");
        Assertions.assertTrue(response.jsonPath().getBoolean("booking.depositpaid"), "Deposit paid should be true");

        bookingIds.add(bookingId);
        System.out.println("Booking created with ID: " + bookingId);
    }

    private Booking createBookingObject(String firstName, String lastName, int totalPrice, String checkin, String checkout) {
        Booking booking = new Booking();
        booking.setFirstname(firstName);
        booking.setLastname(lastName);
        booking.setTotalprice(totalPrice);
        booking.setDepositpaid(true);

        BookingDates bookingDates = new BookingDates();
        bookingDates.setCheckin(checkin);
        bookingDates.setCheckout(checkout);
        booking.setBookingDates(bookingDates);
        booking.setAdditionalneeds("Breakfast");

        return booking;
    }

    private Booking getBookingById(int bookingId) {
        Response response = RestAssured.get(BASE_URL + "/" + bookingId);
        Assertions.assertEquals(200, response.statusCode(), "Failed to retrieve booking");

        Booking booking = response.as(Booking.class); // Deserialize directly to Booking
        Assertions.assertNotNull(booking, "Booking should not be null");

        System.out.println("Retrieved Booking ID: " + bookingId);
        System.out.println("Booking Details: " + booking);

        return booking;
    }

    private void updateBooking(int bookingId) {
        Booking updatedBooking = new Booking();
        updatedBooking.setFirstname("UpdatedJim");
        updatedBooking.setLastname("UpdatedBrown");
        updatedBooking.setTotalprice(222);
        updatedBooking.setDepositpaid(false);

        BookingDates updatedBookingDates = new BookingDates();
        updatedBookingDates.setCheckin("2020-01-01");
        updatedBookingDates.setCheckout("2021-01-01");
        updatedBooking.setBookingDates(updatedBookingDates);
        updatedBooking.setAdditionalneeds("Dinner");

        Response response = RestAssured.given()
                .contentType("application/json")
                .accept("application/json")
                .header("Cookie", "token=" + AUTH_TOKEN)
                .body(updatedBooking)
                .when()
                .put(BASE_URL + "/" + bookingId);

        Assertions.assertEquals(200, response.statusCode(), "Update failed");

        Booking retrievedBooking = getBookingById(bookingId);

        Assertions.assertEquals("UpdatedJim", retrievedBooking.getFirstname(), "First name should be updated");
        Assertions.assertEquals("UpdatedBrown", retrievedBooking.getLastname(), "Last name should be updated");
        Assertions.assertEquals(222, retrievedBooking.getTotalprice(), "Total price should be updated");
        Assertions.assertFalse(retrievedBooking.isDepositpaid(), "Deposit paid should be updated to false");
        Assertions.assertEquals("Dinner", retrievedBooking.getAdditionalneeds(), "Additional needs should be updated");
        Assertions.assertEquals("2020-01-01", retrievedBooking.getBookingDates().getCheckin(), "Check-in date should be updated");
        Assertions.assertEquals("2021-01-01", retrievedBooking.getBookingDates().getCheckout(), "Check-out date should be updated");
        System.out.println("Booking updated with ID: " + bookingId);
    }

    private void deleteBooking(int bookingId) {
        Response response = RestAssured.given()
                .contentType("application/json")
                .header("Cookie", "token=" + AUTH_TOKEN)
                .when()
                .delete(BASE_URL + "/" + bookingId);

        Assertions.assertEquals(201, response.statusCode(), "Delete operation failed");

        Response checkResponse = RestAssured.get(BASE_URL + "/" + bookingId);
        Assertions.assertEquals(404, checkResponse.statusCode(), "Expected status code 404, booking should be deleted");
        System.out.println("Booking deleted with ID: " + bookingId);
    }
}
