package net.cqooc.tool;

import java.io.File;
import java.io.IOException;

public class TestClassPathResource {
    public static void main(String[] args) throws IOException {

        System.out.println(new File("test").getAbsolutePath()+File.separator + "指定课程名.txt");
        ;
//        new ClassPathResource("").getFile().listFiles(new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String name) {
//                return !name.equals("指定课程名.txt");
//            }
//        });

    }
}
