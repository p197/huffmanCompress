package core;

import util.DfsUtil;

import java.io.*;
import java.util.PriorityQueue;

/**
 * @author 12130
 * @date 2019/10/30
 * @time 22:45
 * <p>
 * 文件夹压缩器
 */
public class Compress implements Closeable {
    private BufferedOutputStream outputStream;

    public Compress(OutputStream outputStream) {
        this.outputStream = new BufferedOutputStream(outputStream);
    }

    public void compress(File srcFile) throws IOException {
        if (srcFile.isFile()) {
            fileCompress(srcFile);
        } else if (srcFile.isDirectory()) {
            // 头一个byte标识压缩文件的类型
            outputStream.write(FileConstant.TYPE_FOLDER);
            // 写入文件名
            writeName(srcFile, outputStream);
            // 写入文件个数
            File[] files = srcFile.listFiles();
            outputStream.write(files.length);
            for (int i = 0; i < files.length; i++) {
                compress(files[i]);
            }
        }

    }


    private void fileCompress(File file) throws IOException {
        FileCompress compress = new FileCompress(file, outputStream);
        compress.compressFile();
    }


    private void writeName(File file, BufferedOutputStream bout) throws IOException {
        byte[] bytes = file.getName().getBytes();
        bout.write(bytes.length);
        bout.write(bytes);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    /**
     * 文件压缩内部类
     */
    private class FileCompress {
        private File srcFile;
        private BufferedOutputStream outputStream;
        private int[] bytesCount = new int[256];
        /**
         * 每一个byte对应的二进制编码
         */
        private String[] binaryString = new String[256];

        FileCompress(File srcFile, BufferedOutputStream outputStream) {
            this.srcFile = srcFile;
            this.outputStream = outputStream;
        }


        void compressFile() throws IOException {
            PriorityQueue<Node> queue = new PriorityQueue<>();
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(srcFile));
            if (srcFile.length() != 0) {
                byte[] buffer = new byte[8192];
                int len = 0;
                // 计算每一种byte出现的次数
                while ((len = inputStream.read(buffer)) != -1) {
                    for (int i = 0; i < len; i++) {
                        bytesCount[(buffer[i] & 0xff)]++;
                    }
                }
                // 针对不同的出现次数构建优先队列
                for (int i = 0; i < bytesCount.length; i++) {
                    if (bytesCount[i] > 0) {
                        Node node = new Node((byte) i, bytesCount[i]);
                        queue.add(node);
                    }
                }
                // 将优先队列转换为huffman树
                Node root = HuffmanTree.getHuffmanTree(queue);
                DfsUtil.dfs(root, new StringBuilder(), binaryString);
                // 当前被压缩的是一个文件类型
                outputStream.write(FileConstant.TYPE_FILE);
                writeCompressInfo();
                writeContent();
            } else {
                outputStream.write(FileConstant.TYPE_FILE);
                writeName();
                writeFileIsNull();
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

        private void writeFileIsNull() throws IOException {
            if (srcFile.length() > 0) {
                outputStream.write(FileConstant.FILE_NOT_NULL);
            } else {
                outputStream.write(FileConstant.FILE_NULL);
            }
        }


        private void writeCompressInfo() throws IOException {
            // 写入文件名
            writeName();
            writeFileIsNull();
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
            bitOutput.writeBits(s);
            bitOutput.flush();
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
            long bytesLength = getByteLength();
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

    }
}
