package com.alex.yygh.common.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.*;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-15-11:18
 */
public class ImageBase64Ytil {


    public static void main(String[] args) {
        String imageFile= "D:\\yygh_work\\xh.png";
        String imageString = getImageString(imageFile);
        System.out.println(imageString);
    }

    private static String getImageString(String imageFile) {
        InputStream is =null;


        try {

            byte[] data=null;
            is=new FileInputStream(new File(imageFile));
            data=new byte[is.available()];
            is.read(data);
            return new String(Base64.encodeBase64(data));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null!=is){
                    is.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
         }
    return "";
    }
}
