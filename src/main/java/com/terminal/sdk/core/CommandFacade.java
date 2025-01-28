package com.terminal.sdk.core;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.sdk.system.SystemFacade;

/**
 * Фасад для централизованного управления командами терминала.
 * Предоставляет унифицированный интерфейс для работы с командами разных типов.
 */
public class CommandFacade {
    private static CommandFacade instance;
    private final CommandRegistry registry;
    private static final String CLASS_NAME = CommandFacade.class.getSimpleName();
    private final SystemFacade systemFacade;

    private CommandFacade() {
        this.registry = new CommandRegistry();
        this.systemFacade = SystemFacade.getInstance();
    }

    public static CommandFacade getInstance() {
        if (instance == null) {
            instance = new CommandFacade();
        }
        return instance;
    }

    /**
     * Регистрирует новую команду
     */
    public void registerCommand(Command command) {
        registry.addCommand(command.getName(), command);
        systemFacade.logInfo(CLASS_NAME, "Зарегистрирована команда: " + command.getName());
    }

    /**
     * Регистрирует новую асинхронную команду
     */
    public void registerAsyncCommand(AsyncCommand command) {
        registry.addCommand(command.getName(), command);
        systemFacade.logInfo(CLASS_NAME, "Зарегистрирована асинхронная команда: " + command.getName());
    }

    /**
     * Выполняет команду с заданными параметрами
     */
    public void executeCommand(String commandName, String[] args) {
        try {
            Command command = registry.getCommand(commandName);
            if (command != null) {
                command.execute(args);
            } else {
                systemFacade.logError(CLASS_NAME, "Команда не найдена: " + commandName);
            }
        } catch (Exception e) {
            systemFacade.logError(CLASS_NAME, "Ошибка при выполнении команды " + commandName + ": " + e.getMessage());
        }
    }

    /**
     * Выполняет асинхронную команду
     */
    public void executeAsyncCommand(String commandName, String[] args) {
        try {
            Command command = registry.getCommand(commandName);
            if (command instanceof AsyncCommand) {
                ((AsyncCommand) command).executeAsync(args);
            } else {
                systemFacade.logError(CLASS_NAME, "Асинхронная команда не найдена: " + commandName);
            }
        } catch (Exception e) {
            systemFacade.logError(CLASS_NAME, "Ошибка при выполнении асинхронной команды " + commandName + ": " + e.getMessage());
        }
    }

    /**
     * Получает категорию команды
     */
    public CommandCategory getCommandCategory(String commandName) {
        Command command = registry.getCommand(commandName);
        return command != null ? CommandCategory.valueOf(command.getCategory()) : null;
    }

    public Command createCommand(String name, String description, String category,
                               StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        return new SimpleCommand(name, description, category, doc, style, pathHolder);
    }

    public AsyncCommand createAsyncCommand(String name, String description, String category,
                                         StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        return new SimpleAsyncCommand(name, description, category, doc, style, pathHolder);
    }

    public Command createCommandWithExtraStyle(String name, String description, String category,
                                             StyledDocument doc, Style style, Style extraStyle,
                                             CurrentPathHolder pathHolder) {
        Command command = createCommand(name, description, category, doc, style, pathHolder);
        if (extraStyle != null && doc != null) {
            doc.addStyle(name + "_extra", extraStyle);
        }
        return command;
    }

    public Command createCommandWithParams(String name, String description, String category,
                                         StyledDocument doc, Style style, CurrentPathHolder pathHolder,
                                         Object... params) {
        return createCommand(name, description, category, doc, style, pathHolder);
    }

} 