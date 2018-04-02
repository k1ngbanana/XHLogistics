package service;

import org.apache.poi.ss.usermodel.Row;
import utils.POIUtils;

import java.util.Properties;

public class BaseService {
    Properties billingProp;
    Properties pathProp;

    {
        billingProp = POIUtils.getProp("/billingExcel.properties");
        pathProp = POIUtils.getProp("/filePath.properties");
    }

    //简化获取函数
    public String getCellValue(Row row, String colName) {
        //Workbook wb = POIUtils.getWorkBook("formatExcel");
        //Sheet dataSheet = wb.getSheet("数据表");
        return POIUtils.getCellValue(row.getCell(POIUtils.getColumnNum(billingProp, colName)));
    }
}
