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
    private String[] binary0To255;

    public DeCompress(File srcFile) {
        this.srcFile = srcFile;
        // 缓存每一种byte对应的8位二进制串
        binary0To255 = StringUtil.generateBinary0To255();
    }

    public void deCompress(File parentFile) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(srcFile));
        int fileType = bin.read();
        if (fileType == FileConstant.TYPE_FILE) {
            deCompressFile(bin, parentFile);
        } else if (fileType == FileConstant.TYPE_FOLDER) {
            deCompressFolder(bin, parentFile);
        } else {
            throw new RuntimeException();
        }

    }

    private void deCompressFolder(BufferedInputStream bin, File parentFile) throws IOException {
        int fileNameLength = bin.read();
        byte[] fileName = new byte[fileNameLength];
        int cur = 0;
        int readLen;
        while (cur < fileNameLength && (readLen = bin.read(fileName)) != -1) {
            cur += readLen;
        }
        File rootFile = new File(parentFile, new String(fileName));
        if (rootFile.exists()) {
            rootFile.delete();
        }
        rootFile.mkdir();
        // 获取当前文件夹内部子文件的个数
        int fileCount = bin.read();
        for (int i = 0; i < fileCount; i++) {
            int fileType = bin.read();
            if (fileType == FileConstant.TYPE_FILE) {
                deCompressFile(bin, rootFile);
            } else if (fileType == FileConstant.TYPE_FOLDER) {
                deCompressFolder(bin, rootFile);
            } else {
                throw new RuntimeException();
            }
        }
    }

    private void deCompressFile(BufferedInputStream bin, File parentFile) throws IOException {
        PriorityQueue<Node> queue = new PriorityQueue<>();
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
        File generateFile = new File(parentFile, new String(fileName));
        if (generateFile.exists()) {
            generateFile.delete();
        }
        generateFile.createNewFile();
        int isNull = bin.read();
        if (isNull == FileConstant.FILE_NULL) {
            return;
        }
        int huffmanNodeSize = bin.read();
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
        buildFile(generateFile, bin, root, totalByteCount);
    }

    /**
     * 利用huffman树构建出原始文件
     *
     * @param bin
     * @param root
     * @param totalByteCount
     * @throws IOException
     */
    private void buildFile(File generateFile, BufferedInputStream bin, Node root, long totalByteCount) throws IOException {
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
        stringBuilder.append(binary0To255[d].substring(0, 8 - fillZeroCount));
        dfs(root, cur, stringBuilder, bout);
        bout.close();
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
