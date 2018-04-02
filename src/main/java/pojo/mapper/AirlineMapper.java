package pojo.mapper;

import pojo.Airline;

import java.util.List;

public interface AirlineMapper {
	Airline selectAirline(Airline airline);

	void updateFlightTimeAndVia(List<Airline> airlines);

	void updateFlightTimeAndWeekdays(List<Airline> airlines);

	void insertAirlines(List<Airline> airlines);


}


