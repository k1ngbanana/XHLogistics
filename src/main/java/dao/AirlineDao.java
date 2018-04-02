package dao;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import pojo.Airline;
import pojo.mapper.AirlineMapper;

import java.util.List;

@Repository
public class AirlineDao implements AirlineMapper {

    @Autowired
    SqlSessionFactory ssf;

    @Override
    public Airline selectAirline(Airline airline) {
        // 打开session
        SqlSession session = ssf.openSession();
        AirlineMapper mapper = session.getMapper(AirlineMapper.class);
        airline = mapper.selectAirline(airline);
        session.close();
        return airline;
    }

    @Override
    public void updateFlightTimeAndVia(List<Airline> airlines) {
        // 打开session
        SqlSession session = ssf.openSession();
        AirlineMapper mapper = session.getMapper(AirlineMapper.class);
        mapper.updateFlightTimeAndVia(airlines);
        session.commit();
        session.close();
    }

    @Override
    public void updateFlightTimeAndWeekdays(List<Airline> airlines) {
        // 打开session
        SqlSession session = ssf.openSession();
        AirlineMapper mapper = session.getMapper(AirlineMapper.class);
        mapper.updateFlightTimeAndWeekdays(airlines);
        session.commit();
        session.close();
    }

    @Override
    public void insertAirlines(List<Airline> airlines) {
        // 打开session
        SqlSession session = ssf.openSession();
        AirlineMapper mapper = session.getMapper(AirlineMapper.class);
        mapper.insertAirlines(airlines);
        session.commit();
        session.close();
    }
}
