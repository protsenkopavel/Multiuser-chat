package net.protsenko.util;

import java.util.Random;

public class RandomUtil {
    private static Random random = new Random();

    public static int getRandomNumberUsingInts(int min, int max) {
        return random.ints(min, max)
                .findFirst()
                .getAsInt();
    }
}
