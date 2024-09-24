package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Booking {
    private String firstname;
    private String lastname;
    private int totalprice;
    private boolean depositpaid;

    @JsonProperty("bookingdates")
    private BookingDates bookingDates;

    private String additionalneeds;



}


