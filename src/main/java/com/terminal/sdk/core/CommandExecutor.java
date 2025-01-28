package com.terminal.sdk.core;

import com.terminal.sdk.system.SystemFacade;

/**
 * Централизованный исполнитель команд терминала
 */
public class CommandExecutor {
    private static CommandExecutor instance;
    private final CommandRegistry registry;
    private static final String CLASS_NAME = CommandExecutor.class.getSimpleName();
    private final SystemFacade systemFacade;

    private CommandExecutor() {
        this.registry = new CommandRegistry();
        this.systemFacade = SystemFacade.getInstance();
    }

    public static CommandExecutor getInstance() {
        if (instance == null) {
            instance = new CommandExecutor();
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
} 