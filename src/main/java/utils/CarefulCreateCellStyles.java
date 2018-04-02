package utils;

public class CarefulCreateCellStyles {

/*    public void setDefaultCellSytle(Sheet sheet, Map<CellStyleEnum, int[]> map,String outputPath) throws Exception {

        Workbook workbook = sheet.getWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();

        System.out.println(workbook.getNumCellStyles());

        Font defaultFont = null;

        for (short i = 0; i < workbook.getNumberOfFonts(); i++) {
            //System.out.println(workbook.getFontAt(i).getFontName());
            Font temp = workbook.getFontAt(i);
            if (temp.getFontName().equals("等线") && temp.getFontHeightInPoints() == 10) {
                defaultFont = temp;
            }
        }

        if (defaultFont == null) {
            System.out.println("创建默认字体");
            defaultFont = workbook.createFont();
            defaultFont.setFontName("等线");
            defaultFont.setFontHeightInPoints((short) 10);
        }

        HashMap<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put(CellUtil.ALIGNMENT, HorizontalAlignment.CENTER);
        defaultProperties.put(CellUtil.VERTICAL_ALIGNMENT, VerticalAlignment.CENTER);
        defaultProperties.put(CellUtil.FONT, defaultFont.getIndex());

        HashMap<String, Object> tempProperties = new HashMap<>();

        for (Map.Entry<CellStyleEnum, int[]> entry : map.entrySet()) {
            switch (entry.getKey()) {
                case ALL_BORDER_THIN:
                    tempProperties.put(CellUtil.BORDER_LEFT, BorderStyle.THIN);
                    tempProperties.put(CellUtil.BORDER_TOP, BorderStyle.THIN);
                    tempProperties.put(CellUtil.BORDER_RIGHT, BorderStyle.THIN);
                    tempProperties.put(CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
                    break;
                case DATE:
                    tempProperties.put(CellUtil.DATA_FORMAT, createHelper.createDataFormat().getFormat("yyyy/MM/dd"));
                    break;
                case TIME:
                    tempProperties.put(CellUtil.DATA_FORMAT, createHelper.createDataFormat().getFormat("HH:mm"));
                    break;
            }
            tempProperties.putAll(defaultProperties);
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                for (int j : entry.getValue()) {
                    if(sheet.getRow(i).getCell(j)==null){
                        sheet.getRow(i).createCell(j);
                    }
                    CellUtil.setCellStyleProperties(sheet.getRow(i).getCell(j), tempProperties);
                }
            }
            //用完之后清除样式
            tempProperties.clear();
        }

        workbook.write(new FileOutputStream(POIUtils.getFilePath(outputPath)));
        workbook.close();
    }*/

/*    public static void main(String[] args) throws Exception {
        CarefulCreateCellStyles carefulCreateCellStyles = new CarefulCreateCellStyles();
        Workbook workbook = POIUtils.getWorkBook("exteranlPrice");
        Sheet sheet = workbook.getSheetAt(0);
        HashMap<CellStyleEnum, int[]> styleMap = new HashMap<>();
        styleMap.put(CellStyleEnum.TIME, new int[]{2,3});
        styleMap.put(CellStyleEnum.ALL_BORDER_THIN, new int[]{0,1,2,3,4,5});
        carefulCreateCellStyles.setDefaultCellSytle(sheet, styleMap);

    }*/
}