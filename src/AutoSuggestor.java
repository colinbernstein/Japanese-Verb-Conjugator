import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class AutoSuggestor extends JPanel {
    private Conjugator conjugator;
    private JWindow autoSuggestionPopUpWindow;
    private final Window container;
    private JPanel suggestionsPanel;
    private final JTextField textField;
    private final Map<String, Set<String>> predictiveMap;
    private int currIndex, currScrollIndex;
    private int popupWidth, popupHeight;
    
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
        // JScrollPane scrollPane = new JScrollPane(suggestionsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // scrollPane.setOpaque(true);
        // scrollPane.setVisible(true);
        //scrollPane.addMouseWheelListener(scrollListener);
        String[] keys = {"UP", "DOWN"};
        for (String key : keys)
            textField.getInputMap().put(KeyStroke.getKeyStroke(key), "none");
        suggestionsPanel.setLayout(new GridLayout(0, 1));
        suggestionsPanel.setBackground(ColorMap.AUTO_SUGGESTOR.color());
        suggestionsPanel.setOpaque(true);
        addKeyBindingToRequestFocusInPopUpWindow();
    }
    
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
    
    private void setFocusToTextField() {
        textField.requestFocus();
        textField.selectAll();
    }
    
    private ArrayList<SuggestionLabel> getAddedSuggestionLabels() {
        ArrayList<SuggestionLabel> selectionLabels = new ArrayList<>();
        for (int i = 0; i < suggestionsPanel.getComponentCount(); i++)
            if (suggestionsPanel.getComponent(i) instanceof SuggestionLabel) {
                SuggestionLabel sl = (SuggestionLabel) suggestionsPanel.getComponent(i);
                selectionLabels.add(sl);
            }
        return selectionLabels;
    }
    
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
    
    private void addWordToSuggestions(String word) {
        SuggestionLabel suggestionLabel = new SuggestionLabel(conjugator, word, this);
        suggestionsPanel.add(suggestionLabel);
    }
    
    private void calculatePopUpWindowSize() {
        ArrayList<SuggestionLabel> selectionLabels = getAddedSuggestionLabels();
        if (selectionLabels.size() == 0) return;
        if (popupWidth < selectionLabels.get(0).getPreferredSize().width)
            popupWidth = selectionLabels.get(0).getPreferredSize().width;
        popupHeight = selectionLabels.get(0).getPreferredSize().height * selectionLabels.size();
    }
    
    void showPopUpWindow() {
        ArrayList<SuggestionLabel> selectionLabels = getAddedSuggestionLabels();
        int numOfSuggestions = selectionLabels.size();
        if (numOfSuggestions == 0 || (numOfSuggestions == 1 && textField.getText().equals(selectionLabels.get(0).getText().split(" - ")[0]))) {
            autoSuggestionPopUpWindow.setVisible(false);
            suggestionsPanel.setVisible(false);
            return;
        }
        autoSuggestionPopUpWindow.getContentPane().add(suggestionsPanel);
        autoSuggestionPopUpWindow.setMinimumSize(new Dimension(textField.getWidth() - 10, 30));
        autoSuggestionPopUpWindow.setSize(new Dimension(textField.getWidth() - 10, 30));
        int windowX;
        int windowY;
        windowX = container.getX() + textField.getX() + 5;
        if (suggestionsPanel.getHeight() > autoSuggestionPopUpWindow.getMinimumSize().height)
            windowY = container.getY() + textField.getY() + textField.getHeight() + autoSuggestionPopUpWindow.getMinimumSize().height - 10;
        else
            windowY = container.getY() + textField.getY() + textField.getHeight() + autoSuggestionPopUpWindow.getHeight() - 10;
        autoSuggestionPopUpWindow.setLocation(windowX, windowY);
        autoSuggestionPopUpWindow.setMinimumSize(new Dimension(textField.getWidth() - 10, 30));
        autoSuggestionPopUpWindow.setSize(popupWidth, popupHeight);
        suggestionsPanel.setVisible(true);
        autoSuggestionPopUpWindow.setVisible(true);
        autoSuggestionPopUpWindow.revalidate();
        autoSuggestionPopUpWindow.repaint();
    }
    
    private void initializePredictiveMap(Conjugator conjugator) {
        Comparator<String> byKeyLength = Comparator.comparingInt(String::length).thenComparing(String::compareTo).reversed();
        Map<String, VerbInfoPacket> lexicon = conjugator.getLexicon();
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
            String[] meanings = conjugator.meaningParser(value.getTranslation());
            for (String meaning : meanings)
                for (short i = 1; i <= meaning.length(); i++) {
                    predictiveMap.putIfAbsent(meaning.substring(0, i), new TreeSet<>(byKeyLength));
                    predictiveMap.get(meaning.substring(0, i)).add(key);
                }
        });
        System.out.println("The predictive map contains " + predictiveMap.size() + " entries.");
    }
    
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

class SuggestionLabel extends JLabel {
    private boolean focused;
    private final JTextField textField;
    private final JWindow autoSuggestionsPopUpWindow;
    private Conjugator conjugator;
    
    SuggestionLabel(Conjugator conjugator, String entry, AutoSuggestor autoSuggestor) {
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
    
    void replaceWithSuggestedText() {
        textField.setText(getText().split(" - ")[0]);
        textField.selectAll();
    }
}