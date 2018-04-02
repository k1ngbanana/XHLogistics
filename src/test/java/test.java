import config.RootConfig;
import config.SpringBean;
import dao.AirlineDao;
import dao.CostPriceDao;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pojo.Airline;
import pojo.CostPrice;

import java.io.IOException;
import java.sql.Time;
import java.time.LocalTime;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringBean.class, RootConfig.class})
public class test {

    @Autowired
    AirlineDao ad;
    @Autowired
    CostPriceDao cd;

    @Test
    public void test1() throws IOException {
        Document doc  = Jsoup.connect("http://www.variflight.com/flight/CAN-SHA.html?AE71649A58c77&fdate=20180329").get();
        Element ulNode = doc.getElementById("list");
        Elements eles = ulNode.getElementsByTag("li");
        for(Element ele : eles){
            //5个a标签为正常航班，6个a标签为共享航班
            if(ele.getElementsByTag("a").size()==5){
                Airline temp = new Airline();
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

            }
        }

    }

    @Test
    public void test2() throws InterruptedException {
        CostPrice costPrice = new CostPrice();
        costPrice.setDest("北京");
        costPrice.setFlight("CZ3099");
        System.out.println(cd.selectPriceByFlight(costPrice));


    }
}
