package com.terminal.commands;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.Logger;
import com.terminal.sdk.TerminalPlugin;
import com.terminal.utils.OutputFormatter;
import com.terminal.utils.PluginManager;

public class PluginsCommand extends AbstractCommand {
    private static final Color WORKING_COLOR = new Color(63, 185, 80);
    private static final Color NOT_WORKING_COLOR = new Color(248, 81, 73);
    private static final Color BACKGROUND_COLOR = new Color(13, 17, 23);
    private static final Color TEXT_COLOR = new Color(201, 209, 217);
    private static final Color HEADER_COLOR = new Color(33, 38, 45);
    private static final Color BORDER_COLOR = new Color(48, 54, 61);
    private static final Font FONT = new Font("JetBrains Mono", Font.PLAIN, 12);
    private static final Font HEADER_FONT = new Font("JetBrains Mono", Font.BOLD, 12);

    public PluginsCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    public String getDescription() {
        return "Управление плагинами (используйте 'plugins <название плагина>' для подробной информации)";
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length > 0) {
                TerminalPlugin plugin = findPlugin(args[0]);
                if (plugin == null) {
                    OutputFormatter.printError(doc, style, "Плагин не найден: " + args[0]);
                    return;
                }
                showPluginDetails(args[0]);
            } else {
                OutputFormatter.printBoxedHeader(doc, style, "Загрузка списка плагинов");
                OutputFormatter.printBoxedLine(doc, style, "Открывается окно со списком плагинов...");
                OutputFormatter.printBoxedFooter(doc, style);
                showPluginsList();
            }
        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, "Ошибка при выполнении команды: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при выполнении команды: " + e.getMessage());
            }
        }
    }

    private void showPluginsList() {
        JFrame frame = createFrame("Установленные плагины", 800, 400);
        
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("Название");
        model.addColumn("Тип");
        model.addColumn("Версия");
        model.addColumn("Описание");
        model.addColumn("Команды");
        model.addColumn("Статус");

        JTable table = createTable(model);
        
        List<TerminalPlugin> plugins = PluginManager.getInstance().getLoadedPlugins();
        for (TerminalPlugin plugin : plugins) {
            try {
                String status = isPluginWorking(plugin) ? "Работает" : "Не работает";
                String type = plugin.isBasePlugin() ? "Базовый" : "Пользовательский";
                String commands = plugin.getCommands().keySet().stream()
                    .map(cmd -> cmd + " (" + plugin.getCommands().get(cmd).getDescription() + ")")
                    .collect(java.util.stream.Collectors.joining(", "));
                
                model.addRow(new Object[]{
                    plugin.getName(),
                    type,
                    plugin.getVersion(),
                    plugin.getDescription(),
                    commands,
                    status
                });
            } catch (Exception e) {
                model.addRow(new Object[]{
                    plugin.getName(),
                    "Неизвестно",
                    "Неизвестно",
                    "Ошибка загрузки",
                    "Нет данных",
                    "Не работает"
                });
            }
        }

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(70);
        columnModel.getColumn(3).setPreferredWidth(200);
        columnModel.getColumn(4).setPreferredWidth(200);
        columnModel.getColumn(5).setPreferredWidth(100);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        frame.add(contentPanel);
        
        frame.setVisible(true);
    }

    private void showPluginDetails(String pluginName) {
        TerminalPlugin plugin = findPlugin(pluginName);
        if (plugin == null) {
            Logger.error(getClass().getSimpleName(), "Плагин не найден: " + pluginName);
            return;
        }

        JFrame frame = createFrame("Информация о плагине: " + plugin.getName(), 800, 500);
        
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("Команда");
        model.addColumn("Подкоманды");
        model.addColumn("Описание");

        JTable table = createTable(model);
        
        plugin.getCommands().forEach((name, cmd) -> {
            List<String> subcommands = cmd.getSuggestions(new String[0]);
            String subcommandsStr = subcommands.isEmpty() ? "-" : 
                subcommands.stream().collect(java.util.stream.Collectors.joining(", "));
            
            model.addRow(new Object[]{
                name,
                subcommandsStr,
                cmd.getDescription()
            });
            
            Map<String, String> subcommandDescriptions = plugin.getSubcommandsDescriptions().get(name);
            if (subcommandDescriptions != null && !subcommandDescriptions.isEmpty()) {
                subcommandDescriptions.forEach((subcommand, description) -> {
                    model.addRow(new Object[]{
                        "  ↳ " + name + " " + subcommand,
                        "-",
                        description
                    });
                });
            }
        });

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(200);
        columnModel.getColumn(1).setPreferredWidth(200);
        columnModel.getColumn(2).setPreferredWidth(400);

        JPanel infoPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        infoPanel.setBackground(BACKGROUND_COLOR);
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        addInfoLabel(infoPanel, "Название: " + plugin.getName(), TEXT_COLOR);
        addInfoLabel(infoPanel, "Тип: " + (plugin.isBasePlugin() ? "Базовый" : "Пользовательский"), TEXT_COLOR);
        addInfoLabel(infoPanel, "Версия: " + plugin.getVersion(), TEXT_COLOR);
        addInfoLabel(infoPanel, "Описание: " + plugin.getDescription(), TEXT_COLOR);
        addInfoLabel(infoPanel, (isPluginWorking(plugin) ? 
            "Работает" : "Не работает"), isPluginWorking(plugin) ? WORKING_COLOR : NOT_WORKING_COLOR);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        contentPanel.add(infoPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        frame.add(contentPanel);
        
        frame.setVisible(true);
    }

    private JFrame createFrame(String title, int width, int height) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(width, height));
        frame.getContentPane().setBackground(BACKGROUND_COLOR);
        frame.pack();
        frame.setLocationRelativeTo(null);
        return frame;
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BACKGROUND_COLOR);
        table.setForeground(TEXT_COLOR);
        table.setGridColor(BORDER_COLOR);
        table.setFont(FONT);
        table.setRowHeight(25);
        table.setIntercellSpacing(new Dimension(5, 5));
        table.setShowGrid(true);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_COLOR);
        header.setForeground(TEXT_COLOR);
        header.setFont(HEADER_FONT);
        header.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(TEXT_COLOR);
                c.setBackground(isSelected ? HEADER_COLOR : BACKGROUND_COLOR);
                ((JLabel) c).setBorder(new EmptyBorder(0, 5, 0, 5));
                return c;
            }
        });

        return table;
    }

    private void addInfoLabel(JPanel panel, String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(FONT);
        panel.add(label);
    }

    private TerminalPlugin findPlugin(String name) {
        return PluginManager.getInstance().getLoadedPlugins().stream()
            .filter(p -> p.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    private boolean isPluginWorking(TerminalPlugin plugin) {
        try {
            plugin.getName();
            plugin.getVersion();
            plugin.getDescription();
            plugin.getCommands();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return PluginManager.getInstance().getLoadedPlugins().stream()
                .map(TerminalPlugin::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .collect(java.util.stream.Collectors.toList());
        }
        return Collections.emptyList();
    }

    private void handleUninstall(String pluginName) {
        TerminalPlugin plugin = findPlugin(pluginName);
        if (plugin == null) {
            Logger.error(getClass().getSimpleName(), "Плагин не найден: " + pluginName);
        }
    }
} 