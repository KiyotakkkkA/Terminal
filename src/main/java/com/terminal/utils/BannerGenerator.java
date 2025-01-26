package com.terminal.utils;

/**
 * Утилита для генерации баннеров в консоли с автоматическим выравниванием.
 */
public class BannerGenerator {
    private static final char TOP_LEFT = '╔';
    private static final char TOP_RIGHT = '╗';
    private static final char BOTTOM_LEFT = '╚';
    private static final char BOTTOM_RIGHT = '╝';
    private static final char HORIZONTAL = '═';
    private static final char VERTICAL = '║';
    
    /**
     * Генерирует баннер с автоматическим выравниванием по самой длинной строке.
     * @param lines строки для отображения в баннере
     * @return отформатированный баннер
     */
    public static String generate(String... lines) {
        int maxLength = 0;
        for (String line : lines) {
            maxLength = Math.max(maxLength, line.length());
        }
        
        maxLength += 5;
        
        StringBuilder banner = new StringBuilder();
        
        banner.append(TOP_LEFT);
        for (int i = 0; i < maxLength - 1; i++) {
            banner.append(HORIZONTAL);
        }
        banner.append(TOP_RIGHT).append('\n');
        
        for (String line : lines) {
            banner.append(VERTICAL).append(' ').append(line);
            for (int i = 0; i < maxLength - line.length() - 2; i++) {
                banner.append(' ');
            }
            banner.append(VERTICAL).append('\n');
        }
        
        banner.append(BOTTOM_LEFT);
        for (int i = 0; i < maxLength - 1; i++) {
            banner.append(HORIZONTAL);
        }
        banner.append(BOTTOM_RIGHT).append('\n');
        
        return banner.toString();
    }
} 