
import com.voicerss.tts.AudioFormat;
import com.voicerss.tts.Languages;
import com.voicerss.tts.VoiceParameters;
import com.voicerss.tts.VoiceProvider;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.sound.sampled.DataLine;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A Japanese Verb Conjugator using a Swing GUI
 *
 * @author Colin Bernstein
 * @version 1.5
 */
public class Conjugator extends JPanel implements ActionListener {
    
    
    //Define encoded constants for conjugation types and verb types
    
    private static final byte LONG = 0;
    private static final byte FORMAL_NEGATIVE = 1;
    private static final byte FORMAL_PAST = 2;
    private static final byte FORMAL_NEGATIVE_PAST = 3;
    private static final byte SHORT = 4;
    private static final byte INFORMAL_NEGATIVE = 5;
    private static final byte INFORMAL_PAST = 6;
    private static final byte INFORMAL_NEGATIVE_PAST = 7;
    private static final byte TE = 8;
    private static final byte IMPERATIVE = 9;
    private static final byte INFORMAL_VOLITIONAL = 10;
    private static final byte FORMAL_VOLITIONAL = 11;
    private static final byte HYPOTHETICAL = 12;
    private static final byte CONDITIONAL = 13;
    private static final byte POTENTIAL = 14;
    private static final byte PASSIVE = 15;
    private static final byte CAUSATIVE = 16;
    private static final byte CAUSATIVE_PASSIVE = 17;
    
    private static final byte GODAN = 1;
    private static final byte ICHIDAN = 2;
    private static final byte IRREGULAR = 3;
    private static final byte SURU = 4;
    private static final byte TRANSITIVE = 5;
    private static final byte INTRANSITIVE = 6;
    private static final byte NO_TRANSITIVITY = 7;
    
    private JFrame frame, colorFrame;
    private AutoSuggestor autoSuggestor;
    private JTextField field;
    private JLabel[] conjugatedLabels, furiganaLabels, conjugationTypeLabels;
    private JButton[] speakButtons = new JButton[conjugationTypes.length];
    private JLabel furiganaLabel, translationLabel, verbTypeLabel, transitivityLabel;
    private JButton englishOrJapaneseButton, enterColorSelectorButton;
    private JToggleButton furiganaButton;
    private Map<String, VerbInfoPacket> lexicon;
    private VerbInfoPacket currentPacket;
    private Point colorFrameRelativePos = new Point(60, 300);
    private boolean englishOrJapanese = true;
    
    
    //Define labels and irregular constants
    
    private static final String[] conjugationTypes = {"Long", "Formal Negative", "Formal Past", "Formal Negative Past",
            "Short", "Informal Negative", "Informal Past", "Informal Negative Past",
            "Te", "Imperative", "Informal Volitional", "Formal Volitional", "Hypothetical", "Conditional",
            "Potential", "Passive", "Causative", "Causative Passive"};
    
    private static final String[] conjugationTypesJapanese = {"?????????", "?????????????????????)", "?????????????????????)", "??????????????????????????????)", "?????????",
            "?????????", "??????", "??????????????????????????????", "??????", "?????????", "?????????", "???????????????", "?????????", "?????????", "?????????", "?????????", "?????????", "???????????????"};
    
    private static final String[][] irregulars = {{"????????????", "???????????????", "???????????????", "????????????????????????",
            "??????", "?????????", "??????", "???????????????", "??????", "??????", "?????????", "???????????????", "?????????", "?????????", "????????????", "????????????", "????????????", "??????????????????"},
            {"????????????", "???????????????", "???????????????", "????????????????????????", "??????", "????????????", "?????????", "??????????????????",
                    "?????????", "??????", "?????????", "??????????????????", "?????????", "????????????", "?????????", "????????????", "????????????", "???????????????"},
            {"???????????????", "??????????????????", "??????????????????", "???????????????????????????", "?????????", "???????????????", "????????????", "?????????????????????",
                    "????????????", "?????????", "????????????", "?????????????????????", "????????????", "???????????????", "????????????", "???????????????", "???????????????", "?????????????????????"},
            {"???????????????", "??????????????????", "??????????????????", "???????????????????????????", "?????????", "???????????????", "????????????", "?????????????????????",
                    "????????????", "?????????", "????????????", "?????????????????????", "????????????", "???????????????", "????????????", "???????????????", "???????????????", "?????????????????????"},
            {"????????????", "???????????????", "???????????????", "????????????????????????", "??????", "??????", "?????????", "????????????",
                    "?????????", "??????", "?????????", "??????????????????", "?????????", "????????????", "?????????", "No Passive Form", "????????????", "??????????????????"},
            {"???????????????", "??????????????????", "??????????????????", "???????????????????????????", "?????????", "???????????????", "????????????", "?????????????????????",
                    "????????????", "?????????", "????????????", "?????????????????????", "????????????", "???????????????", "????????????", "No Passive Form", "???????????????", "?????????????????????"},
            {"????????????????????????", "???????????????????????????", "???????????????????????????", "????????????????????????????????????", "??????????????????", "????????????????????????", "?????????????????????",
                    "??????????????????????????????", "?????????????????????", "??????????????????", "?????????????????????", "??????????????????????????????", "?????????????????????", "????????????????????????",
                    "?????????????????????", "No Passive Form", "????????????????????????", "??????????????????????????????"},
            {"?????????????????????", "????????????????????????", "????????????????????????", "?????????????????????????????????", "???????????????", "?????????????????????", "??????????????????", "???????????????????????????",
                    "??????????????????", "???????????????", "??????????????????", "???????????????????????????", "??????????????????", "?????????????????????", "??????????????????", "?????????????????????",
                    "?????????????????????", "???????????????????????????"}};
    private static final String[] kuruFuriganas = {"???", "???", "???", "???", "???", "???", "???", "???", "???", "???", "???", "???", "???", "???", "???", "???", "???", "???"};
    
    
    /**
     * Main method. Calls createAndShowGUI on the Swing Event Dispatch Thread.
     * This allows for a streamlined and thread-safe environment for the GUI elements.
     * Initialize all elements in the JPanel including the search window, buttons, and labels.
     * Display and paint all elements.
     *
     * @param args No arguments are needed in launching
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(Conjugator::createAndShowGUI);
    }
    
    /**
     * Creates a new Conjugator object.
     * Displaying the elements of the GUI happens inside of the constructor of Conjugator.
     */
    private static void createAndShowGUI() {
        new Conjugator();
    }
    
    /**
     * Creates a conjugator object comprised of a JPanel inside of a JFrame.
     * Defines the action listeners for the movement of the main window to prompt updates and to move other windows in sync.
     */
    private Conjugator() {
        super(new BorderLayout());
        frame = new JFrame("Japanese Verb Conjugator");
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);
        frame.setBackground(ColorMap.BACKGROUND.color());
        JPanel panel = new JPanel(new BorderLayout());
        
        ComponentListener moveListener = new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
            }
            
            @Override
            public void componentMoved(ComponentEvent e) {
                autoSuggestor.showPopUpWindow();
                if (colorFrame != null)
                    colorFrame.setLocation((int) (frame.getX() + colorFrameRelativePos.getX()), (int) (frame.getY() + colorFrameRelativePos.getY()));
            }
            
            @Override
            public void componentShown(ComponentEvent e) {
                //  autoSuggestor.getAutoSuggestionPopUpWindow().toFront();
            }
            
            @Override
            public void componentHidden(ComponentEvent e) {
            }
        };
        
        MouseListener mouseListener = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                autoSuggestor.showPopUpWindow();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
            }
        };
        
        frame.addComponentListener(moveListener);
        frame.addMouseListener(mouseListener);
        panel.addMouseListener(mouseListener);
        frame.getGlassPane().addMouseListener(mouseListener);
        add(panel, BorderLayout.LINE_START);
        setBorder(BorderFactory.createEmptyBorder(100, 200, 840, 800));
        
        field = new JTextField();
        field.setFont(new Font("TimesRoman", Font.BOLD, 30));
        field.setBounds(15, 35, 200, 50);
        add(field);
        initializeLexicon();
        autoSuggestor = new AutoSuggestor(this, frame, field);
        add(autoSuggestor);
        
        furiganaButton = new JToggleButton("Furigana", true);
        furiganaButton.setActionCommand("furigana");
        furiganaButton.addActionListener(this);
        furiganaButton.setBounds(235, 10, 80, 30);
        furiganaButton.setFont(new Font("TimesRoman", Font.BOLD, 14));
        furiganaButton.setFocusable(false);
        add(furiganaButton);
        
        englishOrJapaneseButton = new JButton("?????????");
        englishOrJapaneseButton.setActionCommand("englishOrJapanese");
        englishOrJapaneseButton.addActionListener(this);
        englishOrJapaneseButton.setBounds(310, 10, 80, 30);
        englishOrJapaneseButton.setFont(new Font("TimesRoman", Font.BOLD, 14));
        englishOrJapaneseButton.setFocusable(false);
        englishOrJapaneseButton.setOpaque(false);
        englishOrJapaneseButton.setContentAreaFilled(false);
        add(englishOrJapaneseButton);
        
        enterColorSelectorButton = new JButton("Edit Colors");
        enterColorSelectorButton.setActionCommand("color");
        enterColorSelectorButton.addActionListener(this);
        enterColorSelectorButton.setBounds(585, 10, 100, 30);
        enterColorSelectorButton.setFont(new Font("TimesRoman", Font.BOLD, 14));
        enterColorSelectorButton.setFocusable(false);
        add(enterColorSelectorButton);
        
        furiganaLabel = new JLabel("Furigana", JLabel.CENTER);
        translationLabel = new JLabel("English Translation", JLabel.CENTER);
        verbTypeLabel = new JLabel("Verb Type", JLabel.CENTER);
        transitivityLabel = new JLabel("Transitivity", JLabel.CENTER);
        
        furiganaLabel.setFont(new Font("TimesRoman", Font.BOLD, 15));
        translationLabel.setFont(new Font("TimesRoman", Font.BOLD, 15));
        verbTypeLabel.setFont(new Font("TimesRoman", Font.BOLD, 18));
        transitivityLabel.setFont(new Font("TimesRoman", Font.BOLD, 18));
        
        furiganaLabel.setBounds(25, 15, 180, 20);
        translationLabel.setBounds(235, 45, 450, 30);
        verbTypeLabel.setBounds(705, 45, 100, 30);
        transitivityLabel.setBounds(825, 45, 150, 30);
        
        furiganaLabel.setBackground(ColorMap.LABELS.color());
        translationLabel.setBackground(ColorMap.LABELS.color());
        verbTypeLabel.setBackground(ColorMap.LABELS.color());
        transitivityLabel.setBackground(ColorMap.LABELS.color());
        
        furiganaLabel.setOpaque(true);
        translationLabel.setOpaque(true);
        verbTypeLabel.setOpaque(true);
        transitivityLabel.setOpaque(true);
        
        add(furiganaLabel);
        add(translationLabel);
        add(verbTypeLabel);
        add(transitivityLabel);
        
        conjugatedLabels = new JLabel[conjugationTypes.length];
        furiganaLabels = new JLabel[conjugationTypes.length];
        conjugationTypeLabels = new JLabel[conjugationTypes.length];
        
        for (byte i = 0; i < conjugatedLabels.length; i++) {
            conjugatedLabels[i] = new JLabel("", JLabel.LEFT);
            conjugatedLabels[i].setFont(new Font("TimesRoman", Font.BOLD, 25));
            conjugatedLabels[i].setBounds(625, (46 * i) + 115, 500, 30);
            conjugationTypeLabels[i] = new JLabel(conjugationTypes[i]);
            conjugationTypeLabels[i].setFont(new Font("TimesRoman", Font.BOLD, 25));
            conjugationTypeLabels[i].setBounds(325, (46 * i) + 115, 500, 30);
            speakButtons[i] = new JButton("Speak");
            speakButtons[i].setActionCommand(String.valueOf(i));
            speakButtons[i].addActionListener(this);
            speakButtons[i].setBounds(225, (46 * i) + 115, 85, 30);
            speakButtons[i].setFont(new Font("TimesRoman", Font.BOLD, 15));
            speakButtons[i].setFocusable(false);
            speakButtons[i].setEnabled(false);
            furiganaLabels[i] = new JLabel("", JLabel.CENTER);
            furiganaLabels[i].setBounds(587, (46 * i) + 96, 100, 25);
            furiganaLabels[i].setFont(new Font("TimesRoman", Font.BOLD, 15));
            furiganaLabels[i].setVisible(true);
            add(furiganaLabels[i]);
            add(speakButtons[i]);
            add(conjugationTypeLabels[i]);
            add(conjugatedLabels[i]);
            add(new JLabel(""));
        }
        frame.add(panel);
        JComponent newContentPane = this;
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.getContentPane().setBackground(makeTransparent(ColorMap.BACKGROUND.color()));
        frame.getContentPane().setForeground(makeTransparent(ColorMap.BACKGROUND.color()));
        repaint();
    }
    
    /**
     * Determine whether the given word (in english, romaji, furigana, or kanji) is in the lexicon.
     * If so, get the corresponding data packet for that entry from the lexicon hash table.
     * Dissect the info packet and accordingly display conjugated forms based on the verb class (ichidan, godan, suru, and irregular).
     *
     * @param word A verb (in english, romaji, furigana, or kanji)
     */
    void conjugate(String word) {
        if (meaningParser(word).length == 0) {
            clear();
            translationLabel.setText("No Input");
            currentPacket = null;
            setLabels();
            return;
        }
        char c = word.charAt(0);
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
            word = word.toLowerCase();
            String[] parsed = meaningParser(word);
            if (parsed.length != 1) {
                errorMessage(word, true);
                currentPacket = null;
                return;
            }
            word = parsed[0];
        }
        if (!lexicon.containsKey(word)) {
            errorMessage(word, true);
            currentPacket = null;
            return;
        }
        
        //Form the Japanese stem of the verb and find the ending character(s)
        //Get the verb's corresponding info packet from the lexicon HashSet
        currentPacket = lexicon.get(word);
        String stem = word.substring(0, word.length() - (currentPacket.getType() != SURU ? 1 : 2));
        String ending = word.substring(word.length() - (currentPacket.getType() != SURU ? 1 : 2));
        String furigana = currentPacket.getFurigana();
        
        //Set the furigana labels for the one exception to the regularity of a verb's kanji reading (kuru - to come).
        if (word.equals("??????")) {
            for (byte i = 0; i < conjugationTypes.length; i++) {
                furiganaLabels[i].setText(kuruFuriganas[i]);
                furiganaLabels[i].setBounds(587, (46 * i) + 96, 100, 25);
            }
        } else
            //If it is a regular verb, derive and display the proper furigana for the kanji in the verb.
            furiganaOfKanji(word, furigana);
        
        //Set the furigana, transitivity, translation, and verb type labels according to the info packet for the given verb.
        furiganaLabel.setText(englishOrJapanese ? currentPacket.getRomaji() : furigana);
        transitivityLabel.setText(encodedStrings(currentPacket.getTransitivity()));
        translationLabel.setText(currentPacket.getTranslation());
        verbTypeLabel.setText(encodedStrings(currentPacket.getType()));
        
        //Enable all speech buttons
        for (JButton button : speakButtons)
            button.setEnabled(true);
        
        //The actual logical breakdown for the conjugation of all Japanese verbs except for kuru.
        switch (lexicon.get(word).getType()) {
            case GODAN:
                for (byte n = 0; n < conjugationTypes.length; n++) {
                    switch (n) {
                        case LONG:
                        case FORMAL_NEGATIVE:
                        case FORMAL_PAST:
                        case FORMAL_NEGATIVE_PAST:
                        case FORMAL_VOLITIONAL:
                            switch (ending) {
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                            }
                            String curr = conjugatedLabels[n].getText();
                            if (n == FORMAL_NEGATIVE)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 1) + "??????");
                            else if (n == FORMAL_PAST)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 1) + "??????");
                            else if (n == FORMAL_NEGATIVE_PAST)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 1) + "???????????????");
                            else if (n == FORMAL_VOLITIONAL)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 2) + "????????????");
                            break;
                        case SHORT:
                            conjugatedLabels[n].setText(word); break;
                        case INFORMAL_NEGATIVE:
                        case INFORMAL_NEGATIVE_PAST:
                        case PASSIVE:
                        case CAUSATIVE:
                        case CAUSATIVE_PASSIVE:
                            switch (ending) {
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "?????????");
                                    break;
                            }
                            curr = conjugatedLabels[n].getText();
                            if (n == INFORMAL_NEGATIVE_PAST)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 1) + "?????????");
                            else if (n == PASSIVE)
                                conjugatedLabels[n].setText(currentPacket.getTransitivity() == INTRANSITIVE ? "No Passive Form" :
                                        curr.substring(0, curr.length() - 2) + "??????");
                            else if (n == CAUSATIVE)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 2) + "??????");
                            else if (n == CAUSATIVE_PASSIVE)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 2) + (ending.equals("???") ? "????????????" : "??????"));
                            break;
                        case INFORMAL_PAST:
                        case TE:
                        case CONDITIONAL:
                            switch (ending) {
                                case "???":
                                case "???":
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                                case "???":
                                case "???":
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                            }
                            if (n == TE) {
                                curr = conjugatedLabels[n].getText();
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 1)
                                        + ((ending.equals("???") || ending.equals("???") || ending.equals("???")) ? "???" : "???"));
                            } else if (n == CONDITIONAL) {
                                curr = conjugatedLabels[n].getText();
                                conjugatedLabels[n].setText(curr + "???");
                            }
                            break;
                        case IMPERATIVE:
                        case HYPOTHETICAL:
                        case POTENTIAL:
                            switch (ending) {
                                case "???": conjugatedLabels[n].setText(stem + "???");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "???");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "???");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "???");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "???");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "???");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "???");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "???");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "???");
                                    break;
                            }
                            if (n == HYPOTHETICAL)
                                conjugatedLabels[n].setText(conjugatedLabels[n].getText() + "???");
                            else if (n == POTENTIAL)
                                conjugatedLabels[n].setText(conjugatedLabels[n].getText() + "???");
                            break;
                        case INFORMAL_VOLITIONAL:
                            switch (ending) {
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                                case "???": conjugatedLabels[n].setText(stem + "??????");
                                    break;
                            }
                            break;
                    }
                }
                break;
            case ICHIDAN:
                conjugatedLabels[LONG].setText(stem + "??????");
                conjugatedLabels[FORMAL_NEGATIVE].setText(stem + "?????????");
                conjugatedLabels[FORMAL_PAST].setText(stem + "?????????");
                conjugatedLabels[FORMAL_NEGATIVE_PAST].setText(stem + "??????????????????");
                conjugatedLabels[SHORT].setText(word);
                conjugatedLabels[INFORMAL_NEGATIVE].setText(stem + "??????");
                conjugatedLabels[INFORMAL_PAST].setText(stem + "???");
                conjugatedLabels[INFORMAL_NEGATIVE_PAST].setText(stem + "????????????");
                conjugatedLabels[TE].setText(stem + "???");
                conjugatedLabels[IMPERATIVE].setText(stem + "???");
                conjugatedLabels[INFORMAL_VOLITIONAL].setText(stem + "??????");
                conjugatedLabels[FORMAL_VOLITIONAL].setText(stem + "????????????");
                conjugatedLabels[HYPOTHETICAL].setText(stem + "??????");
                conjugatedLabels[CONDITIONAL].setText(stem + "??????");
                conjugatedLabels[POTENTIAL].setText(stem + "?????????");
                conjugatedLabels[PASSIVE].setText(currentPacket.getTransitivity() == INTRANSITIVE ? "No Passive Form" : stem + "?????????");
                conjugatedLabels[CAUSATIVE].setText(stem + "?????????");
                conjugatedLabels[CAUSATIVE_PASSIVE].setText(stem + "???????????????");
                break;
            case IRREGULAR: //Break down the irregular cases
                byte irr = 0;
                switch (word) {
                    case "??????":
                        irr = 0; break;
                    case "??????":
                        irr = 1; break;
                    case "?????????":
                        irr = 2; break;
                    case "?????????":
                        irr = 3; break;
                    case "??????":
                        irr = 4; break;
                    case "?????????":
                        irr = 5; break;
                    case "??????????????????":
                        irr = 6; break;
                    case "???????????????":
                        irr = 7; break;
                }
                for (byte n = 0; n < conjugationTypes.length; n++)
                    conjugatedLabels[n].setText(irregulars[irr][n]);
                break;
            case SURU:
                conjugatedLabels[LONG].setText(stem + "?????????");
                conjugatedLabels[FORMAL_NEGATIVE].setText(stem + "????????????");
                conjugatedLabels[FORMAL_PAST].setText(stem + "????????????");
                conjugatedLabels[FORMAL_NEGATIVE_PAST].setText(stem + "?????????????????????");
                conjugatedLabels[SHORT].setText(stem + "??????");
                conjugatedLabels[INFORMAL_NEGATIVE].setText(stem + "?????????");
                conjugatedLabels[INFORMAL_PAST].setText(stem + "??????");
                conjugatedLabels[INFORMAL_NEGATIVE_PAST].setText(stem + "???????????????");
                conjugatedLabels[TE].setText(stem + "??????");
                conjugatedLabels[IMPERATIVE].setText(stem + "??????");
                conjugatedLabels[INFORMAL_VOLITIONAL].setText(stem + "?????????");
                conjugatedLabels[FORMAL_VOLITIONAL].setText(stem + "???????????????");
                conjugatedLabels[HYPOTHETICAL].setText(stem + "?????????");
                conjugatedLabels[CONDITIONAL].setText(stem + "?????????");
                conjugatedLabels[POTENTIAL].setText(stem + "?????????");
                conjugatedLabels[PASSIVE].setText(stem + "?????????");
                conjugatedLabels[CAUSATIVE].setText(stem + "?????????");
                conjugatedLabels[CAUSATIVE_PASSIVE].setText(stem + "???????????????");
                break;
        }
        if (!englishOrJapanese && (conjugatedLabels[PASSIVE].getText().equals("No Passive Form")
                || conjugatedLabels[PASSIVE].getText().equals("???????????????"))) {
            conjugatedLabels[PASSIVE].setText("???????????????");
            if (furiganaButton.isSelected())
                furiganaLabels[PASSIVE].setText("??????????????????");
            furiganaLabels[PASSIVE].setBounds(623, (46 * PASSIVE) + 96, 100, 25);
            furiganaLabels[PASSIVE].setVisible(furiganaButton.isSelected());
        }
        repaint();
    }
    
    /**
     * Act on events for buttons presses, window movement, and text updates.
     *
     * @param e the triggered event
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("furigana")) {
            for (JLabel label : furiganaLabels)
                label.setVisible(furiganaButton.isSelected());
            return;
        }
        if (command.equals("englishOrJapanese")) {
            englishOrJapanese = !englishOrJapanese;
            frame.setTitle(englishOrJapanese ? "Japanese Verb Conjugator" : "??????????????????????????????");
            englishOrJapaneseButton.setText(englishOrJapanese ? "?????????" : "English");
            enterColorSelectorButton.setText(englishOrJapanese ? "Edit Colors" : "????????????");
            furiganaButton.setText(englishOrJapanese ? "Furigana" : "????????????");
            for (byte i = 0; i < conjugationTypes.length; i++)
                conjugationTypeLabels[i].setText(englishOrJapanese ? conjugationTypes[i] : conjugationTypesJapanese[i]);
            for (JButton button : speakButtons)
                button.setText(englishOrJapanese ? "Speak" : "??????");
            if (currentPacket != null) {
                verbTypeLabel.setText(encodedStrings(currentPacket.getType()));
                transitivityLabel.setText(encodedStrings(currentPacket.getTransitivity()));
            }
            if (conjugatedLabels[PASSIVE].getText().equals("No Passive Form") || conjugatedLabels[PASSIVE].getText().equals("???????????????")) {
                conjugatedLabels[PASSIVE].setText(englishOrJapanese ? "No Passive Form" : "???????????????");
                if (furiganaButton.isSelected() && !englishOrJapanese) {
                    furiganaLabels[PASSIVE].setText("??????????????????");
                    furiganaLabels[PASSIVE].setBounds(623, (46 * PASSIVE) + 96, 100, 25);
                } else
                    furiganaLabels[PASSIVE].setText("");
            }
            setLabels();
            repaint();
            return;
        }
        if (command.equals("color")) {
            if (colorFrame != null)
                if (colorFrame.isShowing()) {
                    colorFrameRelativePos = new Point(60, 300);
                    colorFrame.setLocation((int) (frame.getX() + colorFrameRelativePos.getX()),
                            (int) (frame.getY() + colorFrameRelativePos.getY()));
                    return;
                }
            colorFrame = new JFrame();
            ComponentListener colorMoveListener = new ComponentListener() {
                @Override
                public void componentResized(ComponentEvent e) {
                }
                
                @Override
                public void componentMoved(ComponentEvent e) {
                    colorFrameRelativePos = new Point(colorFrame.getX() - frame.getX(), colorFrame.getY() - frame.getY());
                }
                
                @Override
                public void componentShown(ComponentEvent e) {
                }
                
                @Override
                public void componentHidden(ComponentEvent e) {
                }
            };
            colorFrame.addComponentListener(colorMoveListener);
            colorFrame.setBounds((int) (frame.getX() + colorFrameRelativePos.getX()),
                    (int) (frame.getY() + colorFrameRelativePos.getY()), 300, 300);
            colorFrame.setResizable(false);
            colorFrame.setAlwaysOnTop(true);
            JComponent newContentPane = new ColorSelector(this);
            newContentPane.setOpaque(true);
            colorFrame.setContentPane(newContentPane);
            colorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            colorFrame.pack();
            colorFrame.setVisible(true);
            colorFrame.addComponentListener(colorMoveListener);
            return;
        }
        String shortForm = conjugatedLabels[4].getText();
        byte button = Byte.parseByte(command);
        if (shortForm.equals("??????")) {
            String form = conjugatedLabels[button].getText();
            play(kuruFuriganas[button] + form.substring(1), true);
        } else {
            if (shortForm.isEmpty()) return;
            if (command.equals("15") && lexicon.get(shortForm).getTransitivity() == INTRANSITIVE)
                play(englishOrJapanese ? "No Passive Form" : "?????????????????????", !englishOrJapanese);
            else
                play(furiganaFull(shortForm, lexicon.get(shortForm).getFurigana(), conjugatedLabels[Byte.parseByte(command)].getText()), true);
        }
    }
    
    /**
     * Attempt to open an input stream from the lexicon.
     * If found, iterate through the entire list and create an entry in the HashSet
     */
    @SuppressWarnings("unchecked")
    private void initializeLexicon() {
        try {
            FileInputStream fis = new FileInputStream("rsc/Lexicon.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            lexicon = (HashMap<String, VerbInfoPacket>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            lexicon = new HashMap<>();
            try {
                File excelFile = new File("rsc/Lexicon.xlsx");
                FileInputStream fis = new FileInputStream(excelFile);
                XSSFWorkbook workbook = new XSSFWorkbook(fis);
                XSSFSheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    String kanji = row.getCell(0).toString();
                    String romaji = row.getCell(2).toString().toLowerCase();
                    String rawMeaning = row.getCell(3).toString();
                    VerbInfoPacket packet = new VerbInfoPacket((byte) row.getCell(4).getNumericCellValue(),
                            (byte) row.getCell(5).getNumericCellValue(), row.getCell(1).toString(), romaji, rawMeaning);
                    lexicon.putIfAbsent(kanji, packet);
                }
                workbook.close();
                fis.close();
                System.out.println("The lexicon was created and contains " + lexicon.keySet().size() +
                        (lexicon.keySet().size() == 1 ? " entry." : " entries."));
                try {
                    FileOutputStream fos = new FileOutputStream("rsc/Lexicon.ser");
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(lexicon);
                    oos.close();
                    fos.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            } catch (Exception ioe) {
                errorMessage(ioe.getMessage(), false);
            }
        }
    }
    
    /**
     * Plays the text to speech sound file for a given piece of text and whether that text is English or Japanese.
     *
     * @param text     The text to be converted to speech and played
     * @param japanese a boolean representing the language of text (English = false and Japanese = true)
     */
    private void play(String text, boolean japanese) {
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() {
                for (JButton button : speakButtons)
                    button.setEnabled(false);
                com.voicerss.tts.VoiceProvider tts = new VoiceProvider("8fe517e0754842509460bff1acb9faa5");
                com.voicerss.tts.VoiceParameters params = new VoiceParameters(text, japanese ? Languages.Japanese : Languages.English_UnitedStates);
                params.setCodec(com.voicerss.tts.AudioCodec.WAV);
                params.setFormat(AudioFormat.Format_44KHZ.AF_44khz_16bit_mono);
                params.setBase64(false);
                params.setSSML(false);
                params.setRate(0);
                byte[] voice = new byte[0];
                try {
                    voice = tts.speech(params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                javax.sound.sampled.SourceDataLine line;
                javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(46000, 16, 1, true, false);
                javax.sound.sampled.DataLine.Info info = new DataLine.Info(javax.sound.sampled.SourceDataLine.class, format);
                try {
                    line = (javax.sound.sampled.SourceDataLine) javax.sound.sampled.AudioSystem.getLine(info);
                    line.open(format);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                line.start();
                line.write(voice, 0, voice.length);
                line.drain();
                line.close();
                for (JButton button : speakButtons)
                    button.setEnabled(true);
                return null;
            }
        };
        worker.execute();
    }
    
    /**
     * Given the kanji short form and its equivalent furigana, derive the furigana for a given conjugated form/
     *
     * @param shortForm     The short form of the verb written in kanji
     * @param furiganaShort The short form of the verb written in furigana
     * @param form          The conjugated verb (of any form) written in kanji
     * @return The furigana of the given conjugated verb (form)
     */
    private String furiganaFull(String shortForm, String furiganaShort, String form) {
        byte numOfKana = 0;
        for (char c : shortForm.toCharArray())
            if ((c >= '???' && c <= '???') || (c >= '???' && c <= '???'))
                numOfKana++;
        byte numOfKanji = (byte) (shortForm.length() - numOfKana);
        return furiganaShort.substring(0, furiganaShort.length() - numOfKana) + form.substring(numOfKanji);
    }
    
    /**
     * Given the short form of a verb written in kanji and kana its the furigana equivalent,
     * return the furigana of the kanji in the verb.
     * Display this result at the appropriate coordinates for each conjugated form.
     *
     * @param shortForm     The short form of the verb written in kanji
     * @param furiganaShort The short form of the verb written in only kana
     */
    private void furiganaOfKanji(String shortForm, String furiganaShort) {
        byte numOfKana = 0;
        for (char c : shortForm.toCharArray())
            if ((c >= '???' && c <= '???') || (c >= '???' && c <= '???'))
                numOfKana++;
        byte numOfKanji = (byte) (shortForm.length() - numOfKana);
        if (numOfKanji != 0)
            for (JLabel label : furiganaLabels)
                label.setBounds(587 + ((numOfKanji - 1) * 10), label.getY(), 100, 25);
        String furiganaOfKanji = furiganaShort.substring(0, furiganaShort.length() - numOfKana);
        for (JLabel label : furiganaLabels)
            label.setText(furiganaOfKanji);
        if (lexicon.get(shortForm).getTransitivity() == INTRANSITIVE)
            furiganaLabels[PASSIVE].setText("");
    }
    
    /**
     * Returns a String array of the trimmed english meanings of a Japanese verb.
     * Removes the word "to" as well as any parentheticals or punctuation marks accordingly.
     *
     * @param meaning A long untrimmed String consisting of several meanings
     *                and parenthetical details separated by various characters
     * @return a String array of the trimmed english meanings given
     * @throws IllegalArgumentException May throw an exception if there are any unclosed parentheticals.
     */
    String[] meaningParser(String meaning) throws IllegalArgumentException {
        meaning = meaning.trim();                                            //Trim
        char[] seq = meaning.toCharArray();                                  //Remove illegal characters
        StringBuilder builder = new StringBuilder();
        for (char c : seq)
            if (Character.isLetter(c) || c == ' ' || c == ';' || c == '(' || c == ')')
                builder.append(c);
        meaning = builder.toString();
        if (meaning.length() == 0) return new String[0];
        byte index = (byte) meaning.indexOf('(');                            //Remove parenthetical segments
        while (index != -1) {
            String former = meaning.substring(0, index);
            byte latterIndex = (byte) meaning.indexOf(')');
            if (latterIndex == -1)
                throw new IllegalArgumentException(meaning);
            String latter = meaning.substring(latterIndex + 1);
            meaning = former + latter;
            index = (byte) meaning.indexOf('(');
        }
        meaning = meaning.replaceAll(" to", "");    //Remove "to"s
        meaning = meaning.replaceAll("to ", "");
        String[] meanings = meaning.split(";");                 //Split on ';'
        for (byte i = 0; i < meanings.length; i++)
            meanings[i] = meanings[i].trim().toLowerCase();          //Remove trailing spaces and put to lowercase
        return meanings;
    }
    
    /**
     * Returns a uniform and clean version of the messy raw
     * String for english definition usually given by the lexicon.
     *
     * @param kanji The short form of the verb in kanji
     * @return a uniform and clean version of the messy raw definition attached to the parameter
     */
    String concatMeanings(String kanji) {
        String[] meanings = meaningParser(lexicon.get(kanji).getTranslation());
        StringBuilder entry = new StringBuilder(kanji + " - " + meanings[0]);
        if (entry.length() > 23) {
            entry = new StringBuilder(entry.substring(0, 23) + "...");
        } else {
            byte concatedMeanings = 1;
            while (entry.length() < 23) {
                if (meanings.length > concatedMeanings && entry.length() + meanings[concatedMeanings].length() < 23) {
                    entry.append("; ").append(meanings[concatedMeanings]);
                    concatedMeanings++;
                } else break;
            }
        }
        return entry.toString();
    }
    
    /**
     * Returns the English or Japanese (based on the selected mode) words for verb types and transitivities.
     *
     * @param code The encoded numeral representing the desired label
     * @return The English or Japanese (based on the selected mode) word for a verb type and transitivity
     */
    private String encodedStrings(byte code) {
        switch (code) {
            case GODAN: return englishOrJapanese ? "Godan" : "??????";
            case ICHIDAN: return englishOrJapanese ? "Ichidan" : "??????";
            case IRREGULAR: return englishOrJapanese ? "Irregular" : "????????????";
            case SURU: return englishOrJapanese ? "Suru" : "????????????";
            case TRANSITIVE: return englishOrJapanese ? "Transitive" : "?????????";
            case INTRANSITIVE: return englishOrJapanese ? "Intransitive" : "?????????";
            case NO_TRANSITIVITY: return englishOrJapanese ? "No Transitivity" : "?????????";
        }
        throw new IllegalArgumentException("Illegal verb code: " + code);
    }
    
    /**
     * Set the colors of all GUI elements and refresh the
     * Conjugator's, AutoSuggestor's, and ColorSelector's JFrame and JPanel.
     */
    void setColors() {
        frame.getContentPane().setBackground(makeTransparent(ColorMap.BACKGROUND.color()));
        frame.getContentPane().setForeground(makeTransparent(ColorMap.BACKGROUND.color()));
        field.setSelectedTextColor(ColorMap.TEXT.color());
        furiganaLabel.setBackground(ColorMap.LABELS.color());
        transitivityLabel.setBackground(ColorMap.LABELS.color());
        translationLabel.setBackground(ColorMap.LABELS.color());
        verbTypeLabel.setBackground(ColorMap.LABELS.color());
        furiganaLabel.setForeground(ColorMap.TEXT.color());
        transitivityLabel.setForeground(ColorMap.TEXT.color());
        translationLabel.setForeground(ColorMap.TEXT.color());
        verbTypeLabel.setForeground(ColorMap.TEXT.color());
        englishOrJapaneseButton.setForeground(ColorMap.TEXT.color());
        furiganaButton.setForeground(ColorMap.TEXT.color());
        enterColorSelectorButton.setForeground(ColorMap.TEXT.color());
        boolean enabled = speakButtons[0].isEnabled();
        for (JButton button : speakButtons) {
            if (!enabled) button.setEnabled(true);
            button.setForeground(ColorMap.TEXT.color());
            button.repaint();
            if (!enabled) button.setEnabled(false);
        }
        for (JLabel label : conjugationTypeLabels)
            label.setForeground(ColorMap.TEXT.color());
        for (JLabel label : conjugatedLabels)
            label.setForeground(ColorMap.TEXT.color());
        for (JLabel label : furiganaLabels)
            label.setForeground(ColorMap.TEXT.color());
        frame.repaint();
        repaint();
        autoSuggestor.resetColor();
    }
    
    /**
     * Set the labels of all relevant GUI elements in the
     * Conjugator, AutoSuggestor, and ColorSelector as well as refresh the display.
     */
    private void setLabels() {
        if (currentPacket == null) {
            furiganaLabel.setText(englishOrJapanese ? "Romaji" : "????????????");
            transitivityLabel.setText(englishOrJapanese ? "Transitivity" : "????????? / ?????????");
            translationLabel.setText(englishOrJapanese ? "English Translation" : "????????????");
            verbTypeLabel.setText(englishOrJapanese ? "Verb Type" : "????????????");
        } else {
            furiganaLabel.setText(englishOrJapanese ? currentPacket.getRomaji() : currentPacket.getFurigana());
            verbTypeLabel.setText(encodedStrings(currentPacket.getType()));
            transitivityLabel.setText(encodedStrings(currentPacket.getTransitivity()));
        }
        
        if (colorFrame != null)
            ((ColorSelector) colorFrame.getContentPane()).resetLabels();
        autoSuggestor.resetColor();
        repaint();
    }
    
    /**
     * Set all labels to empty Strings and refresh the display, effectively clearing all text.
     */
    private void clear() {
        for (JButton button : speakButtons)
            button.setEnabled(false);
        for (JLabel label : conjugatedLabels)
            label.setText("");
        for (JLabel label : furiganaLabels)
            label.setText("");
        furiganaLabel.setText("");
        translationLabel.setText("");
        verbTypeLabel.setText("");
        transitivityLabel.setText("");
        repaint();
    }
    
    /**
     * Respond to a thrown exception by displaying a corresponding error message.
     *
     * @param in             The incoming thrown error message
     * @param missingOrError A boolean representing the two types of errors
     */
    private void errorMessage(String in, boolean missingOrError) {
        clear();
        if (missingOrError) {
            conjugatedLabels[0].setText(in);
            conjugatedLabels[1].setText("was not found in the dictionary.");
        } else {
            conjugatedLabels[0].setText("There is a parenthetical error in the entry with meaning:");
            conjugatedLabels[1].setText(in);
        }
        setLabels();
    }
    
    /**
     * Given a Color instance, return a transparent version of it (a=255)
     *
     * @param color an instance of Color to be made transparent
     * @return a transparent version of color (A value set to 255)
     */
    private Color makeTransparent(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
    }
    
    /**
     * An encapsulating method for reaching the lexicon HashSet from other windows
     *
     * @return the lexicon HashSet object
     */
    Map<String, VerbInfoPacket> getLexicon() {
        return lexicon;
    }
    
    /**
     * Returns true if the currently selected language is English and false if it is Japanese.
     *
     * @return true if the currently selected language is English and false if it is Japanese
     */
    boolean getEnglishOrJapanese() {
        return englishOrJapanese;
    }
}