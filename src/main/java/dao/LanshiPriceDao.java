package dao;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import pojo.LanshiPrice;
import pojo.mapper.LanshiPriceMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class LanshiPriceDao implements LanshiPriceMapper {

    @Autowired
    SqlSessionFactory ssf;

    @Override
    public void truncatePrice() {
        // 打开session
        SqlSession session = ssf.openSession();
        LanshiPriceMapper mapper = session.getMapper(LanshiPriceMapper.class);
        mapper.truncatePrice();
        session.commit();
        session.close();
    }

    @Override
    public void insertPrices(List<LanshiPrice> list) {
        // 打开session
        SqlSession session = ssf.openSession();
        LanshiPriceMapper mapper = session.getMapper(LanshiPriceMapper.class);
        mapper.insertPrices(list);
        session.commit();
        session.close();
    }

    @Override
    public LanshiPrice selectPriceByFlight(LanshiPrice lp) {
        // 打开session
        SqlSession session = ssf.openSession();
        LanshiPriceMapper mapper = session.getMapper(LanshiPriceMapper.class);
        lp = mapper.selectPriceByFlight(lp);
        session.close();
        return lp;
    }

    @Override
    public LanshiPrice selectPriceByOther(HashMap<String, String> hashMap) {
        // 打开session
        SqlSession session = ssf.openSession();
        LanshiPriceMapper mapper = session.getMapper(LanshiPriceMapper.class);
        LanshiPrice lp = new LanshiPrice();
        lp = mapper.selectPriceByOther(hashMap);
        session.close();
        return lp;
    }

    @Override
    public List<LanshiPrice> selectPrice(LanshiPrice lp) {
        // 打开session
        SqlSession session = ssf.openSession();
        LanshiPriceMapper mapper = session.getMapper(LanshiPriceMapper.class);
        List<LanshiPrice> list = new ArrayList<>();
        list = mapper.selectPrice(lp);
        session.close();
        return list;
    }

    @Override
    public void updatePriceAndFlag(List<LanshiPrice> list) {
        // 打开session
        SqlSession session = ssf.openSession();
        LanshiPriceMapper mapper = session.getMapper(LanshiPriceMapper.class);
        mapper.updatePriceAndFlag(list);
        session.commit();
        session.close();
    }
}
