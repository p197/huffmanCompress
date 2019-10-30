package core;

/**
 * @author 12130
 * @date 2019/10/29
 * @time 22:15
 */

import java.io.IOException;
import java.io.OutputStream;

/**
 * 按bit写出的output。
 */
public class BitOutput {

    private OutputStream out;
    private int value;
    private int next = 7;

    public BitOutput(OutputStream out) {
        this.out = out;
    }

    public void writeBitOne() throws IOException {
        write_bit(1);
    }

    public void writeBitZero() throws IOException {
        write_bit(0);
    }

    private void write_bit(int bit) throws IOException {
        value = value | (bit << next);
        next--;
        if (next < 0) {
            out.write(value);
            value = 0;
            next = 7;
        }
    }

    public void writeBits(String bits) throws IOException {
        for (int i = 0; i < bits.length(); i++) {
            char v = bits.charAt(i);
            if (v == '0' || v == '1') {
                write_bit(v - '0');
                continue;
            }
            throw new RuntimeException();
        }
    }

    public void writeBits(int[] bits) throws IOException {
        for (int bit : bits) {
            if (bit == 0 || bit == 1) {
                write_bit(bit);
                continue;
            }
            throw new RuntimeException();
        }
    }

    public void writeBits(int[] bits, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            if (bits[i] == 0 || bits[i] == 1) {
                write_bit(bits[i]);
                continue;
            }
            throw new RuntimeException();
        }
    }

    public void close() throws IOException {
        if (next == 7) {
            out.close();
            return;
        }
        out.write(value);
        out.close();
    }
}