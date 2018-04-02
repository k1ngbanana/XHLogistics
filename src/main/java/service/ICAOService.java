package service;

import config.RootConfig;
import config.SpringBean;
import dao.ICAODao;
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
import pojo.ICAO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringBean.class, RootConfig.class})
public class ICAOService {
    @Autowired
    ICAODao id;

    @Test
    public void insertICAOs() throws IOException {
        File file = new File("C:\\Users\\k1ng_\\Desktop\\2.txt");
        Document docuument = Jsoup.parse(file,"UTF-8");
        Elements elements = docuument.getElementsByTag("span");
        List<ICAO> list = new ArrayList<>();
        for(Element ele : elements){
            ICAO icao = new ICAO();
            icao.setDest(ele.text());
            icao.setICAO(ele.attr("code"));
            list.add(icao);
        }
        System.out.println(list);
        id.insertICAOs(list);
    }
}
