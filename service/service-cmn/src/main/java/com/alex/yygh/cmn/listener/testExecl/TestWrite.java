package com.alex.yygh.cmn.listener.testExecl;

import com.alibaba.excel.EasyExcel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-12-19:25
 */
public class TestWrite {
    public static void main(String[] args) {

        String file="E:\\excel\\01.xlsx";

        ArrayList<UserData> list = new ArrayList<>();

        for(int i=0;i<11;i++){
            UserData userData = new UserData();
            userData.setId(i);
            userData.setName("lucy-"+i);
            list.add(userData);
        }
        try {
            EasyExcel.write(file,UserData.class).sheet("我是你老爹").doWrite(list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
