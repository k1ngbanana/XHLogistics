package pojo;

import java.util.Date;

public class Billing {
    //日期
    private Date date;
    //目的地
    private String dest;
    //航班
    private String flight;
    //起飞时间
    private Date takeOffTime;
    //到达时间
    private Date arrivalTime;
    //收货人
    private String receiver;
    //电话
    private String tel;
    //重量
    private String weight;
    //单价
    private double price;
    //费用
    private double cost;
    //到付
    private double freightCollect;
    //提货电话
    private String deliveryPhoneNumber;
    //单号
    private String deliveryNo;
    //件数
    private int packagePiece;
    //毛重
    private double grossWeight;
    //计费重量
    private String chargeWeight;
    //费率
    private double baseRate;
    //货品名称
    private String productName;
    //备注
    private String remark;
    //付款人
    private String payer;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

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

    public Date getTakeOffTime() {
        return takeOffTime;
    }

    public void setTakeOffTime(Date takeOffTime) {
        this.takeOffTime = takeOffTime;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getFreightCollect() {
        return freightCollect;
    }

    public void setFreightCollect(double freightCollect) {
        this.freightCollect = freightCollect;
    }

    public String getDeliveryPhoneNumber() {
        return deliveryPhoneNumber;
    }

    public void setDeliveryPhoneNumber(String deliveryPhoneNumber) {
        this.deliveryPhoneNumber = deliveryPhoneNumber;
    }

    public String getDeliveryNo() {
        return deliveryNo;
    }

    public void setDeliveryNo(String deliveryNo) {
        this.deliveryNo = deliveryNo;
    }

    public int getPackagePiece() {
        return packagePiece;
    }

    public void setPackagePiece(int packagePiece) {
        this.packagePiece = packagePiece;
    }

    public double getGrossWeight() {
        return grossWeight;
    }

    public void setGrossWeight(double grossWeight) {
        this.grossWeight = grossWeight;
    }

    public String getChargeWeight() {
        return chargeWeight;
    }

    public void setChargeWeight(String chargeWeight) {
        this.chargeWeight = chargeWeight;
    }

    public double getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(double baseRate) {
        this.baseRate = baseRate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    @Override
    public String toString() {
        return "Billing{" +
                "date=" + date +
                ", dest='" + dest + '\'' +
                ", flight='" + flight + '\'' +
                ", takeOffTime=" + takeOffTime +
                ", arrivalTime=" + arrivalTime +
                ", receiver='" + receiver + '\'' +
                ", tel='" + tel + '\'' +
                ", weight='" + weight + '\'' +
                ", price=" + price +
                ", cost=" + cost +
                ", freightCollect=" + freightCollect +
                ", deliveryPhoneNumber='" + deliveryPhoneNumber + '\'' +
                ", deliveryNo='" + deliveryNo + '\'' +
                ", packagePiece=" + packagePiece +
                ", grossWeight=" + grossWeight +
                ", chargeWeight='" + chargeWeight + '\'' +
                ", baseRate=" + baseRate +
                ", productName='" + productName + '\'' +
                ", remark='" + remark + '\'' +
                ", payer='" + payer + '\'' +
                '}';
    }
}

