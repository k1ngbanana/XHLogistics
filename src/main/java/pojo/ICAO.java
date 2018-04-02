package pojo;

public class ICAO {
    private String ICAO;
    private String dest;

    public String getICAO() {
        return ICAO;
    }

    public void setICAO(String ICAO) {
        this.ICAO = ICAO;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    @Override
    public String toString() {
        return "ICAO{" +
                "ICAO='" + ICAO + '\'' +
                ", dest='" + dest + '\'' +
                '}';
    }
}
