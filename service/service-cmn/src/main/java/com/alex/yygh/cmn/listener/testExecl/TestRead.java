package com.alex.yygh.cmn.listener.testExecl;

import com.alibaba.excel.EasyExcel;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-12-19:17
 */
public class TestRead {

    public static void main(String[] args) {
        //读取文件路径
        String fileName ="E:\\excel\\01.xlsx";
        //调用方法实现读操作
        EasyExcel.read(fileName, UserData.class,new ExcelListenner()).sheet().doRead();
    }
}
