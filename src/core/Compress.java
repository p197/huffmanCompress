package core;

import util.DfsUtil;

import java.io.*;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * @author 12130
 * @date 2019/10/29
 * @time 20:45
 */
public class Compress implements Closeable {
    private File srcFile;
    private BufferedOutputStream outputStream;
    private HashMap<Byte, Integer> map = new HashMap<>();
    private int[] bytesCount = new int[256];
    private String[] binaryString = new String[256];
    private long bytesLength;

    public Compress(File srcFile, OutputStream outputStream) {
        this.srcFile = srcFile;
        this.outputStream = new BufferedOutputStream(outputStream);
    }


    public void compressFile() throws IOException {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(srcFile));
        if (srcFile.length() != 0) {
            byte[] buffer = new byte[8192];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                for (int i = 0; i < len; i++) {
                    bytesCount[(buffer[i] & 0xff)]++;
                }
            }
            for (int i = 0; i < bytesCount.length; i++) {
                if (bytesCount[i] > 0) {
                    Node node = new Node((byte) i, bytesCount[i]);
                    queue.add(node);
                }
            }
            Node root = HuffmanTree.getHuffmanTree(queue);
            DfsUtil.dfs(root, new StringBuilder(), binaryString);
            writeCompressInfo();
            writeContent();
        } else {
            writeName();
            inputStream.close();
        }
    }

    /**
     * 写入被压缩文件的文件名
     *
     * @throws IOException
     */
    private void writeName() throws IOException {
        String name = srcFile.getName();
        // 文件名的长度，一个byte来保存
        outputStream.write(name.getBytes().length);
        outputStream.write(name.getBytes());
    }


    private void writeCompressInfo() throws IOException {
        // 写入文件名
        writeName();
        // 写入构建的哈夫曼树的信息
        writeHuffmanInfo();
        // 写入重新构建之后的文件的byte数，读的时候就只读这么多的byte
        writeByteLength();
        // 之前写入的是byte，后面写入的是bit，需要先将byte进行刷入
    }

    private void writeContent() throws IOException {
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(srcFile), 8129 * 1024);
        BitOutput bitOutput = new BitOutput(outputStream);
        int d;
        long writeBitSize = 0;
        while ((d = inputStream.read()) != -1) {
            String str = binaryString[d];
            writeBitSize += str.length();
            bitOutput.writeBits(str);
        }
        writeBitSize = 8 - writeBitSize % 8;
        for (int i = 0; i < writeBitSize; i++) {
            bitOutput.writeBitZero();
        }
        String s = Integer.toBinaryString((int) (writeBitSize | 256)).substring(1, 9);
        // 写入填充了多少个0
        for (int i = 0; i < s.length(); i++) {
            bitOutput.writeBits(s);
        }
        bitOutput.close();
    }

    /**
     * 写入哈夫曼树的信息
     */
    private void writeHuffmanInfo() throws IOException {
        // huffman节点有几个
        int size = 0;
        for (int i = 0; i < bytesCount.length; i++) {
            if (bytesCount[i] > 0) {
                size++;
            }
        }
        outputStream.write(size);
        for (int i = 0; i < bytesCount.length; i++) {
            // 写入每个节点出现的次数
            if (bytesCount[i] > 0) {
                outputStream.write(i);
                outputStream.write((byte) (bytesCount[i] >> 24));
                outputStream.write((byte) (bytesCount[i] >> 16));
                outputStream.write((byte) (bytesCount[i] >> 8));
                outputStream.write((byte) bytesCount[i]);
            }
        }
    }

    private void writeByteLength() throws IOException {
        bytesLength = getByteLength();
        outputStream.write((byte) (bytesLength >> 56));
        outputStream.write((byte) (bytesLength >> 48));
        outputStream.write((byte) (bytesLength >> 40));
        outputStream.write((byte) (bytesLength >> 32));
        outputStream.write((byte) (bytesLength >> 24));
        outputStream.write((byte) (bytesLength >> 16));
        outputStream.write((byte) (bytesLength >> 8));
        outputStream.write((byte) bytesLength);
    }

    private long getByteLength() {
        long bytesLength = 0;
        for (int i = 0; i < bytesCount.length; i++) {
            if (bytesCount[i] > 0) {
                // 字节出现次数乘以字节对应的Huffman编码长度
                bytesLength += bytesCount[i] * binaryString[i].length();
            }
        }
        // 字节数
        bytesLength /= 8;
        // 最后不足八位的编码一字节，以及补零个数的一字节
        bytesLength += 2;
        return bytesLength;
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
