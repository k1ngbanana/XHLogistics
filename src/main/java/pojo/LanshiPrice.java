package pojo;

public class LanshiPrice {

    private String dest;
    private String flight;
    private double beyond100;
    private double under100;


    //用于记录自己手动更改的价格
    private boolean flag;

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

    public double getBeyond100() {
        return beyond100;
    }

    public void setBeyond100(double beyond100) {
        this.beyond100 = beyond100;
    }

    public double getUnder100() {
        return under100;
    }

    public void setUnder100(double under100) {
        this.under100 = under100;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }


    @Override
    public String toString() {
        return "LanshiPrice{" +
                "dest='" + dest + '\'' +
                ", flight='" + flight + '\'' +
                ", beyond100=" + beyond100 +
                ", under100=" + under100 +
                ", flag=" + flag +
                '}';
    }
}
