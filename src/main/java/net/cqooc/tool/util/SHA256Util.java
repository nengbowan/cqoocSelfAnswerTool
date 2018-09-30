//package net.cqooc.tool.util;
//
//import org.apache.commons.codec.binary.Hex;
//
//import java.io.UnsupportedEncodingException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//
//public class SHA256Util {
//
//    public static String getSHA256Str(String str){
//        MessageDigest messageDigest;
//        String encdeStr = "";
//        try {
//            messageDigest = MessageDigest.getInstance("SHA-256");
//            byte[] hash = messageDigest.digest(str.getBytes("GBK"));
//            encdeStr = Hex.encodeHexString(hash);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return encdeStr;
//    }
//}
