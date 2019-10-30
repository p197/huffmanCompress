package core;

import util.ByteUtil;
import util.StringUtil;

import java.io.*;
import java.util.PriorityQueue;

/**
 * @author 12130
 * @date 2019/10/30
 * @time 8:25
 */
public class DeCompress {

    private File srcFile;
    private File parentFile;
    private File generateFile;
    private String[] binary0To255;

    public DeCompress(File srcFile, File parentFile) {
        this.srcFile = srcFile;
        this.parentFile = parentFile;
        binary0To255 = StringUtil.generateBinary0To255();
    }

    public void deCompressFile() throws IOException {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(srcFile));
        int fileNameLength = bin.read();
        // 文件名
        byte[] fileName = new byte[fileNameLength];
        int cur = 0;
        int readLen;
        while (cur < fileNameLength && (readLen = bin.read(fileName, cur, fileNameLength - cur)) != -1) {
            cur += readLen;
        }
        if (cur < fileNameLength) {
            throw new IOException("file format error");
        }
        generateFile = new File(parentFile, new String(fileName));
        if (generateFile.exists()) {
            generateFile.delete();
        }
        generateFile.createNewFile();
        // huffman节点的个数
        int huffmanNodeSize = bin.read();
        // 空文件
        if (huffmanNodeSize == -1) {
            return;
        }
        if (huffmanNodeSize == 0) {
            huffmanNodeSize = 256;
        }
        // 当前字节
        int value;
        // 当前字节出现的次数
        byte[] valueCounts = new byte[4];
        // 构建huffman的结构
        for (int i = 0; i < huffmanNodeSize; i++) {
            if ((value = bin.read()) != -1) {
                cur = 0;
                while (cur < valueCounts.length && (readLen = bin.read(valueCounts, cur, valueCounts.length - cur)) != -1) {
                    cur += readLen;
                }
                int byteCount = ByteUtil.bytesToInteger(valueCounts);
                Node node = new Node((byte) value, byteCount);
                queue.add(node);
            } else {
                throw new EOFException("file format error");
            }
        }
        cur = 0;
        byte[] byteLength = new byte[8];
        while (cur < byteLength.length && (readLen = bin.read(byteLength, cur, byteLength.length - cur)) != -1) {
            cur += readLen;
        }
        // 新构建的文件的总的byte数量
        long totalByteCount = ByteUtil.bytesToLong(byteLength);
        Node root = HuffmanTree.getHuffmanTree(queue);
        buildFile(bin, new String(fileName), root, totalByteCount);
    }

    /**
     * 利用huffman树构建出原始文件
     *
     * @param bin
     * @param fileName
     * @param root
     * @param totalByteCount
     * @throws IOException
     */
    private void buildFile(BufferedInputStream bin, String fileName, Node root, long totalByteCount) throws IOException {
        BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(generateFile));
        StringBuilder stringBuilder = new StringBuilder();
        Node cur = root;
        long readByteCount = 0;
        int d;
        while (readByteCount < totalByteCount - 2 && (d = bin.read()) != -1) {
            readByteCount++;
            // 构造字符串
            String s = binary0To255[d];
            stringBuilder.append(s);
            cur = dfs(root, cur, stringBuilder, bout);
            stringBuilder.delete(0, stringBuilder.length());
        }
        d = bin.read();
        int fillZeroCount = bin.read();
        stringBuilder.append(Integer.toBinaryString(d | 256).substring(0, 8 - fillZeroCount));
        dfs(root, cur, stringBuilder, bout);
        bout.close();
        bin.close();
    }

    /**
     * 深度优先遍历huffman树，以此来还原原文件
     *
     * @param root
     * @param cur
     * @param stringBuilder
     * @param bout
     * @return
     * @throws IOException
     */
    private Node dfs(Node root, Node cur, StringBuilder stringBuilder, BufferedOutputStream bout) throws IOException {
        for (int i = 0; i < stringBuilder.length(); i++) {
            if (stringBuilder.charAt(i) == '0') {
                cur = cur.left;
                if (cur.left == null && cur.right == null) {
                    // 遍历到叶子节点了，叶子节点中保存着源文件对应的byte
                    bout.write(cur.value);
                    cur = root;
                }
            } else {
                cur = cur.right;
                if (cur.left == null && cur.right == null) {
                    bout.write(cur.value);
                    cur = root;
                }
            }
        }
        return cur;
    }

}
