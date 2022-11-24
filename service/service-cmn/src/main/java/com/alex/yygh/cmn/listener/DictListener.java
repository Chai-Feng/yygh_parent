package com.alex.yygh.cmn.listener;

import com.alex.yygh.cmn.mapper.DictMapper;
import com.alex.yygh.model.cmn.Dict;
import com.alex.yygh.vo.cmn.DictEeVo;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 *
 * 回调监听器
 *
 * @author: Alex
 * @Version:
 * @date 2022-11-12-15:22
 */


public class DictListener extends AnalysisEventListener<DictEeVo> {


    private DictMapper dictMapper;
    public DictListener( DictMapper dictMapper){
    this.dictMapper=dictMapper;
    }


    //一行一行的读 从第二行开始读，不读取表头
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //调用方法添加数据库
        Dict dict =new Dict();
        BeanUtils.copyProperties(dictEeVo,dict);
        dict.setIsDeleted(0);
        //System.out.println("设置删除位置");
        dictMapper.insert(dict);
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println("表头信息  invokeHeadMap"+headMap );
    }

    @Override
    public void invokeHead(Map<Integer, CellData> headMap, AnalysisContext context) {
        System.out.println("表头信息  invokeHead"+headMap );
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
