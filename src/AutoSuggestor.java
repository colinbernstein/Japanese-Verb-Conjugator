import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * A predictive AutoSuggestor to quickly and easily find a
 * Japanese verb via the input of English, romaji, kana, and kanji
 *
 * @author Colin Bernstein
 * @version 1.4
 */
class AutoSuggestor extends JPanel {
    private Conjugator conjugator;
    private JWindow autoSuggestionPopUpWindow;
    private final Window container;
    private JPanel suggestionsPanel;
    private final JTextField textField;
    private Map<String, Set<String>> predictiveMap;
    private int currIndex, currScrollIndex, popupWidth, popupHeight;
    
    /**
     * Creates and initializes all relevant GUI elements including the text field and predictions window.
     * Also sets up action listeners for typing,
     * arrow key and mouse-scroll navigation, and key presses such as Tab and Enter.
     *
     * @param conjugator The central object of the program representing the main GUI window
     * @param mainWindow The Window object associated with the Conjugator
     * @param textField  The textField object to attach to
     */
    AutoSuggestor(Conjugator conjugator, Window mainWindow, JTextField textField) {
        predictiveMap = new HashMap<>();
        initializePredictiveMap(conjugator);
        textField.setFocusable(true);
        textField.setFocusTraversalKeysEnabled(false);
        this.conjugator = conjugator;
        this.textField = textField;
        this.container = mainWindow;
        
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                currScrollIndex = 0;
                checkForAndShowSuggestions();
            }
            
            @Override
            public void removeUpdate(DocumentEvent de) {
                currScrollIndex = 0;
                checkForAndShowSuggestions();
            }
            
            @Override
            public void changedUpdate(DocumentEvent de) {
            }
        };
        
        MouseWheelListener scrollListener = e -> {
            try {
                //Thread.sleep(0, 300000);
                Thread.sleep(0, 100000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            int numOfPredictions = predictiveMap.get(getCurrentlyTypedWord()).size();
            if (numOfPredictions > 52 && suggestionsPanel.isVisible()) {
                int newScrollIndex = currScrollIndex + e.getUnitsToScroll();
                if (newScrollIndex != currScrollIndex)
                    if (52 + newScrollIndex < numOfPredictions && newScrollIndex >= 0) {
                        currScrollIndex = newScrollIndex;
                        checkForAndShowSuggestions();
                    }
            }
        };
        
        this.textField.getDocument().addDocumentListener(documentListener);
        autoSuggestionPopUpWindow = new JWindow(container);
        autoSuggestionPopUpWindow.setOpacity(1.00f);
        autoSuggestionPopUpWindow.setBackground(new Color(0, 0, 0, 0));
        suggestionsPanel = new JPanel();
        suggestionsPanel.addMouseWheelListener(scrollListener);
        String[] keys = {"UP", "DOWN"};
        for (String key : keys)
            textField.getInputMap().put(KeyStroke.getKeyStroke(key), "none");
        suggestionsPanel.setLayout(new GridLayout(0, 1));
        suggestionsPanel.setBackground(ColorMap.AUTO_SUGGESTOR.color());
        suggestionsPanel.setOpaque(true);
        addKeyBindingToRequestFocusInPopUpWindow();
    }
    
    /**
     * Add corresponding key bindings to the arrow, tab, and enter keys.
     */
    private void addKeyBindingToRequestFocusInPopUpWindow() {
        textField.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(
                KeyEvent.VK_DOWN, 0, false), "Down pressed");
        textField.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(
                KeyEvent.VK_TAB, 0, false), "Tab pressed");
        textField.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(
                KeyEvent.VK_UP, 0, false), "Up pressed");
        textField.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, 0, false), "Enter pressed");
        textField.getActionMap().put("Down pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ArrayList<SuggestionLabel> selectionLabels = getAddedSuggestionLabels();
                int max = selectionLabels.size();
                if (max > 1) {
                    if (currIndex == 0 && !selectionLabels.get(0).isFocused())
                        selectionLabels.get(0).setFocused(true);
                    else {
                        if (currIndex < max - 1) {
                            selectionLabels.get(currIndex++).setFocused(false);
                            selectionLabels.get(currIndex).setFocused(true);
                        } else {
                            selectionLabels.get(currIndex).setFocused(false);
                            currIndex = 0;
                            selectionLabels.get(currIndex).setFocused(true);
                        }
                    }
                } else if (max == 0) {
                    suggestionsPanel.setVisible(false);
                    autoSuggestionPopUpWindow.setVisible(false);
                } else
                    selectionLabels.get(0).setFocused(true);
            }
        });
        textField.getActionMap().put("Tab pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (suggestionsPanel.isVisible()) {
                    ArrayList<SuggestionLabel> selectionLabels = getAddedSuggestionLabels();
                    int max = selectionLabels.size();
                    if (max > 1) {
                        if (currIndex == 0 && !selectionLabels.get(0).isFocused())
                            selectionLabels.get(0).setFocused(true);
                        else {
                            if (currIndex < max - 1) {
                                selectionLabels.get(currIndex++).setFocused(false);
                                selectionLabels.get(currIndex).setFocused(true);
                            } else {
                                selectionLabels.get(currIndex).setFocused(false);
                                currIndex = 0;
                                selectionLabels.get(currIndex).setFocused(true);
                            }
                        }
                    } else if (max == 0) {
                        suggestionsPanel.setVisible(false);
                        autoSuggestionPopUpWindow.setVisible(false);
                    } else {
                        selectionLabels.get(0).replaceWithSuggestedText();
                        conjugator.conjugate(textField.getText().split(" - ")[0]);
                        suggestionsPanel.setVisible(false);
                        autoSuggestionPopUpWindow.setVisible(false);
                        setFocusToTextField();
                    }
                }
            }
        });
        textField.getActionMap().put("Up pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ArrayList<SuggestionLabel> selectionLabels = getAddedSuggestionLabels();
                int max = selectionLabels.size();
                if (max > 1) {
                    if (currIndex > 0) {
                        selectionLabels.get(currIndex--).setFocused(false);
                        selectionLabels.get(currIndex).setFocused(true);
                    } else {
                        selectionLabels.get(currIndex).setFocused(false);
                        currIndex = (byte) (max - 1);
                        selectionLabels.get(currIndex).setFocused(true);
                    }
                } else if (max == 0) {
                    suggestionsPanel.setVisible(false);
                    autoSuggestionPopUpWindow.setVisible(false);
                } else
                    selectionLabels.get(0).setFocused(true);
            }
        });
        textField.getActionMap().put("Enter pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ArrayList<SuggestionLabel> selectionLabels = getAddedSuggestionLabels();
                if (selectionLabels.size() > currIndex && selectionLabels.get(currIndex).isFocused())
                    selectionLabels.get(currIndex).replaceWithSuggestedText();
                conjugator.conjugate(getCurrentlyTypedWord());
                suggestionsPanel.setVisible(false);
                autoSuggestionPopUpWindow.setVisible(false);
                setFocusToTextField();
                currIndex = 0;
            }
        });
    }
    
    /**
     * Focus on the text field as to maintain the ability to type into it.
     */
    private void setFocusToTextField() {
        textField.requestFocus();
        textField.selectAll();
    }
    
    /**
     * @return an ArrayList of every SuggestionLabel in the current found list of suggestions
     */
    private ArrayList<SuggestionLabel> getAddedSuggestionLabels() {
        ArrayList<SuggestionLabel> selectionLabels = new ArrayList<>();
        for (int i = 0; i < suggestionsPanel.getComponentCount(); i++)
            if (suggestionsPanel.getComponent(i) instanceof SuggestionLabel) {
                SuggestionLabel sl = (SuggestionLabel) suggestionsPanel.getComponent(i);
                selectionLabels.add(sl);
            }
        return selectionLabels;
    }
    
    /**
     * Using the given text currently typed into AutoSuggestor text field as a key,
     * get the HashSet value (all relevant suggestions beginning with that text).
     */
    private void checkForAndShowSuggestions() {
        currIndex = 0;
        suggestionsPanel.removeAll();
        popupWidth = 0;
        popupHeight = 0;
        boolean added = wordTyped(getCurrentlyTypedWord());
        if (!added) {
            if (autoSuggestionPopUpWindow.isVisible())
                autoSuggestionPopUpWindow.setVisible(false);
        } else
            showPopUpWindow();
    }
    
    /**
     * Create a new suggestion label and add it to the AutoSuggestor and Conjugator windows.
     *
     * @param word The verb to make a suggestion label for
     */
    private void addWordToSuggestions(String word) {
        SuggestionLabel suggestionLabel = new SuggestionLabel(conjugator, this, word);
        suggestionsPanel.add(suggestionLabel);
    }
    
    /**
     * Calculate the dimensions and coordinates of the
     * popup window using the relative positions of the supporting frames.
     * Set relevant instance variables to these values.
     */
    private void calculatePopUpWindowSize() {
        ArrayList<SuggestionLabel> selectionLabels = getAddedSuggestionLabels();
        if (selectionLabels.size() == 0) return;
        if (popupWidth < selectionLabels.get(0).getPreferredSize().width)
            popupWidth = selectionLabels.get(0).getPreferredSize().width;
        popupHeight = selectionLabels.get(0).getPreferredSize().height * selectionLabels.size();
    }
    
    /**
     * Sets the position and sizing of the popup window and displays it accordingly.
     */
    void showPopUpWindow() {
        ArrayList<SuggestionLabel> selectionLabels = getAddedSuggestionLabels();
        int numOfSuggestions = selectionLabels.size();
        if (numOfSuggestions == 0 || (numOfSuggestions == 1 && textField.getText().equals(selectionLabels.get(0).getText().split(" - ")[0]))) {
            autoSuggestionPopUpWindow.setVisible(false);
            suggestionsPanel.setVisible(false);
            return;
        }
        suggestionsPanel.setVisible(true);
        autoSuggestionPopUpWindow.setVisible(true);
        autoSuggestionPopUpWindow.getContentPane().add(suggestionsPanel);
        autoSuggestionPopUpWindow.setMinimumSize(new Dimension(textField.getWidth() - 10, 30));
        autoSuggestionPopUpWindow.setSize(new Dimension(textField.getWidth() - 10, 30));
        int windowX, windowY;
        windowX = container.getX() + textField.getX() + 5;
        if (suggestionsPanel.getHeight() > autoSuggestionPopUpWindow.getMinimumSize().height)
            windowY = container.getY() + textField.getY() + textField.getHeight() + autoSuggestionPopUpWindow.getMinimumSize().height - 10;
        else
            windowY = container.getY() + textField.getY() + textField.getHeight() + autoSuggestionPopUpWindow.getHeight() - 10;
        autoSuggestionPopUpWindow.setLocation(windowX, windowY);
        autoSuggestionPopUpWindow.setMinimumSize(new Dimension(textField.getWidth() - 10, 30));
        autoSuggestionPopUpWindow.setSize(popupWidth, popupHeight);
        autoSuggestionPopUpWindow.revalidate();
        autoSuggestionPopUpWindow.repaint();
    }
    
    /**
     * Gets the predictive HashMap using a one-time calculation and Serializable object saving.
     * <p>
     * Search for a predefined predictive map object by trying to open an ObjectInputStream.
     * If one is found, load it in and set the instance variable predictiveMap equal to it.
     * <p>
     * If no map has been previously defined, create a new one by cycling
     * through all Japanese verbs in the Conjugator's lexicon and creating
     * a HashMap linking partially typed words (in english, romaji, kana, and kanji) to full words.
     * Lastly, save the map in the directory of the program for future use.
     *
     * @param conjugator the Conjugator object on which the AutoSuggestor is built
     */
    @SuppressWarnings("unchecked")
    private void initializePredictiveMap(Conjugator conjugator) {
        try {
            FileInputStream fis = new FileInputStream("rsc/PredictiveMap.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            predictiveMap = (Map<String, Set<String>>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            predictiveMap = new HashMap<>();
            try {
                class ByKeyLength implements Comparator<String>, Serializable {
                    private ByKeyLength() {
                    }
                    
                    @Override
                    public int compare(String o1, String o2) {
                        return Comparator.comparingInt(String::length).thenComparing(String::compareTo).reversed().compare(o1, o2);
                    }
                    
                    @Override
                    public boolean equals(Object obj) {
                        if (obj instanceof String)
                            return Comparator.comparingInt(String::length).thenComparing(String::compareTo).reversed().equals(obj);
                        throw new IllegalArgumentException();
                    }
                }
                Map<String, VerbInfoPacket> lexicon = conjugator.getLexicon();
                Comparator<String> byKeyLength = new ByKeyLength();
                lexicon.forEach((key, value) -> {
                    for (short i = 1; i <= key.length(); i++) {
                        predictiveMap.putIfAbsent(key.substring(0, i), new TreeSet<>(byKeyLength));
                        predictiveMap.get(key.substring(0, i)).add(key);
                    }
                    for (short i = 1; i <= value.getFurigana().length(); i++) {
                        predictiveMap.putIfAbsent(value.getFurigana().substring(0, i), new TreeSet<>(byKeyLength));
                        predictiveMap.get(value.getFurigana().substring(0, i)).add(key);
                    }
                    for (short i = 1; i <= value.getRomaji().length(); i++) {
                        predictiveMap.putIfAbsent(value.getRomaji().substring(0, i), new TreeSet<>(byKeyLength));
                        predictiveMap.get(value.getRomaji().substring(0, i)).add(key);
                    }
                    for (String meaning : conjugator.meaningParser(value.getTranslation()))
                        for (short i = 1; i <= meaning.length(); i++) {
                            predictiveMap.putIfAbsent(meaning.substring(0, i), new TreeSet<>(byKeyLength));
                            predictiveMap.get(meaning.substring(0, i)).add(key);
                        }
                });
                System.out.println("The predictive map was created and contains " + predictiveMap.size() + " entries.");
                try {
                    FileOutputStream fos = new FileOutputStream("rsc/PredictiveMap.ser");
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(predictiveMap);
                    oos.close();
                    fos.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    /**
     * Returns whether or not the predictive map contains a key associated with the currently typed text.
     *
     * @param typedWord The text currently present in the text field
     * @return a boolean whether or not the predictive map contains a key associated with the currently typed text
     */
    private boolean wordTyped(String typedWord) {
        if (typedWord.isEmpty()) {return false;}
        boolean predictiveMapContains = predictiveMap.containsKey(typedWord);
        if (predictiveMapContains) {
            Set<String> suggestions = predictiveMap.get(typedWord);
            for (int i = currScrollIndex; i < Math.min(suggestions.size(), currScrollIndex + 52); i++)
                addWordToSuggestions(suggestions.toArray(new String[0])[i]);
            calculatePopUpWindowSize();
        }
        return predictiveMapContains;
    }
    
    /**
     * Get the associated colors from the ColorMap and set the corresponding GUI elements to those colors.
     */
    void resetColor() {
        suggestionsPanel.setBackground(ColorMap.AUTO_SUGGESTOR.color());
        ArrayList<SuggestionLabel> addedSuggestionLabels = getAddedSuggestionLabels();
        for (SuggestionLabel label : addedSuggestionLabels)
            label.setForeground(ColorMap.TEXT.color());
        textField.setForeground(ColorMap.TEXT.color());
        if (addedSuggestionLabels.size() > currIndex)
            addedSuggestionLabels.get(currIndex).setBorder(new LineBorder(ColorMap.LABEL_BORDERS.color()));
        repaint();
    }
    
    JWindow getAutoSuggestionPopUpWindow() {
        return autoSuggestionPopUpWindow;
    }
    
    JTextField getTextField() {
        return textField;
    }
    
    private String getCurrentlyTypedWord() {
        return textField.getText().trim().toLowerCase();
    }
}

/**
 * A SuggestionLabel object is essentially a small JLabel placed into the AutoSuggestor popup window.
 * Each contains text for the Japanese and English root forms of the verb.
 * Each has a mouse listener waiting which will conjugate that given word.
 */
class SuggestionLabel extends JLabel {
    private boolean focused;
    private final JTextField textField;
    private final JWindow autoSuggestionsPopUpWindow;
    private Conjugator conjugator;
    
    SuggestionLabel(Conjugator conjugator, AutoSuggestor autoSuggestor, String entry) {
        super(conjugator.concatMeanings(entry));
        this.textField = autoSuggestor.getTextField();
        this.autoSuggestionsPopUpWindow = autoSuggestor.getAutoSuggestionPopUpWindow();
        this.conjugator = conjugator;
        initLabel();
    }
    
    private void initLabel() {
        setFocusable(false);
        setForeground(ColorMap.TEXT.color());
        setOpaque(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                super.mouseClicked(me);
                replaceWithSuggestedText();
                autoSuggestionsPopUpWindow.setVisible(false);
                conjugator.conjugate(getText().split(" - ")[0]);
            }
        });
    }
    
    /**
     * Focus on (or unfocus) a this SuggestionLabel by defining a colored border.
     * @param focused A boolean when if true, focuses this label, and if false, unfocuses it.
     */
    public void setFocused(boolean focused) {
        if (focused)
            setBorder(new LineBorder(ColorMap.LABEL_BORDERS.color()));
        else
            setBorder(null);
        repaint();
        this.focused = focused;
    }
    
    public boolean isFocused() {
        return focused;
    }
    
    /**
     * Sets the contents of the text field equal to the Japanese short form of this SuggestionLabel
     */
    void replaceWithSuggestedText() {
        textField.setText(getText().split(" - ")[0]);
        textField.selectAll();
    }
}