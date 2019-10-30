package util;

/**
 * @author 12130
 * @date 2019/10/30
 * @time 8:48
 */
public class ByteUtil {
    private ByteUtil() {

    }


    public static long bytesToLong(byte[] bytes) {
        long res = 0;
        for (int i = 0; i < 8; i++) {
            res = res << 8;
            res |= (bytes[i] & 0xff);
        }
        return res;
    }

    public static int bytesToInteger(byte[] bytes) {
        int res = 0;
        for (int i = 0; i < 4; i++) {
            res = res << 8;
            res |= (bytes[i] & 0xff);
        }
        return res;
    }

}
