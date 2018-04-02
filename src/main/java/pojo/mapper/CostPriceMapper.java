package pojo.mapper;

import pojo.CostPrice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface CostPriceMapper {

    void insertPrices(List<CostPrice> costPriceList);

    void truncatePrice();

    ArrayList<CostPrice> selectAllPrice();

    CostPrice selectPriceByFlight(CostPrice cp);

    CostPrice selectPriceByOther(HashMap<String,String> hashMap);

}
