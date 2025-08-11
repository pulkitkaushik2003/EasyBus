package com.example.ticket.Admin.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "routes")
@Getter
@Setter
public class Route { // Note: Consider adding @NoArgsConstructor and @AllArgsConstructor from Lombok
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String routeName;
    private String busNumber;

    @Column(name = "from_location") // Avoid MySQL reserved keyword
    private String fromLocation;

    private String destination;
    private LocalDate date;
    private LocalTime arrivalTime;
    private LocalTime departureTime;
    private Double fare;
    private Integer totalSeat;
    private Integer availableSeat;

    public Route() {
        super();
    }

    // A constructor with the correct types is useful for testing and instantiation.
    public Route(int id, String routeName, String busNumber, String fromLocation, String destination, LocalDate date,
                 LocalTime arrivalTime, LocalTime departureTime, Double fare, Integer totalSeat, Integer availableSeat) {
        this.id = id;
        this.routeName = routeName;
        this.busNumber = busNumber;
        this.fromLocation = fromLocation;
        this.destination = destination;
        this.date = date;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.fare = fare;
        this.totalSeat = totalSeat;
        this.availableSeat = availableSeat;
    }
}