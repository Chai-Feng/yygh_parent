package com.alex.yygh.common.utils;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-25-21:51
 */
public class ProgressBarUtil {

    public static void main(String[] args) {
        progressBar();
    }
    public    static void progressBar(){
        char incomplete ='░';
        char complete ='█';

        int totoal =100;
        StringBuilder builder = new StringBuilder();
        Stream.generate(()->incomplete).limit(totoal).forEach(builder::append);
        builder.append(Thread.currentThread().getName());
        for(int i=0;i<totoal;i++){
            builder.replace(i,i+1,String.valueOf(complete));
            String progressBar ="\r"+builder;
            String percent =" "+(i+1)+"%";
            System.out.print(progressBar+percent);
            try {
                TimeUnit.MILLISECONDS.sleep(i*2L);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
