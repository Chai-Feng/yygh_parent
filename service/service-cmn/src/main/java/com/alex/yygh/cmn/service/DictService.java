package com.alex.yygh.cmn.service;

import com.alex.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-11-20:27
 */
public interface DictService extends IService<Dict> {


    //根据数据id查询子数据列表
    List<Dict> findChildData(Long id);


    //导出数据字典
    void exportData(HttpServletResponse response);


    //导入数据字典
    void importDictData(MultipartFile file);

    /**
     * 根据上级id获取子节点数据列表 其实就是加缓存的findChildData
     *
     * @param parentId
    */
    List<Dict> findByParentId(Long parentId);



    /**
     * 根据上级编码与值获取数据字典名称
     * @param parentDictCode
     * @param value
     * @return
     */
    String getNameByParentDictCodeAndValue(String parentDictCode, String value);


    List<Dict> findByDictCode(String dictCode);


    //针对选择城市，只选当前
        Dict findById(Long id);
}
