package test;

import core.Compress;
import core.DeCompress;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author 12130
 * @date 2019/10/29
 * @time 21:03
 */
public class T {
    /**
     * 压缩文件的结构
     * <p>
     * 每一个文件项开始一个byte表示是文件还是文件夹
     * 文件夹
     * 1.1个byte文件名长度
     * 2.n个byte记录文件名
     * 3.1个byte文件项个数
     * 4.各个文件项，又回到开始位置进行循环了
     * 文件
     * 1.1个byte文件名长度
     * 2.n个byte文件名
     * 3.1个byte表示文件是空文件还是不为空
     * 3.huffman树信息
     * 4.8个byte文件压缩之后的总的byte数
     * 5.文件内容
     * 6.填充0
     * 7.填充0的个数
     */


    @Test
    public void tesFolder() throws IOException {
        Compress compress = new Compress(new FileOutputStream("C:\\Users\\12130\\Desktop\\1.txt"));
        compress.compress(new File("C:\\Users\\12130\\Desktop\\layui-v2.5.5"));
        compress.close();
    }

    @Test
    public void testDeFolder() throws IOException {
        DeCompress deCompress = new DeCompress(new File("C:\\Users\\12130\\Desktop\\1.txt"));
        deCompress.deCompress(new File("C:\\Users\\12130\\Desktop"));
    }
}
