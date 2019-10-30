package util;

/**
 * @author 12130
 * @date 2019/10/30
 * @time 20:05
 */
public class StringUtil {
    private StringUtil() {
    }

    public static String[] generateBinary0To255() {
        String[] res = new String[256];
        for (int i = 0; i < 256; i++) {
            res[i] = Integer.toBinaryString(i | 256).substring(1, 9);
        }
        return res;
    }

}
