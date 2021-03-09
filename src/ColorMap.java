import java.awt.Color;

enum ColorMap {
    AUTO_SUGGESTOR(Color.WHITE),
    BACKGROUND(new Color(235, 235, 235)),
    LABEL_BORDERS(Color.BLACK),
    LABELS(Color.LIGHT_GRAY),
    TEXT(Color.BLACK);
    
    private Color color;
    
    ColorMap(Color color) {
        this.color = color;
    }
    
    Color color() {
        return color;
    }
    
    void setColor(Color color) {
        this.color = color;
    }
    
    static ColorMap getColorFromIndex(byte index) {
        switch (index) {
            case 0: return BACKGROUND;
            case 1: return LABELS;
            case 2: return AUTO_SUGGESTOR;
            case 3: return TEXT;
            case 4: return LABEL_BORDERS;
        }
        throw new IndexOutOfBoundsException(index);
    }
    
    static void setDefaults() {
        AUTO_SUGGESTOR.setColor(Color.WHITE);
        BACKGROUND.setColor(new Color(235, 235, 235));
        LABEL_BORDERS.setColor(Color.BLACK);
        LABELS.setColor(Color.LIGHT_GRAY);
        TEXT.setColor(Color.BLACK);
    }
}