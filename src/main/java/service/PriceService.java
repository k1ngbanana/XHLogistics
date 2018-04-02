package service;

import config.RootConfig;
import config.SpringBean;
import dao.AirlineDao;
import dao.CostPriceDao;
import dao.LanshiPriceDao;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pojo.Airline;
import pojo.CostPrice;
import pojo.LanshiPrice;
import utils.CellStyleEnum;
import utils.POIUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringBean.class, RootConfig.class})
@Service
public class PriceService {

    @Autowired
    CostPriceDao cd;
    @Autowired
    LanshiPriceDao ld;
    @Autowired
    AirlineDao ad;

    Properties billingProp;
    Properties pathProp;

    {
        billingProp = POIUtils.getProp("/billingExcel.properties");
        pathProp = POIUtils.getProp("/filePath.properties");
    }

    @Test
    //将格式化价格.xlsx里面的“成本价”插入到数据库（每次truncate一次数据库）
    public void insertPrice() throws ParseException {

        //清空成本数据库，以重新插入新的数据
        cd.truncatePrice();

        Workbook costWb = POIUtils.getWorkBook("costExcel");
        Sheet costSheet = costWb.getSheet("成本价");

        List<CostPrice> costList = new ArrayList<>();
        //用于转换excel表格中的日期
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

        for (int i = 1; i <= costSheet.getLastRowNum(); i++) {
            CostPrice costPrice = new CostPrice();
            costPrice.setDest(POIUtils.getCellValue(costSheet.getRow(i).getCell(0)));
            costPrice.setFlight(POIUtils.getCellValue(costSheet.getRow(i).getCell(1)));
            //将excel中date日期parse出来通过instant转换成localTime格式再转成Time格式
            if (!POIUtils.getCellValue(costSheet.getRow(i).getCell(2)).isEmpty()) {
                Date tempDate1 = sdf.parse(POIUtils.getCellValue(costSheet.getRow(i).getCell(2)));
                Instant instant1 = tempDate1.toInstant();
                LocalDateTime ldt1 = LocalDateTime.ofInstant(instant1, ZoneId.systemDefault());
                costPrice.setLaunch(Time.valueOf(ldt1.toLocalTime()));

                Date tempDate2 = sdf.parse(POIUtils.getCellValue(costSheet.getRow(i).getCell(3)));
                Instant instant2 = tempDate2.toInstant();
                LocalDateTime ldt2 = LocalDateTime.ofInstant(instant2, ZoneId.systemDefault());
                costPrice.setArrival(Time.valueOf(ldt2.toLocalTime()));
            }


            for (int j = 4; j < 12; j++) {
                if (costSheet.getRow(i).getCell(j) != null && !POIUtils.getCellValue(costSheet.getRow(i).getCell(j)).equals("")) {
                    if (POIUtils.getCellValue(costSheet.getRow(i).getCell(j)).matches("[\\d].*")) {
                        switch (j) {
                            case 4:
                                costPrice.setM(Double.valueOf(POIUtils.getCellValue(costSheet.getRow(i).getCell(j))));
                                break;
                            case 5:
                                costPrice.setN(Double.valueOf(POIUtils.getCellValue(costSheet.getRow(i).getCell(j))));
                                break;
                            case 6:
                                costPrice.setP45(Double.valueOf(POIUtils.getCellValue(costSheet.getRow(i).getCell(j))));
                                break;
                            case 7:
                                costPrice.setP100(Double.valueOf(POIUtils.getCellValue(costSheet.getRow(i).getCell(j))));
                                break;
                            case 8:
                                costPrice.setP300(Double.valueOf(POIUtils.getCellValue(costSheet.getRow(i).getCell(j))));
                                break;
                            case 9:
                                costPrice.setP500(Double.valueOf(POIUtils.getCellValue(costSheet.getRow(i).getCell(j))));
                                break;
                            case 10:
                                costPrice.setP1000(Double.valueOf(POIUtils.getCellValue(costSheet.getRow(i).getCell(j))));
                                break;
                            case 11:
                                costPrice.setP2000(Double.valueOf(POIUtils.getCellValue(costSheet.getRow(i).getCell(j))));
                                break;
                        }
                    }
                }
            }
            costList.add(costPrice);
            //每50个插入一次或者最后一批list的时候插入
            if (i % 50 == 0 || i == costSheet.getLastRowNum()) {
                cd.insertPrices(costList);
                costList.clear();
            }
        }
        System.out.println("更新成本价格表到数据库成功");
    }

    @Test
    /**
     * 将45k的价格+0.5存入数据库
     * 将最低等级价格+0.7存入数据库
     * 并将"格式化价格"里面的手动设置价格更新并将flag设置为true
     */
    public void insertLanshiPrice() {
        Workbook exteranlWb = POIUtils.getWorkBook("exteranlPrice");
        Sheet lanshiOriginSheet = exteranlWb.getSheet("蓝氏原始价");

        ld.truncatePrice();

        ArrayList<CostPrice> cpList = cd.selectAllPrice();
        ArrayList<LanshiPrice> lpList = new ArrayList<>();

        for (int i = 0; i < cpList.size(); i++) {
            LanshiPrice lp = new LanshiPrice();
            CostPrice cp = cpList.get(i);
            double additionalPrice = 0.7;

            lp.setDest(cp.getDest());
            lp.setFlight(cp.getFlight());
            if (cp.getP2000() != 0) {
                lp.setBeyond100(cp.getP2000());
            } else if (cp.getP1000() != 0) {
                lp.setBeyond100(cp.getP1000());
            } else if (cp.getP500() != 0) {
                lp.setBeyond100(cp.getP500());
            } else if (cp.getP300() != 0) {
                lp.setBeyond100(cp.getP300());
            } else if (cp.getP100() != 0) {
                lp.setBeyond100(cp.getP100());
            } else if (cp.getP45() != 0) {
                lp.setBeyond100(cp.getP45());
            } else if (cp.getN() != 0) {
                lp.setBeyond100(cp.getN());
            } else {
                lp.setBeyond100(cp.getM());
            }

            BigDecimal bg = new BigDecimal(lp.getBeyond100() + additionalPrice);
            double temp = bg.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            lp.setBeyond100(temp);
            if (cp.getP45() == 0) {
                lp.setUnder100(cp.getN()+0.5);
            } else {
                lp.setUnder100(cp.getP45()+0.5);
            }
            lpList.add(lp);

            //每50个价格插入一次数据库或最后一批list插入数据库
            if (i % 50 == 0 || i == (cpList.size() - 1)) {
                ld.insertPrices(lpList);
                lpList.clear();
            }
        }

        Workbook lanshiWb = POIUtils.getWorkBook("costExcel");
        Sheet lanshiSheet = lanshiWb.getSheet("蓝氏");

        //获取"实际价格"列的列号
        int colNum = 0;
        for (int i = 0; i < lanshiSheet.getRow(0).getLastCellNum(); i++) {
            if (POIUtils.getCellValue(lanshiSheet.getRow(0).getCell(i)).equals("实际价格")) {
                colNum = i;
                break;
            }
        }

        List<LanshiPrice> updateList = new ArrayList<>();
        for (int i = 1; i <= lanshiSheet.getLastRowNum(); i++) {
            if (lanshiSheet.getRow(i).getCell(colNum) != null) {
                if (POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(colNum)).matches("[\\d].*")) {
                    LanshiPrice lp = new LanshiPrice();
                    lp.setDest(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(0)));
                    lp.setFlight(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(1)));
                    lp = selectPrice(lp);
                    lp.setBeyond100(Double.valueOf(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(colNum))));
                    lp.setFlag(true);
                    updateList.add(lp);
                }
            }
        }
        ld.updatePriceAndFlag(updateList);


        System.out.println("蓝氏原始价格插入数据库成功<---(最低价+0.7)||实际价格");

    }

/*    @Test
    public void writeUnder100Price() throws IOException {
        Workbook lanshiWb = POIUtils.getWorkBook("costExcel");
        Sheet lanshiSheet = lanshiWb.getSheet("蓝氏");
        for (int i = 1; i <= lanshiSheet.getLastRowNum(); i++) {
            CostPrice cp = new CostPrice();
            cp.setDest(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(0)));
            cp.setFlight(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(1)));
            cp = selectPrice(cp);
            if (lanshiSheet.getRow(i).getCell(7) == null) {
                lanshiSheet.getRow(i).createCell(7, CellType.NUMERIC);
            }

            //如果45kg没有价格那么用N的价格
            if (cp.getP45() == 0) {
                lanshiSheet.getRow(i).getCell(7).setCellValue(cp.getN() + 0.5);
            } else {
                lanshiSheet.getRow(i).getCell(7).setCellValue(cp.getP45() + 0.5);
            }
        }

        //输出文件到格式化价格.xlsx
        FileOutputStream fos = new FileOutputStream(POIUtils.getFilePath("costExcel"));
        lanshiWb.write(fos);
        fos.close();

    }*/

    @Test
    public void writeLanshiPrice() throws IOException {
        Workbook lanshiWb = POIUtils.getWorkBook("costExcel");
        Sheet lanshiSheet = lanshiWb.getSheet("蓝氏");
        for (int i = 1; i <= lanshiSheet.getLastRowNum(); i++) {
            if (lanshiSheet.getRow(i).getCell(8) == null) {
                lanshiSheet.getRow(i).createCell(8, CellType.NUMERIC);
            }
            LanshiPrice lp = new LanshiPrice();
            lp.setDest(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(0)));
            lp.setFlight(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(1)));

            lanshiSheet.getRow(i).getCell(8).setCellValue(selectPrice(lp).getBeyond100());
            System.out.println(selectPrice(lp));
        }

        //输出文件到格式化价格.xlsx
        FileOutputStream fos = new FileOutputStream(POIUtils.getFilePath("costExcel"));
        lanshiWb.write(fos);
        fos.close();
    }

    /**
     * 通过costPrice的dest和flight来获取对应的价格
     *
     * @param costPrice
     * @return
     */
    public CostPrice selectPrice(CostPrice costPrice) {
        CostPrice cp = new CostPrice();
        cp = cd.selectPriceByFlight(costPrice);
        if (cp == null) {
            HashMap<String, String> hashMap = new HashMap();
            hashMap.put("eCode", costPrice.getFlight().substring(0, 2));
            hashMap.put("dest", costPrice.getDest());
            cp = cd.selectPriceByOther(hashMap);
        }
        return cp;
    }

    /**
     * 通过lanshiPrice的dest和flight来获取对应的价格
     *
     * @param lanshiPrice
     * @return
     */
    public LanshiPrice selectPrice(LanshiPrice lanshiPrice) {
        LanshiPrice lp = new LanshiPrice();
        lp = ld.selectPriceByFlight(lanshiPrice);
        if (lp == null) {
            HashMap<String, String> hashMap = new HashMap();
            hashMap.put("eCode", lanshiPrice.getFlight().substring(0, 2));
            hashMap.put("dest", lanshiPrice.getDest());
            lp = ld.selectPriceByOther(hashMap);
        }
        return lp;
    }

    @Test
    public void writeAllExternalPrice() throws Exception {
        Workbook externalWb = POIUtils.getWorkBook("externalPrice");
        Sheet lanshiSheet = externalWb.getSheet("蓝氏价格表");

        LanshiPrice lanshiPrice = new LanshiPrice();

        List<Integer> listCol4Red = new ArrayList<>();
        List<Integer> listCol5Red = new ArrayList<>();
        List<Integer> listCol4Blue = new ArrayList<>();
        List<Integer> listCol5Blue = new ArrayList<>();

        for(int i = 1; i <= lanshiSheet.getLastRowNum(); i++){
            Airline airline = new Airline();
            airline.setDest(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(0)));
            lanshiPrice.setDest(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(0)));
            airline.setFlight(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(1)));
            lanshiPrice.setFlight(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(1)));
            airline = ad.selectAirline(airline);
            lanshiPrice = ld.selectPriceByFlight(lanshiPrice);

            if(airline!=null){
                POIUtils.isNullCreate(lanshiSheet, i, 2, CellType.NUMERIC);
                POIUtils.isNullCreate(lanshiSheet, i, 3, CellType.NUMERIC);
                lanshiSheet.getRow(i).getCell(2).setCellValue(airline.getLaunch());
                lanshiSheet.getRow(i).getCell(3).setCellValue(airline.getArrival());
            }
            if(lanshiPrice!=null){
                POIUtils.isNullCreate(lanshiSheet, i, 4, CellType.NUMERIC);
                POIUtils.isNullCreate(lanshiSheet, i, 5, CellType.NUMERIC);
                if(!POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(4)).isEmpty()){
                    if(lanshiPrice.getUnder100()<Double.valueOf(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(4)))){
                        //需要设置为
                        listCol4Blue.add(i);
                    }else if(lanshiPrice.getUnder100()>Double.valueOf(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(4)))){
                        listCol4Red.add(i);
                    }
                }
                if(!POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(5)).isEmpty()){
                    if(lanshiPrice.getBeyond100()<Double.valueOf(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(5)))){
                        //需要设置为
                        listCol5Blue.add(i);
                    }else if(lanshiPrice.getBeyond100()>Double.valueOf(POIUtils.getCellValue(lanshiSheet.getRow(i).getCell(5)))){
                        listCol5Red.add(i);
                    }
                }
                lanshiSheet.getRow(i).getCell(4).setCellValue(lanshiPrice.getUnder100());
                lanshiSheet.getRow(i).getCell(5).setCellValue(lanshiPrice.getBeyond100());
            }
        }

        //设置2，3列为时间格式，0-6列为有边框
        HashMap<CellStyleEnum, int[]> styleMap = new HashMap<>();
        styleMap.put(CellStyleEnum.TIME, new int[]{2,3});
        styleMap.put(CellStyleEnum.ALL_BORDER_THIN, new int[]{0,1,2,3,4,5});
        POIUtils.setCellSytleByMap(lanshiSheet, styleMap);

        //设置价格颜色
        HashMap<CellStyleEnum, Map<Integer,Integer[]>> hashMap1 = new HashMap<>();
        HashMap<Integer,Integer[]> map1 = new HashMap<>();
        Integer[] arr1 = new Integer[listCol4Blue.size()];
        map1.put(4, listCol4Blue.toArray(arr1));
        hashMap1.put(CellStyleEnum.BLUE_BOLD, map1);
        POIUtils.setCellSytleByMap(hashMap1, lanshiSheet);

        HashMap<CellStyleEnum, Map<Integer,Integer[]>> hashMap2 = new HashMap<>();
        HashMap<Integer,Integer[]> map2 = new HashMap<>();
        Integer[] arr2 = new Integer[listCol4Red.size()];
        map2.put(4, listCol4Red.toArray(arr2));
        hashMap2.put(CellStyleEnum.RED_BOLD, map2);
        POIUtils.setCellSytleByMap(hashMap2, lanshiSheet);

        HashMap<CellStyleEnum, Map<Integer,Integer[]>> hashMap3 = new HashMap<>();
        HashMap<Integer,Integer[]> map3 = new HashMap<>();
        Integer[] arr3 = new Integer[listCol5Blue.size()];
        map3.put(5, listCol5Blue.toArray(arr3));
        hashMap3.put(CellStyleEnum.BLUE_BOLD, map3);
        POIUtils.setCellSytleByMap(hashMap3, lanshiSheet);

        HashMap<CellStyleEnum, Map<Integer,Integer[]>> hashMap4 = new HashMap<>();
        HashMap<Integer,Integer[]> map4 = new HashMap<>();
        Integer[] arr4 = new Integer[listCol5Red.size()];
        map4.put(5, listCol5Red.toArray(arr4));
        hashMap4.put(CellStyleEnum.RED_BOLD, map4);
        POIUtils.setCellSytleByMap(hashMap4, lanshiSheet);


        //输出文件到格式化价格.xlsx
        FileOutputStream fos = new FileOutputStream(POIUtils.getFilePath("testExcel"));
        externalWb.write(fos);
        fos.close();

    }


    @Test
    public void test2() throws Exception {
        Workbook workbook = POIUtils.getWorkBook("testExcel");
        Sheet sheet = workbook.getSheetAt(0);
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(3);
        list.add(5);

        HashMap<CellStyleEnum, Map<Integer,Integer[]>> hashMap = new HashMap<>();
        HashMap<Integer,Integer[]> map = new HashMap<>();
        Integer[] arr = new Integer[list.size()];
        map.put(4, list.toArray(arr));
        hashMap.put(CellStyleEnum.RED_BOLD, map);
        POIUtils.setCellSytleByMap(hashMap, sheet);

        workbook.write(new FileOutputStream("C:\\Users\\k1ng_\\Desktop\\CarefulCreateCellStyles.xlsx"));
        workbook.close();

    }

}
