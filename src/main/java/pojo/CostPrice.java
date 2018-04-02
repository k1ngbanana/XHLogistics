package pojo;

import java.sql.Time;

public class CostPrice {

    private String dest;
    private String flight;
    private Time launch;
    private Time arrival;
    private double M;
    private double N;
    private double p45;
    private double p100;
    private double p300;
    private double p500;
    private double p1000;
    private double p2000;

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getFlight() {
        return flight;
    }

    public void setFlight(String flight) {
        this.flight = flight;
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

    public double getM() {
        return M;
    }

    public void setM(double m) {
        M = m;
    }

    public double getN() {
        return N;
    }

    public void setN(double n) {
        N = n;
    }

    public double getP45() {
        return p45;
    }

    public void setP45(double p45) {
        this.p45 = p45;
    }

    public double getP100() {
        return p100;
    }

    public void setP100(double p100) {
        this.p100 = p100;
    }

    public double getP300() {
        return p300;
    }

    public void setP300(double p300) {
        this.p300 = p300;
    }

    public double getP500() {
        return p500;
    }

    public void setP500(double p500) {
        this.p500 = p500;
    }

    public double getP1000() {
        return p1000;
    }

    public void setP1000(double p1000) {
        this.p1000 = p1000;
    }

    public double getP2000() {
        return p2000;
    }

    public void setP2000(double p2000) {
        this.p2000 = p2000;
    }

    @Override
    public String toString() {
        return "CostPrice{" +
                "dest='" + dest + '\'' +
                ", flight='" + flight + '\'' +
                ", launch=" + launch +
                ", arrival=" + arrival +
                ", M=" + M +
                ", N=" + N +
                ", p45=" + p45 +
                ", p100=" + p100 +
                ", p300=" + p300 +
                ", p500=" + p500 +
                ", p1000=" + p1000 +
                ", p2000=" + p2000 +
                '}';
    }
}
