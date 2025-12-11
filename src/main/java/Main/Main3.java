package Main;

import java.util.Scanner;

//Получение имен
//Из списка объектов Person получите список только имен.

public class Main3 {
    private static final String RESET = "\u001B[0m";

    // Using Unicode escape codes instead of directly typing emoji
    private static final String GRASS   = "\u001B[102m" + " \uD83C\uDF3F " + RESET; // 🌿
    private static final String DRAGON  = "\u001B[104m" + " \uD83D\uDC09 " + RESET; // 🐉
    private static final String CAT     = "\u001B[105m" + " \uD83D\uDC31 " + RESET; // 🐱
    private static final String FISH    = "\u001B[106m" + " \uD83D\uDC1F " + RESET; // 🐟
    private static final String TURTLE  = "\u001B[102m" + " \uD83D\uDC22 " + RESET; // 🐢
    private static final String OCTOPUS = "\u001B[45m"   + " \uD83D\uDC19 " + RESET; // 🐙

    public static void main(String[] args) {
        System.out.println(GRASS + DRAGON + CAT + FISH + TURTLE + OCTOPUS);
            System.out.println(GRASS + DRAGON + CAT);

    }
}
