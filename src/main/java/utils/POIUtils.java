package utils;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

public class POIUtils {

    /**
     * @param sheet      需要设置格式的表格
     * @param map        需要设置的样式和需要设置的列
     * @param outputPath 需要输出文件的路径
     * @throws Exception
     */
    public static void setCellSytleByMap(Sheet sheet, Map<CellStyleEnum, int[]> map, String outputPath) throws Exception {

        Workbook workbook = sheet.getWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();

        //设置默认字体为"等线"，大小为10像素
        Font defaultFont = null;

        for (short i = 0; i < workbook.getNumberOfFonts(); i++) {
            //System.out.println(workbook.getFontAt(i).getFontName());
            Font temp = workbook.getFontAt(i);
            if (temp.getFontName().equals("等线") && temp.getFontHeightInPoints() == 10) {
                defaultFont = temp;
            }
        }

        if (defaultFont == null) {
            defaultFont = workbook.createFont();
            defaultFont.setFontName("等线");
            defaultFont.setFontHeightInPoints((short) 10);
        }

        //设置为水平，垂直居中对齐
        HashMap<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put(CellUtil.ALIGNMENT, HorizontalAlignment.CENTER);
        defaultProperties.put(CellUtil.VERTICAL_ALIGNMENT, VerticalAlignment.CENTER);
        defaultProperties.put(CellUtil.FONT, defaultFont.getIndex());

        //设置临时属性Map
        HashMap<String, Object> tempProperties = new HashMap<>();

        for (Map.Entry<CellStyleEnum, int[]> entry : map.entrySet()) {
            tempProperties.putAll(defaultProperties);
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
            //设置对应的列为tempProperties的样式
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                for (int j : entry.getValue()) {
                    if (sheet.getRow(i).getCell(j) == null) {
                        sheet.getRow(i).createCell(j);
                    }
                    CellUtil.setCellStyleProperties(sheet.getRow(i).getCell(j), tempProperties);
                }
            }
            //用完之后清除样式
            tempProperties.clear();
        }

        if (outputPath != null) {
            workbook.write(new FileOutputStream(POIUtils.getFilePath(outputPath)));
            workbook.close();
        }
    }

    public static void setCellSytleByMap(Sheet sheet, Map<CellStyleEnum, int[]> map) throws Exception {
        setCellSytleByMap(sheet, map, null);
    }

    /**
     * @param sheet 需要设置格式的表格
     * @param map   Integer为需要设置的列，int[]为需要设置的行
     * @throws Exception
     */
    public static void setCellSytleByMap(Map<CellStyleEnum, Map<Integer, Integer[]>> map, Sheet sheet) throws Exception {
        Workbook workbook = sheet.getWorkbook();

        //设置默认字体为"等线"，大小为10像素
        Font defaultFont = null;
        Font redBoldFont = null;
        Font blueBoldFont = null;

        for (short i = 0; i < workbook.getNumberOfFonts(); i++) {
            //System.out.println(workbook.getFontAt(i).getColor());
            Font temp = workbook.getFontAt(i);
            if (temp.getFontName().equals("等线") && temp.getFontHeightInPoints() == 10) {
                if (temp.getColor() == IndexedColors.RED.getIndex()) {
                    redBoldFont = temp;
                } else if(temp.getColor() == IndexedColors.BLUE.getIndex()) {
                    blueBoldFont = temp;
                } else{
                    defaultFont = temp;
                }
            }

        }

        if (defaultFont == null) {
            defaultFont = workbook.createFont();
            defaultFont.setFontName("等线");
            defaultFont.setFontHeightInPoints((short) 10);
        }
        if (redBoldFont == null) {
            redBoldFont = workbook.createFont();
            redBoldFont.setFontName(defaultFont.getFontName());
            redBoldFont.setFontHeightInPoints(defaultFont.getFontHeightInPoints());
            redBoldFont.setBold(true);
            redBoldFont.setColor(IndexedColors.RED.getIndex());
        }
        if (blueBoldFont == null) {
            blueBoldFont = workbook.createFont();
            blueBoldFont.setFontName(defaultFont.getFontName());
            blueBoldFont.setFontHeightInPoints(defaultFont.getFontHeightInPoints());
            blueBoldFont.setBold(true);
            blueBoldFont.setColor(IndexedColors.BLUE.getIndex());
        }

        //设置为水平，垂直居中对齐
        HashMap<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put(CellUtil.ALIGNMENT, HorizontalAlignment.CENTER);
        defaultProperties.put(CellUtil.VERTICAL_ALIGNMENT, VerticalAlignment.CENTER);
        defaultProperties.put(CellUtil.FONT, defaultFont.getIndex());

        //设置临时属性Map
        HashMap<String, Object> tempProperties = new HashMap<>();

        for (Map.Entry<CellStyleEnum, Map<Integer, Integer[]>> entry : map.entrySet()) {
            tempProperties.putAll(defaultProperties);
            switch (entry.getKey()) {
                case RED_BOLD:
                    tempProperties.put(CellUtil.FONT, redBoldFont.getIndex());
                    break;
                case BLUE_BOLD:
                    tempProperties.put(CellUtil.FONT, blueBoldFont.getIndex());
                    break;
            }

            //设置对应的列为tempProperties的样式
            for (Map.Entry<Integer, Integer[]> entry1 : entry.getValue().entrySet()) {
                for (int row : entry1.getValue()) {
                    CellUtil.setCellStyleProperties(sheet.getRow(row).getCell(entry1.getKey()), tempProperties);
                }
            }
            //用完之后清除样式
            tempProperties.clear();
        }

    }


    public static int getColumnNum(Properties prop, String key) {
        return Integer.parseInt(prop.getProperty(key));
    }

    public static String getFilePath(String filekeyName) {
        return getProp("/filePath.properties").getProperty(filekeyName);
    }

    public static Workbook getWorkBook(String excelName) {
        Properties pathProp = getProp("/filePath.properties");
        String path = pathProp.getProperty(excelName);
        WorkbookFactory wbf = new WorkbookFactory();
        try {
            return wbf.create(new FileInputStream(new File(path)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Properties getProp(String propPath) {
        Properties prop = new Properties();
        InputStream is = POIUtils.class.getResourceAsStream(propPath);
        try {
            prop.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    /**
     * Removes rows where the column B is empty.
     *
     * @param sheet the sheet where rows should be removed
     */
    private void removeEmptyRows(final Sheet sheet) {
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            boolean isRowEmpty;
            if (sheet.getRow(i) == null) {
                sheet.shiftRows(i + 1, sheet.getLastRowNum(), -1);
                i--;
                continue;
            }
            final Row actualRow = sheet.getRow(i);
            isRowEmpty =
                    actualRow.getCell(1).toString().trim().equals("");
            if (isRowEmpty) {
                if (i == sheet.getLastRowNum()) {
                    sheet.removeRow(actualRow);
                } else {
                    sheet.shiftRows(i + 1, sheet.getLastRowNum(), -1);
                }
                i--;
            }
        }
    }

    public static void removeRow(Sheet sheet, int row) {
        if (row == sheet.getLastRowNum()) {
            sheet.removeRow(sheet.getRow(row));
        } else {
            System.out.println(sheet.getLastRowNum());
            sheet.shiftRows(row + 1, sheet.getLastRowNum(), -1);
        }
    }

    /**
     * 根据单元格类型获取对应的String类型的值
     *
     * @param cell 要获取值的单元格
     * @return
     */
    public static String getCellValue(Cell cell) {

        String cellValue = null;
        if (cell != null) {
            switch (cell.getCellTypeEnum()) {
                case STRING:
                    cellValue = cell.getStringCellValue();
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        cellValue = cell.getDateCellValue().toString();
                    } else {
                        cellValue = Double.toString(cell.getNumericCellValue());
                    }
                    break;
                case BLANK:
                    cellValue = "";
                    break;
                case BOOLEAN:
                    cellValue = Boolean.toString(cell.getBooleanCellValue());
                    break;
                case FORMULA:
                    FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                    if(evaluator.evaluate(cell).getCellTypeEnum().equals(CellType.NUMERIC)){
                        BigDecimal bd = new BigDecimal(evaluator.evaluate(cell).getNumberValue());
                        cellValue = bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                        break;
                    }
                    cellValue = evaluator.evaluate(cell).formatAsString();
                    break;
                case ERROR:
                    cellValue = Byte.toString(cell.getErrorCellValue());
            }
        }
        return cellValue;
    }


    public Workbook mergeExcelFiles(Workbook book, List<InputStream> inList)
            throws IOException, EncryptedDocumentException, InvalidFormatException {

        for (InputStream fin : inList) {
            Workbook b = WorkbookFactory.create(fin);
            for (int i = 0; i < b.getNumberOfSheets(); i++) {
                // not entering sheet name, because of duplicated names
                copySheets(book.createSheet(), b.getSheetAt(i));
            }
        }
        return book;
    }

    /**
     * @param newSheet the sheet to create from the copy.
     * @param sheet    the sheet to copy.
     */
    public static void copySheets(Sheet newSheet, Sheet sheet) {
        copySheets(newSheet, sheet, true);
    }

    /**
     * @param newSheet  the sheet to create from the copy.
     * @param sheet     the sheet to copy.
     * @param copyStyle true copy the style.
     */
    public static void copySheets(Sheet newSheet, Sheet sheet, boolean copyStyle) {
        int maxColumnNum = 0;
        Map<Integer, CellStyle> styleMap = (copyStyle) ? new HashMap<Integer, CellStyle>() : null;
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            Row srcRow = sheet.getRow(i);
            Row destRow = newSheet.createRow(i);
            if (srcRow != null) {
                copyRow(sheet, newSheet, srcRow, destRow, styleMap);
                if (srcRow.getLastCellNum() > maxColumnNum) {
                    maxColumnNum = srcRow.getLastCellNum();
                }
            }
        }
        for (int i = 0; i <= maxColumnNum; i++) {
            newSheet.setColumnWidth(i, sheet.getColumnWidth(i));
        }
    }

    /**
     * @param srcSheet  the sheet to copy.
     * @param destSheet the sheet to create.
     * @param srcRow    the row to copy.
     * @param destRow   the row to create.
     * @param styleMap  -
     */
    public static void copyRow(Sheet srcSheet, Sheet destSheet, Row srcRow, Row destRow,
                               Map<Integer, CellStyle> styleMap) {
        // manage a list of merged zone in order to not insert two times a merged zone
        Set<CellRangeAddressWrapper> mergedRegions = new TreeSet<CellRangeAddressWrapper>();
        destRow.setHeight(srcRow.getHeight());
        // reckoning delta rows
        int deltaRows = destRow.getRowNum() - srcRow.getRowNum();
        // pour chaque row
        for (int j = srcRow.getFirstCellNum(); j <= srcRow.getLastCellNum(); j++) {
            if (j == -1) {
                continue;
            }
            Cell oldCell = srcRow.getCell(j); // ancienne cell
            Cell newCell = destRow.getCell(j); // new cell
            if (oldCell != null) {
                if (newCell == null) {
                    newCell = destRow.createCell(j);
                }
                // copy chaque cell
                copyCell(oldCell, newCell, styleMap);
                // copy les informations de fusion entre les cellules
                // System.out.println("row num: " + srcRow.getRowNum() + " , col: " +
                // (short)oldCell.getColumnIndex());
                CellRangeAddress mergedRegion = getMergedRegion(srcSheet, srcRow.getRowNum(),
                        (short) oldCell.getColumnIndex());

                CellRangeAddress lastMergedRegion = null;
                if (srcRow.getRowNum() > 0) {
                    lastMergedRegion = getMergedRegion(srcSheet, srcRow.getRowNum() - 1, (short) oldCell.getColumnIndex());
                }

                if (mergedRegion != null) {

                    //如果两行是同一个合并单元格，那么继续
                    if (mergedRegion.equals(lastMergedRegion)) {
                        continue;
                    }

                    //System.out.println("Selected merged region: " + mergedRegion.toString());
                    CellRangeAddress newMergedRegion = new CellRangeAddress(mergedRegion.getFirstRow() + deltaRows,
                            mergedRegion.getLastRow() + deltaRows, mergedRegion.getFirstColumn(),
                            mergedRegion.getLastColumn());

                    //System.out.println("New merged region: " + newMergedRegion.toString());
                    CellRangeAddressWrapper wrapper = new CellRangeAddressWrapper(newMergedRegion);
                    if (isNewMergedRegion(wrapper, mergedRegions)) {
                        mergedRegions.add(wrapper);
                        destSheet.addMergedRegion(wrapper.range);
                    }
                }
            }
        }
    }

    /**
     * @param oldCell
     * @param newCell
     * @param styleMap
     */
    public static void copyCell(Cell oldCell, Cell newCell, Map<Integer, CellStyle> styleMap) {
        if (styleMap != null) {
            if (oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()) {
                newCell.setCellStyle(oldCell.getCellStyle());
            } else {
                int stHashCode = oldCell.getCellStyle().hashCode();
                CellStyle newCellStyle = styleMap.get(stHashCode);
                if (newCellStyle == null) {
                    newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();
                    newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
                    styleMap.put(stHashCode, newCellStyle);
                }
                newCell.setCellStyle(newCellStyle);
            }
        }
        switch (oldCell.getCellTypeEnum()) {
            //case Cell.CELL_TYPE_STRING:
            case STRING:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case BLANK:
                newCell.setCellType(CellType.BLANK);
                break;
            case BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            default:
                break;
        }

    }

    /**
     * Récupère les informations de fusion des cellules dans la sheet source pour
     * les appliquer à la sheet destination... Récupère toutes les zones merged dans
     * la sheet source et regarde pour chacune d'elle si elle se trouve dans la
     * current row que nous traitons. Si oui, retourne l'objet CellRangeAddress.
     *
     * @param sheet   the sheet containing the data.
     * @param rowNum  the num of the row to copy.
     * @param cellNum the num of the cell to copy.
     * @return the CellRangeAddress created.
     */
    public static CellRangeAddress getMergedRegion(Sheet sheet, int rowNum, short cellNum) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress merged = sheet.getMergedRegion(i);
            if (merged.isInRange(rowNum, cellNum)) {
                return merged;
            }
        }
        return null;
    }

    /**
     * Check that the merged region has been created in the destination sheet.
     *
     * @param newMergedRegion the merged region to copy or not in the destination sheet.
     * @param mergedRegions   the list containing all the merged region.
     * @return true if the merged region is already in the list or not.
     */
    private static boolean isNewMergedRegion(CellRangeAddressWrapper newMergedRegion,
                                             Set<CellRangeAddressWrapper> mergedRegions) {
        return !mergedRegions.contains(newMergedRegion);
    }

    /**
     * 判断该表格里面的row行col列的单元格是否为空，是则创建新单元格
     *
     * @param sheet
     * @param row
     * @param col
     * @param cellType
     */
    public static Cell isNullCreate(Sheet sheet, int row, int col, CellType cellType) {
        if (sheet.getRow(row).getCell(col) == null) {
            if (cellType == null) {
                return sheet.getRow(row).createCell(col);
            } else {
                return sheet.getRow(row).createCell(col, cellType);
            }
        }
        //没用的返回
        return sheet.getRow(row).getCell(col);
    }

    public static void isNullCreate(Sheet sheet, int row, int col) {
        isNullCreate(sheet, row, col, null);
    }

    /**
     * 设置单元格Comment
     * @param cell 需要设置的单元格
     * @param myComment 需要设置的内容
     */
    public static void setCellComment(Cell cell,String myComment){
        Sheet sheet = cell.getSheet();
        Workbook wb = sheet.getWorkbook();
        CreationHelper factory = wb.getCreationHelper();

        Drawing drawing = sheet.createDrawingPatriarch();

        // When the comment box is visible, have it show in a 1x3 space
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex()+2);
        anchor.setRow1(cell.getRow().getRowNum());
        anchor.setRow2(cell.getRow().getRowNum()+4);

        // Create the comment and set the text+author
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString(myComment);
        comment.setString(str);
        comment.setAuthor("banana");

        // Assign the comment to the cell
        cell.setCellComment(comment);
    }

}


