package controller;

import config.RootConfig;
import config.SpringBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import service.AirlineService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringBean.class, RootConfig.class})
public class AirlineController {
    @Autowired
    AirlineService as;

    @Test
    public void test(){

    }

}
