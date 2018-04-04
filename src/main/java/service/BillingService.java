package service;

import config.RootConfig;
import config.SpringBean;
import controller.BillingController;
import dao.AirlineDao;
import dao.BillingDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pojo.Airline;
import pojo.Billing;
import pojo.CostPrice;
import pojo.LanshiPrice;
import utils.POIUtils;

import java.io.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringBean.class, RootConfig.class})
public class BillingService extends BaseService {

    @Autowired
    BillingDao bd;
    @Autowired
    AirlineDao ad;
    @Autowired
    AirlineService as;
    @Autowired
    PriceService ps;


    POIUtils poiUtils = new POIUtils();


    @Test
    /**
     * 写出确认信息文件
     */
    public void writeComfirmInformation() throws ParseException, IOException, InvalidFormatException {
        //Workbook wb = poiUtils.getWorkBook("formatExcel");
        String filePath = getDatePath(pathProp.getProperty("bookingPath"), pathProp.getProperty("billingExcelName"));
        Workbook wb = WorkbookFactory.create(new FileInputStream(filePath));
        Sheet dataSheet = wb.getSheet("数据表");


        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= dataSheet.getLastRowNum(); i++) {
            Row row = dataSheet.getRow(i);
            sb.append(i + ".\n");
            sb.append("【薛航物流】尊敬的");
            sb.append(getCellValue(row, "收货人"));
            sb.append("，您好！广州-");
            sb.append(getCellValue(row, "目的地"));
            sb.append(getCellValue(row, "航班"));
            sb.append("航班预计");
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            Date date = sdf.parse(getCellValue(row, "计划起飞"));
            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
            sb.append(sdf2.format(date));
            sb.append("起飞，");
            if (!getCellValue(row, "件数").isEmpty() && !getCellValue(row, "毛重").isEmpty()) {
                sb.append(Double.valueOf(getCellValue(row, "件数")).intValue());
                sb.append("件");
                Double weight = Double.valueOf(getCellValue(row, "重量"));
                sb.append(Math.round(weight + 0.2));
            } else {
                sb.append("\n\n【注意】没有填件数或重量【注意】\n\n");
            }
            sb.append("公斤，");
            if (getCell(row, "备注") != null && !getCellValue(row, "备注").isEmpty()) {
                if (getCellValue(row, "备注").contains("到付")) {
                    sb.append("到付每公斤");
                    sb.append(getCellValue(row, "单价"));
                    sb.append("元，共");
                    sb.append(Double.valueOf(getCellValue(row, "到付")).intValue());
                    sb.append("元");
                } else {
                    sb.append("广州付每公斤");
                    sb.append(getCellValue(row, "单价"));
                    sb.append("元，共");
                    sb.append(Double.valueOf(getCellValue(row, "费用")).intValue());
                    sb.append("元");
                }
            }
            if (getCellValue(row, "提货电话") != "" && getCell(row, "提货电话") != null && getCellValue(row, "单号") != "" && getCell(row, "单号") != null) {
                sb.append("，提货电话：");
                sb.append(getCellValue(row, "提货电话"));
                sb.append("，提货单号：");
                sb.append(getCellValue(row, "单号"));
            }
            sb.append("\n");
        }
        String comfirmFile = getDatePath(pathProp.getProperty("bookingPath"), pathProp.getProperty("comfirmTxtName"));
        File file = new File(comfirmFile);
        try {
            FileWriter fw = new FileWriter(file, false);
            fw.write(sb.toString());
            fw.close();
            System.out.println(comfirmFile + "输出完成");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    /**
     * 输出订舱和单号.txt文件
     */
    public void writeBookingInformation() throws IOException, InvalidFormatException {
        String filePath = getDatePath(pathProp.getProperty("bookingPath"), pathProp.getProperty("billingExcelName"));
        Workbook wb = WorkbookFactory.create(new FileInputStream(filePath));
        Sheet dataSheet = wb.getSheet("数据表");

        StringBuilder bookingSb = new StringBuilder();
        StringBuilder deliverNoSb = new StringBuilder();
        for (int i = 1; i <= dataSheet.getLastRowNum(); i++) {
            Row row = dataSheet.getRow(i);
            bookingSb.append(i + ".\n");
            bookingSb.append(getCellValue(row, "目的地") + "/");
            bookingSb.append(getCellValue(row, "航班") + "/");
            bookingSb.append(getCellValue(row, "收货人") + "/");
            bookingSb.append(getCellValue(row, "电话") + "\n");
            bookingSb.append(getCellValue(row, "货品名称") + "/");
            if (!getCellValue(row, "件数").isEmpty() && !getCellValue(row, "毛重").isEmpty()) {
                bookingSb.append(Double.valueOf(getCellValue(row, "件数")).intValue() + "/");
                bookingSb.append(Double.valueOf(getCellValue(row, "毛重")).intValue() + "/");
            }
            bookingSb.append(getCellValue(row, "备注") + "\n");

            deliverNoSb.append(i + ".\n");
            deliverNoSb.append(getCellValue(row, "航班") + "/");
            deliverNoSb.append(getCellValue(row, "收货人") + "/");
            if (!getCellValue(row, "件数").isEmpty() && !getCellValue(row, "毛重").isEmpty()) {
                deliverNoSb.append(Double.valueOf(getCellValue(row, "件数")).intValue() + "/");
                deliverNoSb.append(Double.valueOf(getCellValue(row, "毛重")).intValue() + "\n");
            }

        }

        String bookingFile = getDatePath(pathProp.getProperty("bookingPath"), pathProp.getProperty("bookingTxtName"));
        File file = new File(bookingFile);
        writeTxtFile(file, bookingSb.toString() + "\n\n\n\n" + deliverNoSb.toString());

    }

    //简化输出函数
    public void writeTxtFile(File file, String stringToWrite) {
        try {
            FileWriter fw = new FileWriter(file, false);
            fw.write(stringToWrite);
            fw.close();
            System.out.println(file.getAbsolutePath() + "输出完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeBillingRecord() throws IOException, InvalidFormatException {
        String filePath = getDatePath(pathProp.getProperty("bookingPath"), pathProp.getProperty("billingExcelName"));
        Workbook wb = WorkbookFactory.create(new FileInputStream(filePath));
        Workbook recordWb = WorkbookFactory.create(new FileInputStream(pathProp.getProperty("billingRecordExcel")));
        Sheet dataSheet = wb.getSheet("数据表");
        Sheet insertSheet = recordWb.getSheet("未插入数据库");

        Map<Integer, CellStyle> styleMap = new HashMap<>();
        for(int i = 1; i <= dataSheet.getLastRowNum(); i++){
            Row row = dataSheet.getRow(i);
            if(!getCellValue(row, "提货电话").isEmpty()&& !getCellValue(row, "单号").isEmpty()){
                POIUtils.copyRow(dataSheet, insertSheet, row, insertSheet.getRow(insertSheet.getLastRowNum()+1), styleMap);
            }
        }
        recordWb.write(new FileOutputStream(pathProp.getProperty("billingRecordExcel")));
        recordWb.close();

    }

    @Test
    /**
     * 自动完成billing发顺丰表格
     * 以蓝氏兄弟的为准
     */
    public void autoCompleteBilling() throws IOException {
        Workbook wb = POIUtils.getWorkBook("formatExcel");
        Sheet dataSheet = wb.getSheet("数据表");

        //Properties billingProp = POIUtils.getProp("/billingExcel.properties");

        Airline airline = new Airline();
        LanshiPrice lanshiPrice = new LanshiPrice();
        CostPrice costPrice = new CostPrice();

        //创建基础格式和日期、时间单元格格式
        CreationHelper createHelper = wb.getCreationHelper();
        CellStyle timeStyle = wb.createCellStyle();
        CellStyle dateStyle = wb.createCellStyle();
        CellStyle baseStyle = wb.createCellStyle();

        baseStyle.setAlignment(HorizontalAlignment.LEFT);
        Font baseFont = wb.createFont();
        baseFont.setFontHeightInPoints((short) 11);
        baseFont.setFontName("等线");
        baseStyle.setFont(baseFont);

        timeStyle.cloneStyleFrom(baseStyle);
        timeStyle.setDataFormat(createHelper.createDataFormat().getFormat("HH:mm"));
        dateStyle.cloneStyleFrom(baseStyle);
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy/MM/dd"));


        for (int i = 1; i <= dataSheet.getLastRowNum(); i++) {

            Row row = dataSheet.getRow(i);
            long combinedWeight = 0;
            double tempCombinedWeight = 0;

            if (airline == null) {
                airline = new Airline();
            }
            if (lanshiPrice == null) {
                lanshiPrice = new LanshiPrice();
            }
            if (costPrice == null) {
                costPrice = new CostPrice();
            }

            if (!(getCellValue(row, "航班").isEmpty() && getCellValue(row, "目的地").isEmpty())) {
                airline.setFlight(getCellValue(row, "航班"));
                airline.setDest(getCellValue(row, "目的地"));
                lanshiPrice.setFlight(getCellValue(row, "航班"));
                lanshiPrice.setDest(getCellValue(row, "目的地"));
                costPrice.setFlight(getCellValue(row, "航班"));
                costPrice.setDest(getCellValue(row, "目的地"));
            }


            //TODO 先更新当天航班信息.要打开更新
            //as.updateOrInsertFlightTimeAndVia();
            //通过航班号和目的地查询航班信息
            airline = ad.selectAirline(airline);

            //数据库没有该航班信息
            if (airline == null) {
                System.out.println("第" + (i + 1) + "行航班信息不完整或错误，请查看【航班号】和【目的地】是否都已经填写正确");
            } else if (airline.getFlight().isEmpty() || airline.getDest().isEmpty()) {
                System.out.println("第" + (i + 1) + "行航班信息不完整，请查看航班【" + airline.getFlight() + "】和目的地【" + airline.getDest() + "】是否都已经填写完整");
            }

            //日期
            Cell dateCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "日期"), CellType.NUMERIC);
            //起飞时间
            Cell launchCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "计划起飞"), CellType.NUMERIC);
            //到达时间
            Cell arrivalCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "计划到达"), CellType.NUMERIC);
            //单价
            Cell priceCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "单价"), CellType.STRING);
            //费用
            Cell costCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "费用"), CellType.NUMERIC);
            //到付
            Cell freightCollectCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "到付"), CellType.NUMERIC);
            //件数
            Cell pieaceCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "件数"), CellType.NUMERIC);
            //毛重
            Cell grossWeightCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "毛重"), CellType.NUMERIC);
            //计费重量
            Cell chargeWeightCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "计费重量"), CellType.NUMERIC);
            //费率
            Cell chargeRateCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "费率"), CellType.NUMERIC);
            //货品名称
            Cell prodcutNameCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "货品名称"), CellType.STRING);

            //设置dateCell日期，billingExcel.prop里面判断是今天还是明天
            if (POIUtils.getCellValue(dateCell).equals("")) {
                Calendar calendar = Calendar.getInstance();
                if (Boolean.parseBoolean((String) billingProp.get("today"))) {
                    dateCell.setCellValue(calendar.getTime());
                } else {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    dateCell.setCellValue(calendar.getTime());
                }
                dateCell.setCellStyle(dateStyle);
            }

            //插入起飞和到达时间
            if (airline != null) {
                if (POIUtils.getCellValue(launchCell).equals("")) {
                    if (airline.getLaunch() != null) {
                        launchCell.setCellValue(airline.getLaunch());
                    }
                    launchCell.setCellStyle(timeStyle);
                }
                if (POIUtils.getCellValue(arrivalCell).equals("")) {
                    if (airline.getArrival() != null) {
                        arrivalCell.setCellValue(airline.getArrival());
                    }
                    arrivalCell.setCellStyle(timeStyle);
                }
            }

            //如果没有重量，那么插入蓝氏100kg以下和100kg以上价格
            lanshiPrice = ps.selectPrice(lanshiPrice);
            if (lanshiPrice != null) {
                if (getCell(row, "重量") == null || getCellValue(row, "重量").isEmpty()) {
                    priceCell.setCellValue(lanshiPrice.getUnder100() + "/" + lanshiPrice.getBeyond100());
                } else {

                    //填充单价
                    if (Double.valueOf(getCellValue(row, "重量")) < 99.3) {
                        priceCell.setCellValue(lanshiPrice.getUnder100());
                    } else {
                        priceCell.setCellValue(lanshiPrice.getBeyond100());
                    }

                    //有重量，没有备注或者没有【到付】备注的时候，价格全部填入费用一栏，否则填入到付
                    //重量 0.2进位，费用 0.3进位<--可能会存在精度问题
                    long cost = Math.round(Math.round(Double.valueOf(getCellValue(row, "重量")) + 0.3) * Double.valueOf(POIUtils.getCellValue(priceCell)) + 0.2);
                    if (!POIUtils.getCellValue(priceCell).isEmpty() && POIUtils.getCellValue(priceCell).matches("[\\d].*")) {
                        if (getCell(row, "备注") == null || getCellValue(row, "备注").isEmpty() || !getCellValue(row, "备注").contains("到付")) {
                            costCell.setCellValue(cost);
                        } else {
                            freightCollectCell.setCellValue(cost);
                        }
                    }

                    //按照公式的"+"号数量+1作为件数
                    int piece = 1;
                    if (getCell(row, "重量").getCellTypeEnum() == CellType.FORMULA) {
                        String formula = getCell(row, "重量").getCellFormula();
                        piece = StringUtils.countMatches(formula, "+") + 1;
                    }
                    //如果有重量，填入件数
                    if (POIUtils.getCellValue(pieaceCell).isEmpty()) {
                        pieaceCell.setCellValue(piece);
                    } else {
                        if (Double.valueOf(getCellValue(row, "件数")) != piece) {
                            POIUtils.setCellComment(pieaceCell, "重量如果是用公式填写的话，那么件数可能不对，计算的件数是【" + piece + "件】");
                        }
                        //如果平均重量大于40kg一件，那么提示有可能出错
                        if (Double.valueOf(getCellValue(row, "重量")) / Double.valueOf(getCellValue(row, "件数")) > 40) {
                            POIUtils.setCellComment(pieaceCell, "重量超过单件40kg，可能出现问题");
                        }
                    }

                    //写入毛重，0.7进位
                    long grossWeight = Math.round(Double.valueOf(getCellValue(row, "重量")) - 0.2);
                    if (POIUtils.getCellValue(grossWeightCell).isEmpty()) {
                        grossWeightCell.setCellValue(grossWeight);
                    } else {
                        if (grossWeight != Double.valueOf(POIUtils.getCellValue(grossWeightCell))) {
                            POIUtils.setCellComment(grossWeightCell, "计算的重量为" + grossWeight);
                        }
                    }

                    //计算该行合票重量
                    for (int j = 1; j <= dataSheet.getLastRowNum(); j++) {
                        if (getCellValue(row, "航班").equals(getCellValue(dataSheet.getRow(j), "航班"))) {
                            if (getCellValue(row, "目的地").equals(getCellValue(dataSheet.getRow(j), "目的地"))) {
                                if (getCellValue(dataSheet.getRow(j), "备注").contains("合票")) {
                                    if (getCellValue(dataSheet.getRow(j), "重量") != null) {
                                        tempCombinedWeight += Math.round(Double.valueOf(getCellValue(dataSheet.getRow(j), "重量")) - 0.2);
                                    }
                                }
                            }
                        }
                    }

                    //将combinedWeight变成long，0.7进位<----每一行的合票重量，没有合票重量则把毛重复制给该行的combinedWeight
                    if (tempCombinedWeight != 0) {
                        combinedWeight = Math.round(tempCombinedWeight - 0.2);
                    }
                    if (tempCombinedWeight == 0 || !getCellValue(dataSheet.getRow(i), "备注").contains("合票")) {
                        combinedWeight = grossWeight;
                    }


                    //写入计费重量
                    costPrice = ps.selectPrice(costPrice);
                    if (costPrice != null) {
                        chargeWeightCell.setCellValue(combinedWeight);
                        //如果是南航<----要偷偷抛
                        if (costPrice.getFlight().contains("CZ")) {
                            //大于40kg的话，对比45kg还是40kg便宜&3099不要抛
                            if (!(costPrice.getFlight().equals("CZ3099"))) {
                                if (combinedWeight > 39 && combinedWeight < 45) {
                                    if (combinedWeight * costPrice.getN() > 45 * costPrice.getP45()) {
                                        //南航，在39kg到45kg之间，如果45kg便宜，那么偷偷抛到45kg
                                        chargeWeightCell.setCellValue(45);
                                        POIUtils.setCellComment(chargeWeightCell, "原来重量为" + combinedWeight + "公斤，N级别价格为" + costPrice.getN() + "/kg" + "，45kg级别价格为" + costPrice.getP45() + "/kg");
                                    }
                                }
                                //南航，在95kg到100kg之间，如果100kg便宜，那么偷偷抛到100kg
                                if (combinedWeight > 94 && combinedWeight < 100) {
                                    if (combinedWeight * costPrice.getP45() > 100 * costPrice.getP100()) {
                                        chargeWeightCell.setCellValue(100);
                                        POIUtils.setCellComment(chargeWeightCell, "原来重量为" + combinedWeight + "公斤，45kg级别价格为" + costPrice.getP45() + "/kg" + "，100kg级别价格为" + costPrice.getP100() + "/kg");
                                    }
                                }
                            } else {
                                //我暂时认为没有一件货少于3kg的。大于3即有合票
                                if (combinedWeight - combinedWeight > 3) {
                                    POIUtils.setCellComment(chargeWeightCell, "合票重量为" + combinedWeight + "kg");
                                }
                            }
                        } else {
                            //不是南航的<-----选便宜的抛
                            int minRateNum = 0;
                            double minPrice = 0;
                            HashMap<Integer, Double> hashMap = new HashMap<>();
                            if (combinedWeight < 100) {
                                //如果N*重量比M小，那么最便宜的价格为M
                                if (combinedWeight * costPrice.getN() < costPrice.getM()) {
                                    hashMap.put(1, costPrice.getM());
                                }
                                if (combinedWeight < 45) {
                                    hashMap.put(2, 45 * costPrice.getP45());
                                    hashMap.put(3, combinedWeight * costPrice.getN());
                                } else {
                                    hashMap.put(4, combinedWeight * costPrice.getP45());
                                }
                                hashMap.put(5, 100 * costPrice.getP100());
                            } else if (combinedWeight < 300) {
                                hashMap.put(6, combinedWeight * costPrice.getP100());
                                hashMap.put(7, 300 * costPrice.getP300());
                            } else if (combinedWeight < 500) {
                                hashMap.put(8, combinedWeight * costPrice.getP300());
                                hashMap.put(9, 500 * costPrice.getP500());
                            } else if (combinedWeight < 1000) {
                                hashMap.put(10, combinedWeight * costPrice.getP500());
                                hashMap.put(11, 1000 * costPrice.getP1000());
                            }

                            for (Map.Entry<Integer, Double> entry : hashMap.entrySet()) {
                                if (minRateNum == 0) {
                                    //初始化最小值
                                    minRateNum = entry.getKey();
                                    minPrice = entry.getValue();
                                } else {
                                    if (entry.getValue() == 0) {
                                        continue;
                                    } else {
                                        if (entry.getValue() < minPrice) {
                                            minPrice = entry.getValue();
                                            minRateNum = entry.getKey();
                                        }
                                    }
                                }
                            }

                            switch (minRateNum) {
                                case 1:
                                case 3:
                                case 4:
                                case 6:
                                case 8:
                                case 10:
                                    chargeWeightCell.setCellValue(combinedWeight);
                                    break;
                                case 2:
                                    chargeWeightCell.setCellValue(45);
                                    POIUtils.setCellComment(chargeWeightCell, "原来重量为" + combinedWeight + "kg，M/N/45价格为" + costPrice.getM() + "/" + costPrice.getN() + "/" + costPrice.getP45() + "【本来不可以抛到45的，不过便宜啊，试试】");
                                    break;
                                case 5:
                                    chargeWeightCell.setCellValue(100);
                                    POIUtils.setCellComment(chargeWeightCell, "原来重量为" + combinedWeight + "kg，45/100价格为" + costPrice.getP45() + "/" + costPrice.getP100() + "元/kg");
                                    break;
                                case 7:
                                    chargeRateCell.setCellValue(300);
                                    POIUtils.setCellComment(chargeWeightCell, "原来重量为" + combinedWeight + "kg，100/300价格为" + costPrice.getP100() + "/" + costPrice.getP300() + "元/kg");
                                    break;
                                case 9:
                                    chargeRateCell.setCellValue(500);
                                    POIUtils.setCellComment(chargeWeightCell, "原来重量为" + combinedWeight + "kg，300/500价格为" + costPrice.getP300() + "/" + costPrice.getP500() + "元/kg");
                                    break;
                                case 11:
                                    chargeRateCell.setCellValue(1000);
                                    POIUtils.setCellComment(chargeWeightCell, "原来重量为" + combinedWeight + "kg，500/1000价格为" + costPrice.getP500() + "/" + costPrice.getP1000() + "元/kg");
                                    break;
                            }

                        }

                        //写入费率
                        if (!POIUtils.getCellValue(chargeWeightCell).isEmpty()) {
                            if (chargeRateCell.getNumericCellValue() != 0) {
                                //判断计费重量是在哪个区间，然后写入费率
                                if (getIntervalPrice(costPrice, Double.valueOf(getCellValue(row, "计费重量")).longValue()) != chargeRateCell.getNumericCellValue()) {
                                    POIUtils.setCellComment(chargeRateCell, "【计算出来的费率是" + getIntervalPrice(costPrice, Double.valueOf(getCellValue(row, "计费重量")).longValue()) + "元】，请核对");
                                }
                            } else {
                                //判断计费重量是在哪个区间，然后写入费率
                                if (getIntervalPrice(costPrice, Double.valueOf(getCellValue(row, "计费重量")).longValue()) > 30) {
                                    //如果单价>30，那么肯定是M的价格，所以前面加“M/”
                                    chargeRateCell.setCellValue("M/" + getIntervalPrice(costPrice, Double.valueOf(getCellValue(row, "计费重量")).longValue()));
                                } else {
                                    chargeRateCell.setCellValue(getIntervalPrice(costPrice, Double.valueOf(getCellValue(row, "计费重量")).longValue()));
                                }
                                //暂时把所有费率写入comment，以后确认没问题之后删除//TODO
                                POIUtils.setCellComment(chargeRateCell, "【M=" + costPrice.getM() + "】\n【N=" + costPrice.getN() + "】\n【45=" + costPrice.getP45() + "】\n【100=" + costPrice.getP100() + "】\n【300=" + costPrice.getP300() + "】");
                            }

                        }

                    } else {
                        System.out.println("没有找到对应成本价格，请确认成本价格表是否已更新到数据库");
                    }
                }
            } else {
                System.out.println("没有找到对应鱼商价格，请确认蓝氏价格表是否已更新到数据库");
            }

            //插入货物名称
            if (POIUtils.getCellValue(prodcutNameCell).isEmpty()) {
                prodcutNameCell.setCellValue("观赏鱼");
            }

        }

        //将所有隐藏列变成不隐藏
        for (int i = 0; i < dataSheet.getRow(0).getLastCellNum(); i++) {
            if (dataSheet.isColumnHidden(i)) {
                dataSheet.setColumnHidden(i, false);
                dataSheet.autoSizeColumn(i);
                System.out.println("第" + i + "列");
                System.out.println(dataSheet.isColumnHidden(i));
            }
        }

        //输出文件到格式化价格.xlsx
        FileOutputStream fos = new FileOutputStream(getDatePath(pathProp.getProperty("bookingPath"), pathProp.getProperty("billingExcelName")));
        wb.write(fos);
        fos.close();
        System.out.println("billing发顺丰.xlsx自动填充完成");

        try {
            writeBookingInformation();
            writeComfirmInformation();
            writeSLI();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }

    }

    @Test
    //输出托运书Shipper’s Letter of Instruction
    public void writeSLI() throws IOException, InvalidFormatException {
        Workbook sliWb = POIUtils.getWorkBook("sliExcel");
        String billingPath = getDatePath(pathProp.getProperty("bookingPath"), pathProp.getProperty("billingExcelName"));
        Workbook dataWb = WorkbookFactory.create(new FileInputStream(billingPath));
        Sheet dataSheet = dataWb.getSheetAt(0);
        //Sheet sliSheet = sliWb.getSheetAt(0);

/*        Cell destCell = sliSheet.getRow(2).getCell(6);
        Cell nameCell = sliSheet.getRow(4).getCell(2);
        Cell flightCell = sliSheet.getRow(7).getCell(0);
        Cell pieceCell = sliSheet.getRow(9).getCell(0);
        Cell grossWeightCell = sliSheet.getRow(9).getCell(1);
        Cell chargeWeightCell = sliSheet.getRow(9).getCell(2);
        Cell chargeRateCell = sliSheet.getRow(9).getCell(3);
        Cell productNameCell = sliSheet.getRow(9).getCell(4);
        Cell dateCell = sliSheet.getRow(13).getCell(5);*/

        List<Billing> billingList = new ArrayList<>();

        //遍历当天的所有billing并放到billingList里面
        for (int i = 1; i <= dataSheet.getLastRowNum(); i++) {
            Row row = dataSheet.getRow(i);
            Billing billing = new Billing();
            billing.setDest(getCellValue(row, "目的地"));
            billing.setFlight(getCellValue(row, "航班"));
            billing.setReceiver(getCellValue(row, "收货人"));
            billing.setTel(getCellValue(row, "电话"));
            billing.setRemark(getCellValue(row, "备注"));
            billing.setPackagePiece(Double.valueOf(getCellValue(row, "件数")).intValue());
            billing.setGrossWeight(Double.valueOf(getCellValue(row, "毛重")));
            billing.setChargeWeight(getCellValue(row, "计费重量"));
            String baseRateStr;
            if (getCellValue(row, "费率").contains("M/")) {
                baseRateStr = getCellValue(row, "费率").replace("M/", "");
            } else {
                baseRateStr = getCellValue(row, "费率");
            }
            billing.setBaseRate(Double.valueOf(baseRateStr));
            billing.setProductName(getCellValue(row, "货品名称"));
            if (getCell(row, "到付") != null && !getCellValue(row, "到付").isEmpty()) {
                billing.setFreightCollect(Double.valueOf(getCellValue(row, "到付")));
            }
            billingList.add(billing);
        }

        //创建一个List，存放对应合票或者单开对应的行的list
        List<List<Integer>> sliList = new ArrayList<>();
        for (int i = 0; i < billingList.size(); i++) {
            List<Integer> rowList = new ArrayList<>();
            for (int j = 0; j < billingList.size(); j++) {
                //要遍历的改行为合票
                if (billingList.get(i).getRemark().contains("合票")) {
                    //航班相同
                    if (billingList.get(i).getFlight().equals(billingList.get(j).getFlight())) {
                        //目的地相同
                        if (billingList.get(i).getDest().equals(billingList.get(j).getDest())) {
                            //合票
                            if (billingList.get(j).getRemark().contains("合票")) {
                                //如果i<=j，即改行还没遍历过，添加，否则已经遍历过，break;
                                if (i <= j) {
                                    rowList.add(j + 1);
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    //不是合票的不用遍历完，只需要添加到rowList作为一行就可以了
                    rowList.add(i + 1);
                    break;
                }
            }
            //该行有数据保存到rowList才添加到silList里面去
            if (rowList.size() > 0){
                sliList.add(rowList);
            }
        }

        // 复制对应数量的托运书，并填入相应的数据
        for (
                int i = 0; i < sliList.size(); i++)

        {
            if (i != 0) {
                ((XSSFWorkbook) sliWb).cloneSheet(0, sliWb.getSheetName(0) + (i + 1));
            }
            //遍历sliList
            List<Integer> rowList = sliList.get(i);

            //获取单开或合票的第一个billing，通过他来填写目的地和费率
            Billing billing = billingList.get(rowList.get(0) - 1);
            //目的站
            sliWb.getSheetAt(i).getRow(2).getCell(6).setCellValue(billing.getDest());
            //计费重量
            sliWb.getSheetAt(i).getRow(9).getCell(2).setCellValue(Double.valueOf(billing.getChargeWeight()).intValue());
            //费率
            if(billing.getBaseRate()>30){
                sliWb.getSheetAt(i).getRow(9).getCell(3).setCellValue("M/"+(int)billing.getBaseRate());
            }else{
                sliWb.getSheetAt(i).getRow(9).getCell(3).setCellValue(billing.getBaseRate());
            }
            //航班号
            sliWb.getSheetAt(i).getRow(7).getCell(0).setCellValue(billing.getFlight() + "/");
            //日期
            Calendar calendar = Calendar.getInstance();
            if (!Boolean.parseBoolean(billingProp.getProperty("today"))) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            sliWb.getSheetAt(i).getRow(13).getCell(5).setCellValue(calendar.getTime());
            if (rowList.size() == 1) {
                //收货人姓名和电话
                sliWb.getSheetAt(i).getRow(4).getCell(2).setCellValue(billing.getReceiver() + "/" + billing.getTel());
                if (billing.getRemark().contains("到付")) {
                    sliWb.getSheetAt(i).getRow(7).getCell(0).setCellValue(billing.getFlight() + "/" + billing.getRemark() + "（" + (int) billing.getFreightCollect() + "元）");
                } else {
                    sliWb.getSheetAt(i).getRow(7).getCell(0).setCellValue(billing.getFlight() + "/" + billing.getRemark());
                }
                //件数
                sliWb.getSheetAt(i).getRow(9).getCell(0).setCellValue(billing.getPackagePiece());
                //毛重
                sliWb.getSheetAt(i).getRow(9).getCell(1).setCellValue(billing.getGrossWeight());
                //货品名称
                sliWb.getSheetAt(i).getRow(9).getCell(4).setCellValue(billing.getProductName());

            } else {
                sliWb.getSheetAt(i).getRow(4).getCell(2).setCellValue("开代理合票");
                StringBuilder sb = new StringBuilder(POIUtils.getCellValue(sliWb.getSheetAt(i).getRow(7).getCell(0)));
                int packagePiece = 0;
                int grossWeight = 0;
                //记数用于记录是否到达最后一次
                int count = 0;
                String productName = "";
                for (int j : rowList) {
                    billing = billingList.get(j - 1);
                    //名称和航班
                    if (billing.getRemark().contains("到付")) {
                        sb.append(billing.getReceiver() + "（到付" + (int) billing.getFreightCollect() + "元）");
                    } else {
                        sb.append(billing.getReceiver() + "（广州付）");
                    }
                    count++;
                    if (count < rowList.size()) {
                        sb.append("，");
                    }
                    //件数
                    packagePiece += billing.getPackagePiece();
                    //毛重
                    grossWeight += billing.getGrossWeight();
                    //货物名称
                    if (productName == "") {
                        productName = billing.getProductName();
                    } else if (!productName.contains(billing.getProductName())) {
                        productName += ("/" + billing.getProductName());
                    }
                }
                sliWb.getSheetAt(i).getRow(7).getCell(0).setCellValue(sb.toString());
                sliWb.getSheetAt(i).getRow(9).getCell(0).setCellValue(packagePiece);
                sliWb.getSheetAt(i).getRow(9).getCell(1).setCellValue(grossWeight);
                //货品名称
                sliWb.getSheetAt(i).getRow(9).getCell(4).setCellValue(billing.getProductName());
            }
        }

        sliWb.setSheetName(0, "托运书1");

        String sliPath = getDatePath(pathProp.getProperty("bookingPath"), "托运书.xlsx");
        sliWb.write(new

                FileOutputStream(sliPath));
        sliWb.close();
        System.out.println(sliPath + " 输出完成");


    }

    //简化判断该区间价格是否为0的价格，并获取最接近weight重量的价格
    public double getIntervalPrice(CostPrice costPrice, long weight) {
        int level;
        if (weight < 45) {
            if (costPrice.getN() * weight < costPrice.getM()) {
                //M
                level = 0;
            } else {
                //N
                level = 1;
            }
        } else if (weight < 100) {
            //45
            level = 2;
        } else if (weight < 300) {
            //100
            level = 3;
        } else if (weight < 500) {
            //300
            level = 4;
        } else if (weight < 1000) {
            //500
            level = 5;
        } else if (weight < 2000) {
            //1000
            level = 6;
        } else {
            //2000
            level = 7;
        }
        System.out.println(weight);
        if (costPrice.getP2000() != 0 && level > 6) {
            return costPrice.getP2000();
        } else if (costPrice.getP1000() != 0 && level > 5) {
            return costPrice.getP1000();
        } else if (costPrice.getP500() != 0 && level > 4) {
            return costPrice.getP500();
        } else if (costPrice.getP300() != 0 && level > 3) {
            return costPrice.getP300();
        } else if (costPrice.getP100() != 0 && level > 2) {
            return costPrice.getP100();
        } else if (costPrice.getP45() != 0 && level > 1) {
            return costPrice.getP45();
        } else if (costPrice.getN() != 0 && level > 0) {
            return costPrice.getN();
        } else {
            return costPrice.getM();
        }

    }


    //简化获取函数
    public Cell getCell(Row row, String colName) {
        //Workbook wb = poiUtils.getWorkBook("formatExcel");
        //Sheet dataSheet = wb.getSheet("数据表");
        return row.getCell(POIUtils.getColumnNum(billingProp, colName));
    }

    //通过单号和人名选择一个billing
    public Billing selectBilling(Billing billing) {
        return bd.selectBilling(billing);
    }

    //插入在"插入数据库"表格里面的所有billing到数据库
    @Test
    public void insertAndUpdateAllBilling() throws IOException, InvalidFormatException {
        Workbook wb = poiUtils.getWorkBook("formatExcel");
        Sheet insertSheet = wb.getSheet("销售记账");
        //Sheet insertedSheet = wb.getSheet("已插入数据库");

        //通过propertis文件获取列对应的值
        Properties billingProp = new Properties();
        InputStream is2 = BillingController.class.getResourceAsStream("/billingExcel.properties");
        billingProp.load(is2);
        //System.out.println(billingProp.get("日期"));

        //创建需要插入的对象

        //遍历所有行
        for (int i = 1; i <= insertSheet.getLastRowNum(); i++) {
            Billing billing = new Billing();
            Row row = insertSheet.getRow(i);
            billing.setDate(insertSheet.getRow(i).getCell(Integer.valueOf((String) billingProp.get("日期"))).getDateCellValue());
            billing.setDest(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("目的地")))));
            billing.setFlight(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("航班")))));
            billing.setTakeOffTime(row.getCell(Integer.valueOf((String) billingProp.get("计划起飞"))).getDateCellValue());
            billing.setArrivalTime(row.getCell(Integer.valueOf((String) billingProp.get("计划到达"))).getDateCellValue());
            billing.setReceiver(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("收货人")))));
            billing.setTel(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("电话")))));
            billing.setWeight(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("重量")))));
            if (row.getCell(Integer.valueOf((String) billingProp.get("单价"))) != null && !(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("单价"))))).equals("")) {
                BigDecimal bd = new BigDecimal(Double.valueOf(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("单价"))))));
                billing.setPrice(bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if (row.getCell(Integer.valueOf((String) billingProp.get("费用"))) != null && !(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("费用"))))).equals("")) {
                BigDecimal bd = new BigDecimal(Double.valueOf(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("费用"))))));
                billing.setCost(bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if (row.getCell(Integer.valueOf((String) billingProp.get("到付"))) != null && !(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("到付"))))).equals("")) {
                BigDecimal bd = new BigDecimal(Double.valueOf(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("到付"))))));
                billing.setFreightCollect(bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            billing.setDeliveryPhoneNumber(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("提货电话")))));
            billing.setDeliveryNo(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("单号")))));
            String pStr = POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("件数"))));

            billing.setPackagePiece(Integer.valueOf(pStr.substring(0, pStr.indexOf("."))));
            //billing.setGrossWeight(Double.valueOf(POIUtils.getCellValue(row.getCell(Integer.valueOf((String)billingProp.get("毛重"))))));
            if (row.getCell(Integer.valueOf((String) billingProp.get("毛重"))) != null && !(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("毛重"))))).equals("")) {
                BigDecimal bd = new BigDecimal(Double.valueOf(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("毛重"))))));
                billing.setGrossWeight(bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            billing.setChargeWeight(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("计费重量")))));
            //billing.setBaseRate(Double.valueOf(POIUtils.getCellValue(row.getCell(Integer.valueOf((String)billingProp.get("费率"))))));
            if (row.getCell(Integer.valueOf((String) billingProp.get("费率"))) != null && !(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("费率"))))).equals("")) {
                if (POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("费率")))).contains("M")) {
                    billing.setBaseRate(Double.valueOf(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("费率")))).replace("M/", "")));
                } else {
                    BigDecimal bd = new BigDecimal(Double.valueOf(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("费率"))))));
                    billing.setBaseRate(bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
            billing.setProductName(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("货品名称")))));
            billing.setRemark(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("备注")))));
            billing.setPayer(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("付款人")))));

            //判断数据库里面有没有这条数据
            if (bd.selectBilling(billing) == null) {
                //没有则插入billing到数据库
                bd.insertBilling(billing);
            } else {
                //如果有则更新这条数据
                bd.updateBilling(billing);
            }


        }
        //输出文件到格式化价格.xlsx
        FileOutputStream fos = new FileOutputStream(poiUtils.getFilePath("formatExcel"));
        wb.write(fos);
        fos.close();
    }

    //sheet为成本价表格
    public int getBaseRateNum(Sheet sheet, double weight, int rowNum) {
        if (weight < 45) {
            //N的价格
            double nPrice = Double.valueOf(POIUtils.getCellValue(sheet.getRow(rowNum).getCell(5)));
            double total = nPrice * weight;
            //M的价格
            double mPrice = Double.valueOf(POIUtils.getCellValue(sheet.getRow(rowNum).getCell(4)));
            return mPrice > nPrice ? 4 : 5;
        } else if (weight < 100) {
            //45
            return 6;
        } else if (weight < 300) {
            //100
            return 7;
        } else if (weight < 500) {
            //300
            return 8;
        } else if (weight < 1000) {
            //500
            return 9;
        } else if (weight < 2000) {
            //1000
            return 10;
        } else {
            //2000
            return 11;
        }
    }

    @Test
/**
 * 写出对账信息
 */
    public void writeStatement() throws ParseException, IOException {
        HashMap<Object, Object> hashMap = new HashMap<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date beginDate = dateFormat.parse("2018-03-26 00:00:00");
        Date endDate = dateFormat.parse("2018-04-01 23:59:59");
        hashMap.put("beginDate", beginDate);
        hashMap.put("endDate", endDate);
        hashMap.put("payer", "蓝景明");

        List<Billing> billingList = bd.selectBillingBetweenDateAndPayer(hashMap);

        Workbook statementWb = POIUtils.getWorkBook("statementExcel");
        Sheet statementSheet = statementWb.getSheetAt(0);
        statementSheet.getRow(8).getCell(0).setCellType(CellType.STRING);
        SimpleDateFormat sdf = new SimpleDateFormat("日期：yyyy年MM月dd日");
        statementSheet.getRow(8).getCell(0).setCellValue(sdf.format(new Date()));

        int row = 10;
        statementSheet.shiftRows(10, statementSheet.getLastRowNum(), billingList.size());
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd");
        for (int i = 0; i < billingList.size(); i++) {
            Billing billing = billingList.get(i);
            System.out.println(billing);
            Row tempRow = statementSheet.createRow(row++);
            CellUtil.createCell(tempRow, 0, billing.getPayer());
            CellUtil.createCell(tempRow, 1, billing.getReceiver());
            CellUtil.createCell(tempRow, 2, sdf1.format(billing.getDate()));
            CellUtil.createCell(tempRow, 3, billing.getFlight());
            CellUtil.createCell(tempRow, 4, billing.getDest());
            CellUtil.createCell(tempRow, 5, String.valueOf(billing.getPackagePiece()));
            CellUtil.createCell(tempRow, 6, billing.getWeight());
            CellUtil.createCell(tempRow, 7, "-");
            CellUtil.createCell(tempRow, 8, String.valueOf(billing.getPrice()));
            if (billing.getCost() != 0) {
                CellUtil.createCell(tempRow, 9, String.valueOf(billing.getCost()));
            } else {
                CellUtil.createCell(tempRow, 9, String.valueOf(billing.getFreightCollect()));
            }
            //TODO
            CellUtil.createCell(tempRow, 10, "-");
            if (billing.getCost() != 0) {
                CellUtil.createCell(tempRow, 11, String.valueOf(billing.getCost()));
            } else {
                CellUtil.createCell(tempRow, 11, String.valueOf(billing.getFreightCollect()));
            }
            if (billing.getRemark().contains("到付")) {
                if (billing.getCost() != 0) {
                    CellUtil.createCell(tempRow, 12, String.valueOf(billing.getCost()));
                } else {
                    CellUtil.createCell(tempRow, 12, String.valueOf(billing.getFreightCollect()));
                }
            } else {
                CellUtil.createCell(tempRow, 12, "-");
            }
            if (billing.getRemark().contains("到付")) {
                CellUtil.createCell(tempRow, 13, "合票到付");
            } else {
                CellUtil.createCell(tempRow, 13, "广州付");
            }
            CellUtil.createCell(tempRow, 14, billing.getDeliveryNo());
        }

        statementWb.write(new FileOutputStream("C:\\Users\\k1ng_\\Desktop\\CarefulCreateCellStyles.xlsx"));
        statementWb.close();


    }

    /**
     * 获取所需路径的对应日期子路径
     *
     * @param prePath  前缀路径的propertyKey
     * @param fileName 要创建的文件名
     * @return
     */
    public String getDatePath(String prePath, String fileName) {
        Calendar calendar = Calendar.getInstance();
        if (!Boolean.parseBoolean(billingProp.getProperty("today"))) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        SimpleDateFormat monthSdf = new SimpleDateFormat("MM");
        SimpleDateFormat dateSdf = new SimpleDateFormat("dd");
        prePath += File.separator + monthSdf.format(calendar.getTime()) + File.separator + dateSdf.format(calendar.getTime());
        File file = new File(prePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        if (fileName.endsWith(".xlsx")) {
            fileName = sdf.format(calendar.getTime()) + "-" + fileName;
        } else if (fileName.endsWith(".txt")) {
            fileName = sdf.format(calendar.getTime()) + "-" + fileName;
        }
        return prePath + File.separator + fileName;
    }

    @Test
    public void test() {
        System.out.println(6 % 5);
    }

}
