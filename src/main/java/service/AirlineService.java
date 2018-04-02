package service;

import config.RootConfig;
import config.SpringBean;
import dao.AirlineDao;
import dao.ICAODao;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pojo.Airline;
import pojo.ICAO;
import utils.POIUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringBean.class, RootConfig.class})
public class AirlineService extends BaseService {
    @Autowired
    AirlineDao ad;
    @Autowired
    ICAODao id;

    Properties billingProp;
    Properties pathProp;

    {
        billingProp = POIUtils.getProp("/billingExcel.properties");
        pathProp = POIUtils.getProp("/filePath.properties");
    }

    public Airline selectAirline(Airline airline) {
        airline = ad.selectAirline(airline);
        return ad.selectAirline(airline);
    }

    public void updateFlightTime(List<Airline> airlines) {
        ad.updateFlightTimeAndVia(airlines);
    }

    public void insertAirlines(List<Airline> airlines) {
        ad.insertAirlines(airlines);
    }

    //根据billing.发顺丰的航班号和目的地，获取当天或明天的航班起落时间和经停地点，并更新数据库
    @Test
    public void updateOrInsertFlightTimeAndVia() {
        Workbook wb = POIUtils.getWorkBook("formatExcel");
        Sheet dataSheet = wb.getSheet("数据表");
        List<Airline> list = new ArrayList<>();

        //判断是否读取得到document
        boolean getDoc = false;
        int count = 0;

        for (int i = 1; i <= dataSheet.getLastRowNum(); i++) {

            if (Boolean.parseBoolean((String) billingProp.get("proxy"))) {
                if (count % 15 == 0 || getDoc) {
                    System.out.println("使用代理i=" + i);
                    // 每5次获取代理ip
                    Document doc = null;
                    try {
                        doc = Jsoup.connect("http://tvp.daxiangdaili.com/ip/?tid=558427629519199&num=1").get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (doc == null) {
                        getDoc = true;
                        count = 0;
                        i--;
                        continue;
                    }
                    String[] split = doc.text().split(":");
                    //设置代理
                    //System.setProperty("http.proxySet", "true");
                    System.setProperty("http.proxyHost", split[0]);
                    System.setProperty("http.proxyPort", split[1]);
                    getDoc = false;
                }
            }

            count++;

            if (getCellValue(dataSheet.getRow(i), "航班")!=null&&(!(getCellValue(dataSheet.getRow(i), "航班").isEmpty() || getCellValue(dataSheet.getRow(i), "目的地").isEmpty()))) {
                //判断是否相同航班号和目的地
                boolean flag = true;
                Airline temp = new Airline();
                temp.setFlight(getCellValue(dataSheet.getRow(i), "航班"));
                temp.setDest(getCellValue(dataSheet.getRow(i), "目的地"));
                for (Airline airline : list) {
                    if (airline.equals(temp)) {
                        flag = false;
                        break;
                    }
                }
                //航班号和目的地不同则增加需要查看时间的Airling
                if (flag) {
                    list.add(temp);
                }
            }
        }

        Calendar calendar = Calendar.getInstance();
        //如果要生成的不是今天，则在日期上+1
        if (!Boolean.parseBoolean(billingProp.get("today").toString())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String html = "http://www.variflight.com/flight/fnum/{FLIGHT}.html?AE71649A58c77&fdate=" + sdf.format(calendar.getTime());

        Document doc;
        List<Airline> updateList = new ArrayList<>();
        List<Airline> insertList = new ArrayList<>();
        for (Airline airline : list) {
            try {
                //http://www.variflight.com/flight/fnum/CZ3099.html?AE71649A58c77&fdate=20180325
                doc = Jsoup.connect(html.replace("{FLIGHT}", airline.getFlight())).get();
                Element listEle = doc.getElementById("list");
                //如果查询不到航班就跳过
                if (listEle == null) {
                    continue;
                }

                Elements liList = listEle.getElementsByClass("li_com");


                Airline temp = new Airline();

                for (Element li : liList) {
                    //南方航空
                    temp.setCompany(li.child(0).child(1).child(0).text());
                    //CZ3601
                    temp.setFlight(li.child(0).child(1).child(1).text());
                    //08：05
                    try {
                        temp.setLaunch(Time.valueOf(LocalTime.parse(li.child(1).text())));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //广州白云B
                    temp.setDeparture(li.child(3).text());
                    //11：05
                    try {
                        temp.setArrival(Time.valueOf(LocalTime.parse(li.child(4).text())));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //青岛流亭T1
                    temp.setDest(li.child(6).text());

                    //出发地和目的地对上了就跳出循环
                    if (temp.getDeparture().contains("广州白云") && temp.getDest().contains(airline.getDest())) {
                        //如果通过航班号查询到多个航班那么就是有可能存在中转
                        if (liList.size() > 1) {
                            for (Element li1 : liList) {
                                //起点不是广州白云，那么这个就有可能是中转城市
                                if (!li1.child(3).text().contains("广州白云")) {
                                    //出发地和目的地不同，那么这就是中转的地方
                                    if (li1.child(3).text() != li.child(6).text()) {
                                        temp.setVia(li1.child(3).text());
                                    }
                                }
                            }
                        }
                        break;
                    }
                }

                if (ad.selectAirline(temp) == null) {
                    //出发地为广州才添加
                    if (temp.getDeparture().contains("广州白云")) {
                        insertList.add(temp);
                    }
                } else {
                    //出发地为广州才添加
                    if (temp.getDeparture().contains("广州白云")) {
                        updateList.add(temp);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (updateList.size() > 0) {
            System.out.println(updateList);
            ad.updateFlightTimeAndVia(updateList);
        }
        if (insertList.size() > 0) {
            System.out.println(insertList);
            ad.insertAirlines(insertList);
        }
        if (Boolean.parseBoolean(billingProp.get("today").toString())) {
            System.out.println("更新当天航班时间到数据库完成");
        } else {
            System.out.println("更新明天航班时间到数据库完成");
        }
    }

    @Test
    public void updateOrInsertAllFlightTimeAndWeekdays() {

        //判断是否读取得到document
        boolean getDoc = false;
        //如果count为15的倍数那么就获取一次代理ip
        int count = 0;

        List<Airline> airlineList = new ArrayList<>();
        List<ICAO> icaoList = id.selectAll();


        //循环一个星期的时间（获取每个航班一周那天有航班）
        //todo<=====这里是day正常是要改成1的！！！
        for (int day = 1; day < 8; day++) {
            Calendar calendar = Calendar.getInstance();
            //从今天开始遍历
            calendar.add(Calendar.DAY_OF_MONTH, day);
            Proxy proxy = null;
            for (int i = 0; i < icaoList.size(); i++) {
                System.out.println("i=" + i);
                if (Boolean.parseBoolean((String) billingProp.get("proxy"))) {
                    //15次或者获取不到页面doc的情况重新获取代理
                    if (count % 15 == 0 || !getDoc) {
                        proxy = useProxy();
                        getDoc = true;
                    }
                }
                count++;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String html = "http://www.variflight.com/flight/CAN-" + icaoList.get(i).getICAO() + ".html?AE71649A58c77&fdate=" + sdf.format(calendar.getTime());
                System.out.println(html);
                Document doc = null;
                List<Airline> updateList = new ArrayList<>();
                List<Airline> insertList = new ArrayList<>();
                try {
                    doc = Jsoup.connect(html)
                            .proxy(proxy)
                            //.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.167 Safari/537.36")
                            .get();
                    //停止0-1.5s
                    TimeUnit.MILLISECONDS.sleep(Math.round(Math.random() * 500));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //清零计数器，重新算15次
                    count = 0;
                    //获取网页失败
                    getDoc = false;
                    //重新这个网页获取
                    i--;
                    continue;
                } catch (IOException e) {
                    e.printStackTrace();
                    //清零计数器，重新算15次
                    count = 0;
                    //获取网页失败
                    getDoc = false;
                    //重新这个网页获取
                    i--;
                    continue;
                } catch (Error e) {
                    e.printStackTrace();
                    //清零计数器，重新算15次
                    count = 0;
                    //获取网页失败
                    getDoc = false;
                    //重新这个网页获取
                    i--;
                    continue;
                }
                //如果获取页面失败
                if (doc == null) {
                    //清零计数器，重新算15次
                    count = 0;
                    //获取网页失败
                    getDoc = false;
                    //重新这个网页获取
                    i--;
                    continue;
                } else {
                    //没有验证码的时候验证是否没有航班
                    //System.out.println("selector:"+doc.select(".authCodeBox[style=display: none;]").size());
                    if (doc.select(".authCodeBox[style=display: none;]").size() == 1) {
                        //判断是否当天没有航班，是则继续程序
                        if (doc.getElementsByClass("fly_nome").size() > 0) {
                            if (doc.getElementsByClass("fly_nome").get(0).getElementsByTag("p").get(0).text().equals("抱歉，没有找到您输入的航班信息")) {
                                //System.out.println(doc.toString());
                                System.out.println(icaoList.get(i).getDest() + "当天没有航班");
                                continue;
                            }
                        }
                    } else {
                        if (doc.text().contains("{\"msg\":\"IP blocked\"}")) {
                            System.out.println("IP被封锁");
                        } else {
                            //有验证码的时候重新获取代理
                            System.out.println("需要输入验证码");
                        }
                        //清零计数器，重新算15次
                        count = 0;
                        //获取网页失败
                        getDoc = false;
                        //重新这个网页获取
                        i--;
                        continue;
                    }
                }
                Element ulNode = doc.getElementById("list");
                //前面判断没有航班了，如果这里查询不到航班那么就是proxy获取网页出现问题，重置信息，重新获取网页
                if (ulNode == null) {
                    //清零计数器，重新算15次
                    count = 0;
                    //获取网页失败
                    getDoc = false;
                    //重新这个网页获取
                    i--;
                    continue;
                }
                Elements liList = ulNode.getElementsByTag("li");

                for (Element ele : liList) {
                    Airline temp = new Airline();
                    //5个a标签为正常航班，6个a标签为共享航班
                    if (ele.getElementsByTag("a").size() == 5) {
                        Element li = ele.getElementsByClass("li_com").get(0);
                        //南方航空
                        temp.setCompany(li.child(0).child(1).child(0).text());
                        //CZ3601
                        temp.setFlight(li.child(0).child(1).child(1).text());
                        //08：05
                        try {
                            temp.setLaunch(Time.valueOf(LocalTime.parse(li.child(1).text())));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //广州白云B
                        temp.setDeparture(li.child(3).text());
                        //11：05
                        try {
                            temp.setArrival(Time.valueOf(LocalTime.parse(li.child(4).text())));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //青岛流亭T1
                        temp.setDest(li.child(6).text());

                        //获取今天是星期几
                        Integer weekday = 0;
                        if (calendar.get(Calendar.DAY_OF_WEEK) - 1 == 0) {
                            weekday = 7;
                        } else {
                            weekday = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                        }
                        //设置临时存储航班的星期几有航班
                        temp.setWeekdays(weekday.toString());

                        //数据库没有则放到添加航班list里面否则放到更新list里面
                        if (ad.selectAirline(temp) == null) {
                            //System.out.println("insertList+"+temp);
                            insertList.add(temp);
                        } else {
                            //如果更新时间相差小于1天，那么setWeeks不用重置，否则直接把刚获得的星期几的值更新到数据库
                            Airline airline = ad.selectAirline(temp);
                            if (airline.getUpdateTime() == null || (new Date().getTime() - airline.getUpdateTime().getTime()) / (1000) < 24) {
                                //更新所有数据的时候，遍历星期几的时候，第一天把weekdays重置，第二至第七天concatString
                                if (airline.getWeekdays() == null || day == 1) {
                                    temp.setWeekdays(weekday.toString());
                                } else {
                                    //要判断字符串是否有重复，有则替代
                                    if (airline.getWeekdays().contains(weekday.toString())) {
                                        temp.setWeekdays(airline.getWeekdays().replace(weekday.toString(), ""));
                                        temp.setWeekdays(temp.getWeekdays().concat(weekday.toString()));
                                    } else {
                                        temp.setWeekdays(airline.getWeekdays().concat(weekday.toString()));
                                    }
                                }
                            }
                            //System.out.println("updateList+"+temp);
                            updateList.add(temp);
                        }
                    }
                }
                if (updateList.size() > 0) {
                    System.out.println("updateList:" + updateList);
                    ad.updateFlightTimeAndWeekdays(updateList);
                }
                if (insertList.size() > 0) {
                    System.out.println("insertList:" + insertList);
                    ad.insertAirlines(insertList);
                }

                System.out.println("目的地【" + icaoList.get(i).getDest() + "】的星期" + (calendar.get(Calendar.DAY_OF_WEEK) - 1) + "的航班更新完成(星期0是星期天)");

            }
        }




      /* for (Airline airline : airlineList) {
            try {
                //http://www.variflight.com/flight/fnum/CZ3099.html?AE71649A58c77&fdate=20180325
                doc = Jsoup.connect(html.replace("{FLIGHT}",airline.getFlight())).get();
                Element listEle = doc.getElementById("airlineList");
                //如果查询不到航班就跳过
                if(listEle==null){
                    continue;
                }

                Elements liList = listEle.getElementsByClass("li_com");


                Airline temp = new Airline();


                if(ad.selectAirline(temp)==null){
                    //出发地为广州才添加
                    if(temp.getDeparture().contains("广州白云")){
                        insertList.add(temp);
                    }
                }else{
                    //出发地为广州才添加
                    if(temp.getDeparture().contains("广州白云")){
                        updateList.add(temp);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (updateList.size() > 0) {
            System.out.println(updateList);
            ad.updateFlightTimeAndVia(updateList);
        }
        if (insertList.size() > 0) {
            System.out.println(insertList);
            ad.insertAirlines(insertList);
        }
        if (Boolean.parseBoolean(billingProp.get("today").toString())) {
            System.out.println("更新当天航班时间到数据库完成");
        } else {
            System.out.println("更新明天航班时间到数据库完成");
        }*/
    }

    /**
     * 使用代理
     */
    public Proxy useProxy() {

        //判断是否读取得到代理json的document
        boolean getDoc = false;
        Document doc = null;

        //如果获取不到proxyIP那么就继续获取
        while (getDoc == false) {
            System.out.println("正在获取代理");
            try {
                doc = Jsoup.connect("http://tvp.daxiangdaili.com/ip/?tid=558427629519199&num=1").get();
            } catch (IOException e) {
                System.out.println("获取代理失败，重新获取");
            }

            if (doc != null) {
                getDoc = true;
            }
        }
        String[] split = doc.text().split(":");
        //设置代理
        /*System.setProperty("http.proxySet", "true");
        System.setProperty("http.proxyHost", split[0]);
        System.setProperty("http.proxyPort", split[1]);*/
        Proxy proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(split[0], Integer.valueOf(split[1])));
        return proxy;
    }
}


