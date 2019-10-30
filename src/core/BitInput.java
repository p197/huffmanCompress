package core;

/**
 * @author 12130
 * @date 2019/10/29
 * @time 22:22
 */

import java.io.IOException;
import java.io.InputStream;

/**
 * 按bit读取的inputStream
 */
public class BitInput {

    private InputStream input;
    private int value;
    private int next = 7;

    public BitInput(InputStream input) {
        this.input = input;
    }

    /**
     * 读取一个bit,1,0,-1(文件结束)。
     */
    public int readBit() throws IOException {
        if (next == 7) {
            value = input.read();
            if (value == -1) {
                return -1;
            }
        }

        int result = (value & (1 << next)) >>> next;
        next--;

        if (next == -1) {
            next = 7;
        }

        return result;
    }

    /**
     * 读取size个bit。
     */
    public int[] readBits(int size) throws IOException {
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            int v = readBit();
            if (v == -1) {
                throw new RuntimeException();
            }
            result[i] = v;
        }
        return result;
    }
}