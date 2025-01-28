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
    private final CommandChainManager chainManager;
    private static final String CLASS_NAME = CommandFacade.class.getSimpleName();
    private final SystemFacade systemFacade;

    private CommandFacade() {
        this.registry = new CommandRegistry();
        this.chainManager = CommandChainManager.getInstance();
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
    public void executeCommand(String commandName, String[] args, StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        try {
            Command command = registry.getCommand(commandName);
            if (command != null) {
                CommandContext context = new CommandContext(commandName, args, doc, style, pathHolder);
                context.setCommand(command);
                
                if (!chainManager.processCommand(context)) {
                    systemFacade.logError(CLASS_NAME, "Не удалось обработать команду: " + commandName);
                }
                
                if (!context.isHandled() && context.getResult() != null) {
                    systemFacade.logError(CLASS_NAME, context.getResult());
                }
            } else {
                systemFacade.logError(CLASS_NAME, "Команда не найдена: " + commandName);
            }
        } catch (Exception e) {
            systemFacade.logError(CLASS_NAME, "Ошибка при выполнении команды " + commandName + ": " + e.getMessage());
        }
    }

    /**
     * Получает информацию о команде
     */
    public CommandInfo getCommandInfo(String commandName) {
        Command command = registry.getCommand(commandName);
        return command != null ? command.getInfo() : null;
    }

    /**
     * Получает категорию команды
     */
    public CommandCategory getCommandCategory(String commandName) {
        Command command = registry.getCommand(commandName);
        return command != null ? CommandCategory.valueOf(command.getCategory()) : null;
    }

    /**
     * Добавляет новый обработчик команд в цепочку
     */
    public void addCommandHandler(CommandHandler handler) {
        chainManager.addHandler(handler);
        systemFacade.logInfo(CLASS_NAME, "Добавлен новый обработчик команд: " + handler.getClass().getSimpleName());
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