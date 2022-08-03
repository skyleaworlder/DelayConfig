package android.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * @hide
 */
public class RandomGenerator {
    private static Random sRandom;

    static {
        try {
            sRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            sRandom = new Random();
        }
    }

    /**
     * rand method don't throw any Exception
     * if low == high return low
     * if low > high return 0
     * else return [low, high), namely [low, high-1]
     * @param low
     * @param high
     * @return
     */
    public static Integer rand(Integer low, Integer high) {
        if (Objects.equals(low, high)) {
            return low;
        }
        if (low > high) {
            return 0;
        }
        return low + sRandom.nextInt(high - low);
    }
}
