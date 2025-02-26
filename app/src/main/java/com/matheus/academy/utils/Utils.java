package com.example.academy.utils;

public class Utils {
    public static String getLetter(int number) {
        StringBuilder stringBuilder = new StringBuilder();
        while (number >= 0) {
            stringBuilder.insert(0, (char) ('A' + (number % 26)));
            number = (number / 26) - 1;
        }
        return stringBuilder.toString();
    }
}
