package net.cqooc.tool.util;

import sun.misc.BASE64Decoder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageUtil {

    public static boolean generateImage(String base64str, String savepath) {   //对字节数组字符串进行Base64解码并生成图片

        if (base64str == null) //图像数据为空
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] b = decoder.decodeBuffer(base64str);
            //  System.out.println("解码完成");
//            for(int i=0;i<b.length;++i)
//            {
//                if(b[i]<0)
//                {//调整异常数据
//                    b[i]+=256;
//                }

            // System.out.println("开始生成图片");
            //生成jpeg图片
            OutputStream out = new FileOutputStream(savepath);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}