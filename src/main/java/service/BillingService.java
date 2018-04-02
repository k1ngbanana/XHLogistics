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
public class BillingService extends BaseService{

    @Autowired
    BillingDao bd;
    @Autowired
    AirlineDao ad;
    @Autowired
    AirlineService as;
    @Autowired
    PriceService ps;


    POIUtils poiUtils = new POIUtils();



    //【骏升空运】尊敬的客户，您好！广州-长春CZ6378航班预计08:10起飞，
    // 路营1件24公斤，到付每公斤10元，提货电话0431-85856548，提货单号：38986603
    @Test
    /**
     * 自动完成骏升格式的SMS文件
     */
    public void junShentSMS() throws ParseException {
        Workbook wb = poiUtils.getWorkBook("formatExcel");
        Sheet dataSheet = wb.getSheet("数据表");
        //Properties billingProp = poiUtils.getProp("/billingExcel.properties");

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= dataSheet.getLastRowNum(); i++) {
            sb.append(i + ".\n");
            sb.append("【薛航物流】尊敬的");
            sb.append(getCellValue(dataSheet.getRow(i), "收货人"));
            sb.append("，您好！广州-");
            sb.append(getCellValue(dataSheet.getRow(i), "目的地"));
            sb.append(getCellValue(dataSheet.getRow(i), "航班"));
            sb.append("航班预计");
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            Date date = sdf.parse(getCellValue(dataSheet.getRow(i), "计划起飞"));
            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
            sb.append(sdf2.format(date));
            sb.append("起飞，");
            sb.append(Double.valueOf(getCellValue(dataSheet.getRow(i), "件数")).intValue());
            sb.append("件");
            Double weight = Double.valueOf(getCellValue(dataSheet.getRow(i), "重量"));
            sb.append(Math.round(weight + 0.2));
            sb.append("公斤，");
            if (getCellValue(dataSheet.getRow(i), "费用") == "" || getCell(dataSheet.getRow(i), "费用") == null) {
                sb.append("到付每公斤");
                sb.append(getCellValue(dataSheet.getRow(i), "单价"));
                sb.append("元，共");
                sb.append(Double.valueOf(getCellValue(dataSheet.getRow(i), "到付")).intValue());
                sb.append("元");
            } else {
                sb.append("广州付每公斤");
                sb.append(getCellValue(dataSheet.getRow(i), "单价"));
                sb.append("元，共");
                sb.append(Double.valueOf(getCellValue(dataSheet.getRow(i), "费用")).intValue());
                sb.append("元");
            }


            if (getCellValue(dataSheet.getRow(i), "提货电话") != "" && getCell(dataSheet.getRow(i), "提货电话") != null) {
                sb.append("，提货电话：");
                sb.append(getCellValue(dataSheet.getRow(i), "提货电话"));
            }
            if (getCellValue(dataSheet.getRow(i), "单号") != "" && getCell(dataSheet.getRow(i), "单号") != null) {
                sb.append("，提货单号：");
                sb.append(getCellValue(dataSheet.getRow(i), "单号"));
            }
            sb.append("\n");
        }
        File file = new File(pathProp.get("lanshiConfirmInfo").toString());
        try {
            FileWriter fw = new FileWriter(file, false);
            fw.write(sb.toString());
            fw.close();
            System.out.println(pathProp.get("lanshiConfirmInfo").toString() + "输出完成");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Test
    /**
     * 自动完成billing发顺丰表格
     * 以蓝氏兄弟的为准
     */
    public void autoCompleteBilling() throws IOException {
        Workbook wb = POIUtils.getWorkBook("formatExcel");
        Workbook costWb = POIUtils.getWorkBook("costExcel");
        Sheet dataSheet = wb.getSheet("数据表");
        Sheet lanshiSheet = costWb.getSheet("蓝氏");
        Sheet costSheet = costWb.getSheet("成本价");


        Properties billingProp = POIUtils.getProp("/billingExcel.properties");

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
        baseFont.setFontHeightInPoints((short)11);
        baseFont.setFontName("等线");
        baseStyle.setFont(baseFont);

        timeStyle.cloneStyleFrom(baseStyle);
        timeStyle.setDataFormat(createHelper.createDataFormat().getFormat("HH:mm"));
        dateStyle.cloneStyleFrom(baseStyle);
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy/MM/dd"));




        for (int i = 1; i <= dataSheet.getLastRowNum(); i++) {

            Row row = dataSheet.getRow(i);
            double freightWeight = 0;

            if(airline == null){
                airline = new Airline();
            }
            if(lanshiPrice == null){
                lanshiPrice = new LanshiPrice();
            }
            if(costPrice == null){
                costPrice = new CostPrice();
            }

            if(!(getCellValue(row, "航班").isEmpty()&&getCellValue(row, "目的地").isEmpty())){
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
            if(airline==null){
                System.out.println("第"+(i+1)+"行航班信息不完整或错误，请查看【航班号】和【目的地】是否都已经填写正确");
            }else if(airline.getFlight().isEmpty()|| airline.getDest().isEmpty()){
                System.out.println("第"+(i+1)+"行航班信息不完整，请查看航班【"+airline.getFlight()+"】和目的地【"+airline.getDest()+"】是否都已经填写完整");
            }

            //日期
            Cell dateCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "日期"),CellType.NUMERIC);
            //起飞时间
            Cell launchCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "计划起飞"),CellType.NUMERIC);
            //到达时间
            Cell arrivalCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "计划到达"),CellType.NUMERIC);
            //单价
            Cell priceCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "单价"),CellType.STRING);
            //费用
            Cell costCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "费用"),CellType.NUMERIC);
            //到付
            Cell freightCollectCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "到付"),CellType.NUMERIC);
            //件数
            Cell pieaceCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "件数"), CellType.NUMERIC);
            //毛重
            Cell grossWeightCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "毛重"), CellType.NUMERIC);
            //计费重量
            Cell chargeWeightCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "计费重量"), CellType.NUMERIC);
            //费率
            Cell chargeRateCell = POIUtils.isNullCreate(dataSheet, i, POIUtils.getColumnNum(billingProp, "费率"), CellType.NUMERIC);


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
            if(airline!=null){
                if (POIUtils.getCellValue(launchCell).equals("")) {
                    if(airline.getLaunch()!=null){
                        launchCell.setCellValue(airline.getLaunch());
                    }
                    launchCell.setCellStyle(timeStyle);
                }
                if (POIUtils.getCellValue(arrivalCell).equals("")) {
                    if(airline.getArrival()!=null){
                        arrivalCell.setCellValue(airline.getArrival());
                    }
                    arrivalCell.setCellStyle(timeStyle);
                }
            }

            //如果没有重量，那么插入蓝氏100kg以下和100kg以上价格
            lanshiPrice = ps.selectPrice(lanshiPrice);
            if(lanshiPrice!=null) {
                if (getCell(row, "重量") == null || getCellValue(row, "重量").isEmpty()) {
                    priceCell.setCellValue(lanshiPrice.getUnder100() + "/" + lanshiPrice.getBeyond100());
                }else{
                    //计算该行合票重量
                    for(int j = 0 ; j < dataSheet.getLastRowNum(); j++){
                        if(getCellValue(row, "航班").equals(getCellValue(dataSheet.getRow(j), "航班"))){
                            if(getCellValue(row, "目的地").equals(getCellValue(dataSheet.getRow(j), "目的地"))){
                                if(getCellValue(dataSheet.getRow(j), "备注").contains("合票")){
                                    freightWeight += Double.valueOf(getCellValue(dataSheet.getRow(j), "重量"));
                                }
                            }
                        }
                    }

                    System.out.println((i+1)+"行的合票重量为："+freightWeight);


                    //填充单价
                    if(Double.valueOf(getCellValue(row, "重量"))<99.3){
                        priceCell.setCellValue(lanshiPrice.getUnder100());
                    }else{
                        priceCell.setCellValue(lanshiPrice.getBeyond100());
                    }

                    //有重量，没有备注或者没有【到付】备注的时候，价格全部填入费用一栏，否则填入到付
                    //重量 0.2进位，费用 0.3进位<--可能会存在精度问题
                    long cost = Math.round(Math.round(Double.valueOf(getCellValue(row, "重量"))+0.3)*Double.valueOf(POIUtils.getCellValue(priceCell))+0.2);
                    if(!POIUtils.getCellValue(priceCell).isEmpty()&&POIUtils.getCellValue(priceCell).matches("[\\d].*")){
                        if(getCell(row, "备注") == null || getCellValue(row, "备注").isEmpty()||!getCellValue(row, "备注").contains("到付")){
                            costCell.setCellValue(cost);
                        }else{
                            freightCollectCell.setCellValue(cost);
                        }
                    }

                    //按照公式的"+"号数量+1作为件数
                    int piece = 1;
                    if(getCell(row, "重量").getCellTypeEnum()==CellType.FORMULA){
                        String formula = getCell(row, "重量").getCellFormula();
                        piece = StringUtils.countMatches(formula, "+")+1;
                    }
                    //如果有重量，填入件数
                    if(POIUtils.getCellValue(pieaceCell).isEmpty()){
                        pieaceCell.setCellValue(piece);
                    }else{
                        if(Double.valueOf(getCellValue(row, "件数"))!=piece){
                            POIUtils.setCellComment(pieaceCell, "重量如果是用公式填写的话，那么件数可能不对，计算的件数是【"+piece+"件】");
                        }
                    }

                    //写入毛重，0.7进位
                    long grossWeight = Math.round(Double.valueOf(getCellValue(row, "重量"))-0.2);
                    if(POIUtils.getCellValue(grossWeightCell).isEmpty()){
                        grossWeightCell.setCellValue(grossWeight);
                    }


                    //写入计费重量
                    costPrice = ps.selectPrice(costPrice);
                    if(costPrice!=null){
                        chargeWeightCell.setCellValue(grossWeight);
                        //如果是南航
                        if(costPrice.getFlight().contains("CZ")){
                            //大于40kg的话，对比45kg还是40kg便宜&3099不要抛或者有合票的航班不要抛
                            if(!(costPrice.getFlight().equals("CZ3099")&&freightWeight-grossWeight>3)){
                                if(grossWeight>39 && grossWeight<45){
                                    if(grossWeight*costPrice.getN()> 45*costPrice.getP45()){
                                        //南航，在39kg到45kg之间，如果45kg便宜，那么偷偷抛到45kg
                                        chargeWeightCell.setCellValue(45);
                                    }
                                }
                                //南航，在95kg到100kg之间，如果100kg便宜，那么偷偷抛到100kg
                                if(grossWeight>94 && grossWeight<100){
                                    if(grossWeight*costPrice.getP45() > 100*costPrice.getP100()){
                                        chargeWeightCell.setCellValue(100);
                                    }
                                }
                            }else{
                                //我暂时认为没有一件货少于3kg的。大于3即有合票
                                if(freightWeight-grossWeight>3){
                                    POIUtils.setCellComment(chargeWeightCell, "合票重量为"+freightWeight+"kg");
                                }
                            }
                        }else{
                            //不是南航的并且没有合票的费率
                            int minRateNum=0;
                            double minPrice = 0;
                            HashMap<Integer, Double> hashMap = new HashMap<>();
                            if(grossWeight<100){
                                hashMap.put(1, costPrice.getM());
                                if(grossWeight<45){
                                    hashMap.put(2, 45*costPrice.getP45());
                                    hashMap.put(3, grossWeight*costPrice.getN());
                                }else{
                                    hashMap.put(4, grossWeight*costPrice.getP45());
                                }
                                hashMap.put(5, 100*costPrice.getP100());
                            }else if(grossWeight<300){
                                hashMap.put(6, grossWeight*costPrice.getP100());
                                hashMap.put(7, 300*costPrice.getP300());
                            }else if(grossWeight<500){
                                hashMap.put(8, grossWeight*costPrice.getP300());
                                hashMap.put(9, 500*costPrice.getP500());
                            }else if(grossWeight<1000){
                                hashMap.put(10, grossWeight*costPrice.getP500());
                                hashMap.put(11, 1000*costPrice.getP1000());
                            }

                            for(Map.Entry<Integer,Double> entry : hashMap.entrySet()){
                                if(minRateNum==0){
                                    minRateNum = entry.getKey();
                                    minPrice = entry.getValue();
                                }else{
                                    if(entry.getValue()==0){
                                        continue;
                                    }else{
                                        if(entry.getValue()<minPrice){
                                            minRateNum = entry.getKey();
                                        }
                                    }
                                }
                            }

                            switch (minRateNum){
                                case 1:
                                    break;

                            }





                        }

                    }else{
                        System.out.println("没有找到对应成本价格，请确认成本价格表是否已更新到数据库");
                    }




                }
            }else{
                System.out.println("没有找到对应鱼商价格，请确认蓝氏价格表是否已更新到数据库");
            }



        }

        //输出文件到格式化价格.xlsx
        FileOutputStream fos = new FileOutputStream(poiUtils.getFilePath("testExcel"));
        wb.write(fos);
        fos.close();
        System.out.println("billing发顺丰.xlsx自动填充完成");

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
                if(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("费率")))).contains("M")){
                    billing.setBaseRate(Double.valueOf(POIUtils.getCellValue(row.getCell(Integer.valueOf((String) billingProp.get("费率")))).replace("M/", "")));
                }else{
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
    public void writeStatement() throws ParseException, IOException {
        HashMap<Object,Object> hashMap = new HashMap<>();
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
        for(int i = 0; i <   billingList.size(); i++){
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
            if(billing.getCost()!=0){
                CellUtil.createCell(tempRow, 9, String.valueOf(billing.getCost()));
            }else{
                CellUtil.createCell(tempRow, 9, String.valueOf(billing.getFreightCollect()));
            }
            //TODO
            CellUtil.createCell(tempRow, 10, "-");
            if(billing.getCost()!=0){
                CellUtil.createCell(tempRow, 11, String.valueOf(billing.getCost()));
            }else{
                CellUtil.createCell(tempRow, 11, String.valueOf(billing.getFreightCollect()));
            }
            if(billing.getRemark().contains("到付")){
                if(billing.getCost()!=0){
                    CellUtil.createCell(tempRow, 12, String.valueOf(billing.getCost()));
                }else{
                    CellUtil.createCell(tempRow, 12, String.valueOf(billing.getFreightCollect()));
                }
            }else{
                CellUtil.createCell(tempRow, 12, "-");
            }
            if(billing.getRemark().contains("到付")){
                CellUtil.createCell(tempRow, 13, "合票到付");
            }else{
                CellUtil.createCell(tempRow, 13, "广州付");
            }
            CellUtil.createCell(tempRow, 14, billing.getDeliveryNo());
        }

        statementWb.write(new FileOutputStream("C:\\Users\\k1ng_\\Desktop\\CarefulCreateCellStyles.xlsx"));
        statementWb.close();



    }

    @Test
    public void test() {

    }

}
