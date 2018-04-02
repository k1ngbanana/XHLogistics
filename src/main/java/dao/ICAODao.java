package dao;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import pojo.ICAO;
import pojo.mapper.ICAOMapper;

import java.util.List;

@Repository
public class ICAODao implements ICAOMapper{

    @Autowired
    SqlSessionFactory ssf;

    @Override
    public void insertICAOs(List<ICAO> list) {
        // 打开session
        SqlSession session = ssf.openSession();
        ICAOMapper mapper = session.getMapper(ICAOMapper.class);
        mapper.insertICAOs(list);
        session.commit();
        session.close();
    }

    @Override
    public List<ICAO> selectAll() {
        // 打开session
        SqlSession session = ssf.openSession();
        ICAOMapper mapper = session.getMapper(ICAOMapper.class);
        List<ICAO> list = mapper.selectAll();
        session.close();
        return list;
    }


}
