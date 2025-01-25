package com.terminal.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.terminal.TerminalPanel;

public class SidebarManager {
    private final JPanel sidebarPanel;
    private final JList<String> historyList;
    private final JList<String> favoritesList;
    private final DefaultListModel<String> historyModel;
    private final DefaultListModel<String> favoritesModel;
    private final Set<String> favorites;
    private static final int MAX_HISTORY_SIZE = 100;
    private Color backgroundColor;
    private Color foregroundColor;
    private Color selectionColor;
    private Color borderColor;
    private int hoveredTabIndex = -1;
    private static final String FAVORITES_FILE = "content/data/favorites.json";
    private final Gson gson;
    private final TerminalPanel terminalPanel;
    private JTextField searchField;
    private String lastSearchQuery = "";
    private JTabbedPane tabbedPane;

    private void setupSearchField() {
        searchField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2d.setColor(new Color(foregroundColor.getRed(), foregroundColor.getGreen(), 
                                         foregroundColor.getBlue(), 80));
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString("Поиск...", 
                        getInsets().left, 
                        getHeight() / 2 + fm.getAscent() / 2 - 1);
                    g2d.dispose();
                }
            }
        };
        searchField.setBackground(backgroundColor);
        searchField.setForeground(foregroundColor);
        searchField.setCaretColor(foregroundColor);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                searchField.repaint();
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                searchField.repaint();
            }
        });
        
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setForeground(foregroundColor);
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(backgroundColor);
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterLists(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterLists(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterLists(); }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                JList<?> currentList = tabbedPane.getSelectedIndex() == 0 ? historyList : favoritesList;
                
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        if (currentList.getSelectedValue() != null) {
                            terminalPanel.insertCommand((String)currentList.getSelectedValue());
                        }
                        break;
                    case KeyEvent.VK_UP:
                        int prevIndex = currentList.getSelectedIndex() - 1;
                        if (prevIndex >= 0) {
                            currentList.setSelectedIndex(prevIndex);
                            currentList.ensureIndexIsVisible(prevIndex);
                        }
                        e.consume();
                        break;
                    case KeyEvent.VK_DOWN:
                        int nextIndex = currentList.getSelectedIndex() + 1;
                        if (nextIndex < currentList.getModel().getSize()) {
                            currentList.setSelectedIndex(nextIndex);
                            currentList.ensureIndexIsVisible(nextIndex);
                        }
                        e.consume();
                        break;
                }
            }
        });
    }

    private void filterLists() {
        String searchQuery = searchField.getText().toLowerCase();
        if (searchQuery.equals(lastSearchQuery)) return;
        lastSearchQuery = searchQuery;

        DefaultListModel<String> filteredHistory = new DefaultListModel<>();
        for (int i = 0; i < historyModel.size(); i++) {
            String command = historyModel.getElementAt(i);
            if (command.toLowerCase().contains(searchQuery)) {
                filteredHistory.addElement(command);
            }
        }
        historyList.setModel(searchQuery.isEmpty() ? historyModel : filteredHistory);

        DefaultListModel<String> filteredFavorites = new DefaultListModel<>();
        for (String command : favorites) {
            if (command.toLowerCase().contains(searchQuery)) {
                filteredFavorites.addElement(command);
            }
        }
        favoritesList.setModel(searchQuery.isEmpty() ? favoritesModel : filteredFavorites);
    }

    public SidebarManager(TerminalPanel terminalPanel) {
        this.terminalPanel = terminalPanel;
        sidebarPanel = new JPanel(new BorderLayout());
        historyModel = new DefaultListModel<>();
        favoritesModel = new DefaultListModel<>();
        favorites = new HashSet<>();
        gson = new GsonBuilder().setPrettyPrinting().create();

        new File("content/data").mkdirs();
        loadFavorites();
        updateColors();
        setupSearchField();

        terminalPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                clearSelections();
            }
        });

        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                
                if (isSelected) {
                    label.setBackground(selectionColor);
                    label.setForeground(foregroundColor);
                } else {
                    label.setBackground(backgroundColor);
                    label.setForeground(foregroundColor);
                }

                if (!searchField.getText().isEmpty()) {
                    String text = value.toString();
                    String searchQuery = searchField.getText().toLowerCase();
                    if (text.toLowerCase().contains(searchQuery)) {
                        label.setText(highlightText(text, searchQuery));
                    }
                }
                
                return label;
            }
        };

        historyList = new JList<>(historyModel);
        historyList.setCellRenderer(renderer);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.setBackground(backgroundColor);
        historyList.setForeground(foregroundColor);
        
        favoritesList = new JList<>(favoritesModel);
        favoritesList.setCellRenderer(renderer);
        favoritesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        favoritesList.setBackground(backgroundColor);
        favoritesList.setForeground(foregroundColor);

        JPopupMenu favoritesMenu = new JPopupMenu();
        JMenuItem removeItem = new JMenuItem("Удалить из избранного");
        removeItem.addActionListener(e -> {
            String selected = favoritesList.getSelectedValue();
            if (selected != null) {
                removeFromFavorites(selected);
            }
        });
        favoritesMenu.add(removeItem);
        favoritesList.setComponentPopupMenu(favoritesMenu);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(backgroundColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };

        tabbedPane.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int newIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                if (newIndex != hoveredTabIndex) {
                    hoveredTabIndex = newIndex;
                    tabbedPane.repaint();
                }
            }
        });

        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredTabIndex = -1;
                tabbedPane.repaint();
            }
        });

        UIManager.put("TabbedPane.focus", backgroundColor);
        UIManager.put("TabbedPane.selected", backgroundColor);
        UIManager.put("TabbedPane.selectedForeground", foregroundColor);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabInsets", new Insets(5, 10, 5, 10));
        
        tabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                shadow = backgroundColor;
                darkShadow = backgroundColor;
                lightHighlight = backgroundColor;
                focus = backgroundColor;
            }
            
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                if (isSelected) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(foregroundColor);
                    g2d.drawLine(x, y + h - 1, x + w, y + h - 1);
                    g2d.dispose();
                }
            }
            
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            }
            
            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, 
                    int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                g.setColor(backgroundColor);
                g.fillRect(x, y, w, h);
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                    int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setColor(getTabTextColor(isSelected, tabIndex == hoveredTabIndex));
                g2d.setFont(font);
                g2d.drawString(title, textRect.x, textRect.y + metrics.getAscent());
                g2d.dispose();
            }
        });

        JLabel historyLabel = new JLabel("История");
        historyLabel.setForeground(foregroundColor);
        
        JPanel favoritesTabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        favoritesTabPanel.setOpaque(false);
        JLabel favoritesLabel = new JLabel("Избранное");
        favoritesLabel.setForeground(foregroundColor);
        JLabel helpIcon = new JLabel("\uD83D\uDEC8");
        helpIcon.setForeground(foregroundColor);
        helpIcon.setFont(helpIcon.getFont().deriveFont(14f));
        helpIcon.setToolTipText("<html>Чтобы добавить команду в избранное:<br>" +
                              "1. Щелкните правой кнопкой мыши по команде в истории<br>" +
                              "2. Выберите 'Добавить в избранное'<br></html>");
        favoritesTabPanel.add(favoritesLabel);
        favoritesTabPanel.add(helpIcon);

        tabbedPane.addTab("История", new JScrollPane(historyList));
        tabbedPane.setTabComponentAt(0, historyLabel);
        tabbedPane.addTab("Избранное", new JScrollPane(favoritesList));
        tabbedPane.setTabComponentAt(1, favoritesTabPanel);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(backgroundColor);
        contentPanel.add(searchField, BorderLayout.NORTH);
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        
        sidebarPanel.add(contentPanel, BorderLayout.CENTER);
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, borderColor));

        JPopupMenu historyMenu = new JPopupMenu();
        JMenuItem addToFavoritesItem = new JMenuItem("Добавить в избранное");
        addToFavoritesItem.addActionListener(e -> {
            String selected = historyList.getSelectedValue();
            if (selected != null) {
                addToFavorites(selected);
            }
        });
        historyMenu.add(addToFavoritesItem);
        historyList.setComponentPopupMenu(historyMenu);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JList<?> list = (JList<?>) e.getSource();
                    String selected = (String) list.getSelectedValue();
                    if (selected != null) {
                        terminalPanel.insertCommand(selected);
                    }
                }
            }
        };
        historyList.addMouseListener(mouseAdapter);
        favoritesList.addMouseListener(mouseAdapter);
    }

    private void updateColors() {
        ThemeManager themeManager = ThemeManager.getInstance();
        backgroundColor = Color.decode(themeManager.getThemeColor("background"));
        foregroundColor = Color.decode(themeManager.getThemeColor("prompt"));
        selectionColor = Color.decode(themeManager.getThemeColor("selection"));
        borderColor = Color.decode(themeManager.getThemeColor("prompt"));
    }

    private Color getTabTextColor(boolean isSelected, boolean isHovered) {
        if (isSelected) {
            return foregroundColor;
        } else if (isHovered) {
            return new Color(foregroundColor.getRed(), foregroundColor.getGreen(), 
                           foregroundColor.getBlue(), 200);
        } else {
            return new Color(foregroundColor.getRed(), foregroundColor.getGreen(), 
                           foregroundColor.getBlue(), 100);
        }
    }

    public void updateTheme() {
        updateColors();
        sidebarPanel.setBackground(backgroundColor);
        historyList.setBackground(backgroundColor);
        historyList.setForeground(foregroundColor);
        favoritesList.setBackground(backgroundColor);
        favoritesList.setForeground(foregroundColor);
        searchField.setBackground(backgroundColor);
        searchField.setForeground(foregroundColor);
        searchField.setCaretColor(foregroundColor);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, borderColor));
        sidebarPanel.repaint();
    }

    public JPanel getSidebarPanel() {
        return sidebarPanel;
    }

    public void addToHistory(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        historyModel.insertElementAt(command, 0);

        if (historyModel.getSize() > MAX_HISTORY_SIZE) {
            historyModel.removeElementAt(historyModel.getSize() - 1);
        }
    }

    public void addToFavorites(String command) {
        if (command == null || command.trim().isEmpty() || favorites.contains(command)) {
            return;
        }

        favorites.add(command);
        favoritesModel.addElement(command);
        saveFavorites();
    }

    public void removeFromFavorites(String command) {
        if (favorites.remove(command)) {
            favoritesModel.removeElement(command);
            saveFavorites();
        }
    }

    public java.util.List<String> getHistory() {
        java.util.List<String> history = new ArrayList<>();
        for (int i = 0; i < historyModel.getSize(); i++) {
            history.add(historyModel.getElementAt(i));
        }
        return history;
    }

    public java.util.List<String> getFavorites() {
        return new ArrayList<>(favorites);
    }

    private void loadFavorites() {
        try {
            File file = new File(FAVORITES_FILE);
            if (file.exists()) {
                try (Reader reader = new FileReader(file)) {
                    Type type = new TypeToken<List<String>>(){}.getType();
                    List<String> loadedFavorites = gson.fromJson(reader, type);
                    if (loadedFavorites != null) {
                        loadedFavorites.forEach(this::addToFavorites);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке избранных команд: " + e.getMessage());
        }
    }

    private void saveFavorites() {
        try {
            File file = new File(FAVORITES_FILE);
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(new ArrayList<>(favorites), writer);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении избранных команд: " + e.getMessage());
        }
    }

    private String highlightText(String text, String searchQuery) {
        if (searchQuery.isEmpty()) return text;
        
        StringBuilder result = new StringBuilder("<html>");
        String lowerText = text.toLowerCase();
        int lastIndex = 0;
        int searchIndex;
        
        while ((searchIndex = lowerText.indexOf(searchQuery, lastIndex)) != -1) {
            result.append(text.substring(lastIndex, searchIndex))
                  .append("<font color='#")
                  .append(String.format("%06x", foregroundColor.getRGB() & 0xFFFFFF))
                  .append("'><b>")
                  .append(text.substring(searchIndex, searchIndex + searchQuery.length()))
                  .append("</b></font>");
            lastIndex = searchIndex + searchQuery.length();
        }
        
        result.append(text.substring(lastIndex));
        result.append("</html>");
        return result.toString();
    }

    private void clearSelections() {
        historyList.clearSelection();
        favoritesList.clearSelection();
    }
} 