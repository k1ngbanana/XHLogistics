package controller;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import service.BillingService;

public class BillingController {

    @Autowired
    BillingService bs;

    //插入或更新所有在"插入数据库"表格的数据到数据库
    @Test
    public void insertAndUpdateAll(){
        try {
            bs.insertAndUpdateAllBilling();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void autoCompleteBilling(){

    }

    //蓝景明确认信息
    public void lanshiConfirmInfo(){

    }

}
