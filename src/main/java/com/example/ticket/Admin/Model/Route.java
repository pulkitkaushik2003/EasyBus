package com.example.ticket.Admin.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Route { // Note: Consider adding @NoArgsConstructor and @AllArgsConstructor from Lombok
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}
