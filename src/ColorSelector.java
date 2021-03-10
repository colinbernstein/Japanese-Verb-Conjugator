import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A Color Selector GUI for customizing the appearance of the GUI.
 *
 * @author Colin Bernstein
 * @version 1.4
 */
class ColorSelector extends JPanel implements ActionListener {
    
    private JButton[] optionButtons;
    private JButton setDefaultsButton;
    private JPanel[] colorSquares;
    private static final String[] optionsEnglish = {"Background", "Labels", "Auto-Suggestor", "Text", "Label Borders"},
            optionsJapanese = {"背景", "ラベル", "自動提案者", "テキスト", "提案枠"};
    private Conjugator conjugator;
    private JColorChooser colorChooser;
    
    /**
     * Defines all relevant buttons, labels, and menus.
     * @param conjugator the Conjugator object to build off of
     */
    ColorSelector(Conjugator conjugator) {
        super();
        this.conjugator = conjugator;
        
        JPanel buttonPane = new JPanel(new GridLayout(2, 1));
        
        JPanel fiveButtonPane = new JPanel();
        fiveButtonPane.setPreferredSize(new Dimension(180, 180));
        optionButtons = new JButton[optionsEnglish.length];
        colorSquares = new JPanel[optionsEnglish.length];
        for (byte i = 0; i < optionsEnglish.length * 2; i++) {
            if (i % 2 == 0) {
                optionButtons[i / 2] = new JButton(conjugator.getEnglishOrJapanese() ? optionsEnglish[i / 2] : optionsJapanese[i / 2]);
                optionButtons[i / 2].setBounds(10, 10 + (25 * i), 100, 30);
                optionButtons[i / 2].setFont(new Font("TimesRoman", Font.BOLD, 14));
                optionButtons[i / 2].setActionCommand(optionsEnglish[i / 2]);
                optionButtons[i / 2].addActionListener(this);
                optionButtons[i / 2].setVisible(true);
                optionButtons[i / 2].setOpaque(true);
                optionButtons[i / 2].setFocusable(false);
                fiveButtonPane.add(optionButtons[i / 2]);
            } else {
                colorSquares[i / 2] = new JPanel();
                colorSquares[i / 2].setBounds(50, 10 + (25 * i), 20, 20);
                colorSquares[i / 2].setFocusable(false);
                colorSquares[i / 2].setBackground(ColorMap.getColorFromIndex((byte) (i / 2)).color());
                colorSquares[i / 2].setVisible(true);
                colorSquares[i / 2].setOpaque(true);
                fiveButtonPane.add(colorSquares[i / 2]);
            }
        }
        
        buttonPane.add(fiveButtonPane);
        
        JPanel defaultButtonPane = new JPanel();
        defaultButtonPane.setPreferredSize(new Dimension(180, 120));
        setDefaultsButton = new JButton(conjugator.getEnglishOrJapanese() ? "Set Defaults" : "初期設定");
        setDefaultsButton.setFont(new Font("TimesRoman", Font.BOLD, 14));
        setDefaultsButton.setActionCommand("defaults");
        setDefaultsButton.addActionListener(this);
        setDefaultsButton.setVisible(true);
        setDefaultsButton.setOpaque(true);
        setDefaultsButton.setFocusable(false);
        defaultButtonPane.add(setDefaultsButton, Component.CENTER_ALIGNMENT);
        buttonPane.add(defaultButtonPane, Component.CENTER_ALIGNMENT);
        
        setColorBoxBorders();
        add(buttonPane);
        colorChooser = new JColorChooser();
        colorChooser.setVisible(true);
        colorChooser.setOpaque(true);
        add(colorChooser);
        repaint();
    }
    
    /**
     * Actions for action listeners.
     * Redefines the color map according to the text on the pressed button.
     * Refreshes all GUI windows to reflect these changes.
     * @param e the event containing the text of the button pressed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("defaults")) {
            ColorMap.setDefaults();
            conjugator.setColors();
            setColorBoxBorders();
            for (byte i = 0; i < optionsEnglish.length; i++)
                colorSquares[i].setBackground(ColorMap.getColorFromIndex(i).color());
            repaint();
            return;
        }
        if (command.equals(optionsEnglish[2]))
            command = "AUTO_SUGGESTOR";
        else if (command.equals(optionsEnglish[4]))
            command = "LABEL_BORDERS";
        Color color = colorChooser.getColor();
        ColorMap.valueOf(command.toUpperCase()).setColor(color);
        conjugator.setColors();
        for (byte i = 0; i < optionsEnglish.length; i++)
            colorSquares[i].setBackground(ColorMap.getColorFromIndex(i).color());
        setColorBoxBorders();
        repaint();
    }
    
    /**
     * Set the labels to their text in either English or Japanese depending on a value stored in the Conjugator.
     */
    void resetLabels() {
        boolean englishOrJapanese = conjugator.getEnglishOrJapanese();
        for (byte i = 0; i < optionsEnglish.length; i++)
            optionButtons[i].setText(englishOrJapanese ? optionsEnglish[i] : optionsJapanese[i]);
        setDefaultsButton.setText(englishOrJapanese ? "Set Defaults" : "初期設定");
        repaint();
    }
    
    /**
     * Set the borders of the color boxes to a color that is visibly contrasting with it.
     */
    private void setColorBoxBorders() {
        for (byte i = 0; i < optionsEnglish.length; i++) {
            Color color = ColorMap.getColorFromIndex(i).color();
            Color inverseColor = new Color(Math.min(255 - color.getRed(), 175),
                    Math.min(255 - color.getGreen(), 175), Math.min(255 - color.getBlue(), 175));
            colorSquares[i].setBorder(new LineBorder(inverseColor));
        }
    }
}