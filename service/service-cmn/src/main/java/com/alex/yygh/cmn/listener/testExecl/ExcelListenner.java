package com.alex.yygh.cmn.listener.testExecl;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.Map;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-12-19:21
 */
public class ExcelListenner extends AnalysisEventListener<UserData> {
    @Override
    public void invoke(UserData userData, AnalysisContext analysisContext) {

        System.out.println("逐行读取 --"+userData.getName()+ "  " + userData.getId());

    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println("表头信息 "+headMap);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
