package utils;

public enum CellStyleEnum {
    /**
     * 没有边框，字体“等线”，居中
     */
    DEFAULT(0),
    /**
     * yyyy/MM/dd
     */
    DATE(1),
    /**
     * HH:mm
     */
    TIME(2),
    /**
     * 全边框，THIN
     */
    ALL_BORDER_THIN(3),
    /**
     * 红色粗体
     */
    RED_BOLD(4),
    /**
     * 蓝色粗体
     */
    BLUE_BOLD(5);

    private final int code;

    CellStyleEnum(int i) {
        this.code = i;
    }

    public int getCode() {
        return code;
    }
}
