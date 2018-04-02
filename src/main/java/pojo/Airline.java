package pojo;

import java.sql.Time;
import java.sql.Timestamp;

public class Airline {
    private String flight;
    private String company;
    private String departure;
    private String dest;
    private Time launch;
    private Time arrival;
    private Timestamp updateTime;
    private String weekdays;
    private String via;

    public String getFlight() {
        return flight;
    }

    public void setFlight(String flight) {
        this.flight = flight;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public Time getLaunch() {
        return launch;
    }

    public void setLaunch(Time launch) {
        this.launch = launch;
    }

    public Time getArrival() {
        return arrival;
    }

    public void setArrival(Time arrival) {
        this.arrival = arrival;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(String weekdays) {
        this.weekdays = weekdays;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    @Override
    public String toString() {
        return "Airline{" +
                "flight='" + flight + '\'' +
                ", company='" + company + '\'' +
                ", departure='" + departure + '\'' +
                ", dest='" + dest + '\'' +
                ", launch=" + launch +
                ", arrival=" + arrival +
                ", updateTime=" + updateTime +
                ", weekdays='" + weekdays + '\'' +
                ", via='" + via + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(!(obj instanceof Airline)){
            return false;
        }
        final Airline airline = (Airline) obj;
        if(!this.getFlight().equals(airline.getFlight())){
            return false;
        }
        if(!this.getDest().equals(airline.getDest())){
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.flight.hashCode() + this.dest.hashCode();
    }
}

