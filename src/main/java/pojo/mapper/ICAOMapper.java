package pojo.mapper;

import pojo.ICAO;

import java.util.List;

public interface ICAOMapper {

	void insertICAOs(List<ICAO> list);

	List<ICAO> selectAll();

}


