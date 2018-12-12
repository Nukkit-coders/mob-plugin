package nukkitcoders.mobplugin.utils;

import cn.nukkit.math.Vector3;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz (kniffo80)</a>
 */
public class Utils {

    private static final Random random = new Random(System.currentTimeMillis());

    public static int rand(int min, int max) {
        if (min == max) {
            return max;
        }
        return ThreadLocalRandom.current().nextInt(min,max);
    }
    public static double rand(double min, double max) {
        if (min == max) {
            return max;
        }
        return ThreadLocalRandom.current().nextDouble(min,max);
    }

    public static float rand(float min, float max) {
        if (min == max) {
            return max;
        }
        return min + (ThreadLocalRandom.current().nextFloat() * (max));
    }

    public static boolean rand() {
        return random.nextBoolean();
    }

    public static final int ACCORDING_X_OBTAIN_Y = 0;
    public static final int ACCORDING_Y_OBTAIN_X = 1;

    public static double calLinearFunction(Vector3 pos1, Vector3 pos2, double element, int type) {
        if (pos1.getFloorY() != pos2.getFloorY()) return Double.MAX_VALUE;
        if (pos1.getX() == pos2.getX()) {
            if (type == ACCORDING_Y_OBTAIN_X) return pos1.getX();
            else return Double.MAX_VALUE;
        } else if (pos1.getZ() == pos2.getZ()) {
            if (type == ACCORDING_X_OBTAIN_Y) return pos1.getZ();
            else return Double.MAX_VALUE;
        } else {
            if (type == ACCORDING_X_OBTAIN_Y) {
                return (element-pos1.getX()) * (pos1.getZ()-pos2.getZ()) / (pos1.getX()-pos2.getX()) + pos1.getZ();
            } else {
                return (element-pos1.getZ()) * (pos1.getX()-pos2.getX()) / (pos1.getZ()-pos2.getZ()) + pos1.getX();
            }
        }
    }
}
