/**
 * @Author Colin Bernstein
 * @Title: Japanese Verb Conjugator
 * @version 1.3
 */

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
    
    private static final String[] conjugationTypesJapanese = {"ます形", "ます形（否定形)", "ます形（過去形)", "ます形（否定・過去形)", "辞書形",
            "否定形", "た形", "た形（否定・過去形）", "て形", "命令形", "意向形", "ましょう形", "仮定形", "条件形", "可能形", "受身形", "使役形", "使役受身形"};
    
    private static final String[][] irregulars = {{"来きます", "来きません", "来きました", "来きませんでした",
            "来る", "来ない", "来た", "来なかった", "来て", "来い", "来よう", "来ましょう", "来れば", "来たら", "来られる", "来られる", "来させる", "来させられる"},
            {"行きます", "行きません", "行きました", "行きませんでした", "行く", "行かない", "行った", "行かなかった",
                    "行って", "行け", "行こう", "行きましょう", "行けば", "行ったら", "行ける", "行かれる", "行かせる", "行かされる"},
            {"下さいます", "下さいません", "下さいました", "下さいませんでした", "下さる", "下さらない", "下さった", "下さらなかった",
                    "下さって", "下され", "下さろう", "下さいましょう", "下されば", "下さったら", "下される", "下さられる", "下さらせる", "下さらせられる"},
            {"なさいます", "なさいません", "なさいました", "なさいませんでした", "なさる", "なさらない", "なさった", "なさらなかった",
                    "なさって", "なされ", "なさろう", "なさいましょう", "なされば", "なさったら", "なされる", "なさられる", "なさらせる", "なさらせられる"},
            {"あります", "ありません", "ありました", "ありませんでした", "ある", "ない", "あった", "なかった",
                    "あって", "あれ", "あろう", "ありましょう", "あれば", "あったら", "あれる", "No Passive Form", "あらせる", "あらせられる"},
            {"ございます", "ございません", "ございました", "ございませんでした", "ござる", "ござらない", "ござった", "ござらなかった",
                    "ござって", "ござれ", "ござろう", "ございましょう", "ござれば", "ござったら", "ござれる", "No Passive Form", "ござらせる", "ござらせられる"},
            {"いらっしゃいます", "いらっしゃいません", "いらっしゃいました", "いらっしゃいませんでした", "いらっしゃる", "いらっしゃらない", "いらっしゃった",
                    "いらっしゃらなかった", "いらっしゃって", "いらっしゃれ", "いらっしゃろう", "いらっしゃいましょう", "いらっしゃれば", "いらっしゃったら",
                    "いらっしゃれる", "No Passive Form", "いらっしゃらせる", "いらっしゃらせられる"},
            {"おっしゃいます", "おっしゃいません", "おっしゃいました", "おっしゃいませんでした", "おっしゃる", "おっしゃらない", "おっしゃった", "おっしゃらなかった",
                    "おっしゃって", "おっしゃれ", "おっしゃろう", "おっしゃいましょう", "おっしゃれば", "おっしゃったら", "おっしゃれる", "おっしゃられる",
                    "おっしゃらせる", "おっしゃらせられる"}};
    private static final String[] kuruFuriganas = {"き", "き", "き", "き", "く", "こ", "き", "こ", "き", "こ", "こ", "き", "く", "き", "こ", "こ", "こ", "こ"};
    
    
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
        
        englishOrJapaneseButton = new JButton("日本語");
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
     * @param word - A verb (in english, romaji, furigana, or kanji)
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
        //Get the verb's corresponding info packet from the lexicon TreeSet
        currentPacket = lexicon.get(word);
        String stem = word.substring(0, word.length() - (currentPacket.getType() != SURU ? 1 : 2));
        String ending = word.substring(word.length() - (currentPacket.getType() != SURU ? 1 : 2));
        String furigana = currentPacket.getFurigana();
        
        //Set the furigana labels for the one exception to the regularity of a verb's kanji reading (kuru - to come).
        if (word.equals("来る")) {
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
                                case "う": conjugatedLabels[n].setText(stem + "います");
                                    break;
                                case "く": conjugatedLabels[n].setText(stem + "きます");
                                    break;
                                case "ぐ": conjugatedLabels[n].setText(stem + "ぎます");
                                    break;
                                case "す": conjugatedLabels[n].setText(stem + "します");
                                    break;
                                case "つ": conjugatedLabels[n].setText(stem + "ちます");
                                    break;
                                case "ぬ": conjugatedLabels[n].setText(stem + "にます");
                                    break;
                                case "ぶ": conjugatedLabels[n].setText(stem + "びます");
                                    break;
                                case "む": conjugatedLabels[n].setText(stem + "みます");
                                    break;
                                case "る": conjugatedLabels[n].setText(stem + "ります");
                                    break;
                            }
                            String curr = conjugatedLabels[n].getText();
                            if (n == FORMAL_NEGATIVE)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 1) + "せん");
                            else if (n == FORMAL_PAST)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 1) + "した");
                            else if (n == FORMAL_NEGATIVE_PAST)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 1) + "せんでした");
                            else if (n == FORMAL_VOLITIONAL)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 2) + "ましょう");
                            break;
                        case SHORT:
                            conjugatedLabels[n].setText(word); break;
                        case INFORMAL_NEGATIVE:
                        case INFORMAL_NEGATIVE_PAST:
                        case PASSIVE:
                        case CAUSATIVE:
                        case CAUSATIVE_PASSIVE:
                            switch (ending) {
                                case "う": conjugatedLabels[n].setText(stem + "わない");
                                    break;
                                case "く": conjugatedLabels[n].setText(stem + "かない");
                                    break;
                                case "ぐ": conjugatedLabels[n].setText(stem + "がない");
                                    break;
                                case "す": conjugatedLabels[n].setText(stem + "さない");
                                    break;
                                case "つ": conjugatedLabels[n].setText(stem + "たない");
                                    break;
                                case "ぬ": conjugatedLabels[n].setText(stem + "なない");
                                    break;
                                case "ぶ": conjugatedLabels[n].setText(stem + "ばない");
                                    break;
                                case "む": conjugatedLabels[n].setText(stem + "まない");
                                    break;
                                case "る": conjugatedLabels[n].setText(stem + "らない");
                                    break;
                            }
                            curr = conjugatedLabels[n].getText();
                            if (n == INFORMAL_NEGATIVE_PAST)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 1) + "かった");
                            else if (n == PASSIVE)
                                conjugatedLabels[n].setText(currentPacket.getTransitivity() == INTRANSITIVE ? "No Passive Form" :
                                        curr.substring(0, curr.length() - 2) + "れる");
                            else if (n == CAUSATIVE)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 2) + "せる");
                            else if (n == CAUSATIVE_PASSIVE)
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 2) + (ending.equals("す") ? "せられる" : "れる"));
                            break;
                        case INFORMAL_PAST:
                        case TE:
                        case CONDITIONAL:
                            switch (ending) {
                                case "う":
                                case "つ":
                                case "る": conjugatedLabels[n].setText(stem + "った");
                                    break;
                                case "く": conjugatedLabels[n].setText(stem + "いた");
                                    break;
                                case "ぐ": conjugatedLabels[n].setText(stem + "いだ");
                                    break;
                                case "す": conjugatedLabels[n].setText(stem + "した");
                                    break;
                                case "ぬ":
                                case "ぶ":
                                case "む": conjugatedLabels[n].setText(stem + "んだ");
                                    break;
                            }
                            if (n == TE) {
                                curr = conjugatedLabels[n].getText();
                                conjugatedLabels[n].setText(curr.substring(0, curr.length() - 1)
                                        + ((ending.equals("ぬ") || ending.equals("ぶ") || ending.equals("む")) ? "で" : "て"));
                            } else if (n == CONDITIONAL) {
                                curr = conjugatedLabels[n].getText();
                                conjugatedLabels[n].setText(curr + "ら");
                            }
                            break;
                        case IMPERATIVE:
                        case HYPOTHETICAL:
                        case POTENTIAL:
                            switch (ending) {
                                case "う": conjugatedLabels[n].setText(stem + "え");
                                    break;
                                case "く": conjugatedLabels[n].setText(stem + "け");
                                    break;
                                case "ぐ": conjugatedLabels[n].setText(stem + "げ");
                                    break;
                                case "す": conjugatedLabels[n].setText(stem + "せ");
                                    break;
                                case "つ": conjugatedLabels[n].setText(stem + "て");
                                    break;
                                case "ぬ": conjugatedLabels[n].setText(stem + "ね");
                                    break;
                                case "ぶ": conjugatedLabels[n].setText(stem + "べ");
                                    break;
                                case "む": conjugatedLabels[n].setText(stem + "め");
                                    break;
                                case "る": conjugatedLabels[n].setText(stem + "れ");
                                    break;
                            }
                            if (n == HYPOTHETICAL)
                                conjugatedLabels[n].setText(conjugatedLabels[n].getText() + "ば");
                            else if (n == POTENTIAL)
                                conjugatedLabels[n].setText(conjugatedLabels[n].getText() + "る");
                            break;
                        case INFORMAL_VOLITIONAL:
                            switch (ending) {
                                case "う": conjugatedLabels[n].setText(stem + "おう");
                                    break;
                                case "く": conjugatedLabels[n].setText(stem + "こう");
                                    break;
                                case "ぐ": conjugatedLabels[n].setText(stem + "ごう");
                                    break;
                                case "す": conjugatedLabels[n].setText(stem + "そう");
                                    break;
                                case "つ": conjugatedLabels[n].setText(stem + "とう");
                                    break;
                                case "ぬ": conjugatedLabels[n].setText(stem + "のう");
                                    break;
                                case "ぶ": conjugatedLabels[n].setText(stem + "ぼう");
                                    break;
                                case "む": conjugatedLabels[n].setText(stem + "もう");
                                    break;
                                case "る": conjugatedLabels[n].setText(stem + "ろう");
                                    break;
                            }
                            break;
                    }
                }
                break;
            case ICHIDAN:
                conjugatedLabels[LONG].setText(stem + "ます");
                conjugatedLabels[FORMAL_NEGATIVE].setText(stem + "ません");
                conjugatedLabels[FORMAL_PAST].setText(stem + "ました");
                conjugatedLabels[FORMAL_NEGATIVE_PAST].setText(stem + "ませんでした");
                conjugatedLabels[SHORT].setText(word);
                conjugatedLabels[INFORMAL_NEGATIVE].setText(stem + "ない");
                conjugatedLabels[INFORMAL_PAST].setText(stem + "た");
                conjugatedLabels[INFORMAL_NEGATIVE_PAST].setText(stem + "なかった");
                conjugatedLabels[TE].setText(stem + "て");
                conjugatedLabels[IMPERATIVE].setText(stem + "ろ");
                conjugatedLabels[INFORMAL_VOLITIONAL].setText(stem + "よう");
                conjugatedLabels[FORMAL_VOLITIONAL].setText(stem + "ましょう");
                conjugatedLabels[HYPOTHETICAL].setText(stem + "れば");
                conjugatedLabels[CONDITIONAL].setText(stem + "たら");
                conjugatedLabels[POTENTIAL].setText(stem + "られる");
                conjugatedLabels[PASSIVE].setText(currentPacket.getTransitivity() == INTRANSITIVE ? "No Passive Form" : stem + "られる");
                conjugatedLabels[CAUSATIVE].setText(stem + "させる");
                conjugatedLabels[CAUSATIVE_PASSIVE].setText(stem + "させられる");
                break;
            case IRREGULAR: //Break down the irregular cases
                byte irr = 0;
                switch (word) {
                    case "来る":
                        irr = 0; break;
                    case "行く":
                        irr = 1; break;
                    case "下さる":
                        irr = 2; break;
                    case "なさる":
                        irr = 3; break;
                    case "ある":
                        irr = 4; break;
                    case "ござる":
                        irr = 5; break;
                    case "いらっしゃる":
                        irr = 6; break;
                    case "おっしゃる":
                        irr = 7; break;
                }
                for (byte n = 0; n < conjugationTypes.length; n++)
                    conjugatedLabels[n].setText(irregulars[irr][n]);
                break;
            case SURU:
                conjugatedLabels[LONG].setText(stem + "します");
                conjugatedLabels[FORMAL_NEGATIVE].setText(stem + "しません");
                conjugatedLabels[FORMAL_PAST].setText(stem + "しました");
                conjugatedLabels[FORMAL_NEGATIVE_PAST].setText(stem + "しませんでした");
                conjugatedLabels[SHORT].setText(stem + "する");
                conjugatedLabels[INFORMAL_NEGATIVE].setText(stem + "しない");
                conjugatedLabels[INFORMAL_PAST].setText(stem + "した");
                conjugatedLabels[INFORMAL_NEGATIVE_PAST].setText(stem + "しなかった");
                conjugatedLabels[TE].setText(stem + "して");
                conjugatedLabels[IMPERATIVE].setText(stem + "しろ");
                conjugatedLabels[INFORMAL_VOLITIONAL].setText(stem + "しよう");
                conjugatedLabels[FORMAL_VOLITIONAL].setText(stem + "しましょう");
                conjugatedLabels[HYPOTHETICAL].setText(stem + "すれば");
                conjugatedLabels[CONDITIONAL].setText(stem + "したら");
                conjugatedLabels[POTENTIAL].setText(stem + "できる");
                conjugatedLabels[PASSIVE].setText(stem + "される");
                conjugatedLabels[CAUSATIVE].setText(stem + "させる");
                conjugatedLabels[CAUSATIVE_PASSIVE].setText(stem + "させられる");
                break;
        }
        if (!englishOrJapanese && (conjugatedLabels[PASSIVE].getText().equals("No Passive Form")
                || conjugatedLabels[PASSIVE].getText().equals("受身形無し"))) {
            conjugatedLabels[PASSIVE].setText("受身形無し");
            if (furiganaButton.isSelected())
                furiganaLabels[PASSIVE].setText("うけみけいな");
            furiganaLabels[PASSIVE].setBounds(623, (46 * PASSIVE) + 96, 100, 25);
            furiganaLabels[PASSIVE].setVisible(furiganaButton.isSelected());
        }
        repaint();
    }
    
    /**
     * Act on events for buttons presses, window movement, and text updates
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
            frame.setTitle(englishOrJapanese ? "Japanese Verb Conjugator" : "日本語動詞活用アプリ");
            englishOrJapaneseButton.setText(englishOrJapanese ? "日本語" : "English");
            enterColorSelectorButton.setText(englishOrJapanese ? "Edit Colors" : "色を選ぶ");
            furiganaButton.setText(englishOrJapanese ? "Furigana" : "振り仮名");
            for (byte i = 0; i < conjugationTypes.length; i++)
                conjugationTypeLabels[i].setText(englishOrJapanese ? conjugationTypes[i] : conjugationTypesJapanese[i]);
            for (JButton button : speakButtons)
                button.setText(englishOrJapanese ? "Speak" : "話す");
            if (currentPacket != null) {
                verbTypeLabel.setText(encodedStrings(currentPacket.getType()));
                transitivityLabel.setText(encodedStrings(currentPacket.getTransitivity()));
            }
            if (conjugatedLabels[PASSIVE].getText().equals("No Passive Form") || conjugatedLabels[PASSIVE].getText().equals("受身形無し")) {
                conjugatedLabels[PASSIVE].setText(englishOrJapanese ? "No Passive Form" : "受身形無し");
                if (furiganaButton.isSelected() && !englishOrJapanese) {
                    furiganaLabels[PASSIVE].setText("うけみけいな");
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
            //frame.setComponentZOrder(autoSuggestor.getAutoSuggestionPopUpWindow(), 0);
            //frame.setComponentZOrder(colorFrame.getContentPane(), 1);
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
        if (shortForm.equals("来る")) {
            String form = conjugatedLabels[button].getText();
            play(kuruFuriganas[button] + form.substring(1), true);
        } else {
            if (shortForm.isEmpty()) return;
            if (command.equals("15") && lexicon.get(shortForm).getTransitivity() == INTRANSITIVE)
                play(englishOrJapanese ? "No Passive Form" : "うけみけいなし", !englishOrJapanese);
            else
                play(furiganaFull(shortForm, lexicon.get(shortForm).getFurigana(), conjugatedLabels[Byte.parseByte(command)].getText()), true);
        }
    }
    
    /**
     *
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
    
    private String furiganaFull(String shortForm, String furiganaShort, String form) {
        byte numOfKana = 0;
        for (char c : shortForm.toCharArray())
            if ((c >= 'あ' && c <= 'ん') || (c >= 'ア' && c <= 'ン'))
                numOfKana++;
        byte numOfKanji = (byte) (shortForm.length() - numOfKana);
        return furiganaShort.substring(0, furiganaShort.length() - numOfKana) + form.substring(numOfKanji);
    }
    
    private void furiganaOfKanji(String shortForm, String furiganaShort) {
        byte numOfKana = 0;
        //List<String> furiganas = new ArrayList<>();
        //boolean consecutiveKanji = false;
        for (char c : shortForm.toCharArray())
            if ((c >= 'あ' && c <= 'ん') || (c >= 'ア' && c <= 'ン')) {
                numOfKana++;
                //consecutiveKanji = false;
            } //else {
        //    consecutiveKanji = true;
        // }
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
    
    private String encodedStrings(byte code) {
        switch (code) {
            case GODAN: return englishOrJapanese ? "Godan" : "五段";
            case ICHIDAN: return englishOrJapanese ? "Ichidan" : "一段";
            case IRREGULAR: return englishOrJapanese ? "Irregular" : "変格活用";
            case SURU: return englishOrJapanese ? "Suru" : "サ変名詞";
            case TRANSITIVE: return englishOrJapanese ? "Transitive" : "推移的";
            case INTRANSITIVE: return englishOrJapanese ? "Intransitive" : "自動詞";
            case NO_TRANSITIVITY: return englishOrJapanese ? "No Transitivity" : "無適用";
        }
        throw new IllegalArgumentException("Illegal verb code: " + code);
    }
    
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
    
    private void setLabels() {
        if (currentPacket == null) {
            furiganaLabel.setText(englishOrJapanese ? "Romaji" : "振り仮名");
            transitivityLabel.setText(englishOrJapanese ? "Transitivity" : "推移的 / 自動詞");
            translationLabel.setText(englishOrJapanese ? "English Translation" : "英語翻訳");
            verbTypeLabel.setText(englishOrJapanese ? "Verb Type" : "動詞種類");
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
    
    private Color makeTransparent(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
    }
    
    Map<String, VerbInfoPacket> getLexicon() {
        return lexicon;
    }
    
    boolean getEnglishOrJapanese() {
        return englishOrJapanese;
    }
}