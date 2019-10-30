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


    @Test
    public void testCompress() throws IOException {
        Compress compress = new Compress(new File("C:\\Users\\12130\\Desktop\\进阶7.mp4"),
                new FileOutputStream("C:\\Users\\12130\\Desktop\\1.txt"));
        compress.compressFile();
    }

    @Test
    public void testDeCompress() throws IOException {
        DeCompress deCompress = new DeCompress(new File("C:\\Users\\12130\\Desktop\\1.txt"), new File("C:\\Users\\12130\\Desktop"));
        deCompress.deCompressFile();
    }

}
