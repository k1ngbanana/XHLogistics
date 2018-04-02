package pojo.mapper;

import pojo.LanshiPrice;

import java.util.HashMap;
import java.util.List;

public interface LanshiPriceMapper {

    void truncatePrice();

    void insertPrices(List<LanshiPrice> list);

    LanshiPrice selectPriceByFlight(LanshiPrice lp);

    LanshiPrice selectPriceByOther(HashMap<String,String> hashMap);

    List<LanshiPrice> selectPrice(LanshiPrice lp);

    void updatePriceAndFlag(List<LanshiPrice> list);

}
