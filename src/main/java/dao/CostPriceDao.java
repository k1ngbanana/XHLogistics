package dao;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import pojo.CostPrice;
import pojo.mapper.CostPriceMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class CostPriceDao implements CostPriceMapper {

    @Autowired
    SqlSessionFactory ssf;

    @Override
    public void insertPrices(List<CostPrice> costPriceList) {
        // 打开session
        SqlSession session = ssf.openSession();
        CostPriceMapper mapper = session.getMapper(CostPriceMapper.class);
        mapper.insertPrices(costPriceList);
        session.commit();
        session.close();
    }

    @Override
    public void truncatePrice() {
        // 打开session
        SqlSession session = ssf.openSession();
        CostPriceMapper mapper = session.getMapper(CostPriceMapper.class);
        mapper.truncatePrice();
        session.commit();
        session.close();
    }

    @Override
    public ArrayList<CostPrice> selectAllPrice() {
        // 打开session
        SqlSession session = ssf.openSession();
        CostPriceMapper mapper = session.getMapper(CostPriceMapper.class);
        ArrayList<CostPrice> priceList = mapper.selectAllPrice();
        session.close();
        return priceList;
    }

    @Override
    public CostPrice selectPriceByFlight(CostPrice cp) {
        // 打开session
        SqlSession session = ssf.openSession();
        CostPriceMapper mapper = session.getMapper(CostPriceMapper.class);
        cp = mapper.selectPriceByFlight(cp);
        session.close();
        return cp;
    }

    @Override
    //hashMap<--eCode=CZ3099&dest=北京
    public CostPrice selectPriceByOther(HashMap<String, String> hashMap) {
        // 打开session
        SqlSession session = ssf.openSession();
        CostPriceMapper mapper = session.getMapper(CostPriceMapper.class);
        CostPrice cp = new CostPrice();
        cp = mapper.selectPriceByOther(hashMap);
        session.close();
        return cp;
    }


}
