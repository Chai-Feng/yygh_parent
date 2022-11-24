package com.alex.yygh.cmn.service.impl;

import com.alex.yygh.cmn.listener.DictListener;
import com.alex.yygh.cmn.mapper.DictMapper;
import com.alex.yygh.cmn.service.DictService;
import com.alex.yygh.model.cmn.Dict;
import com.alex.yygh.vo.cmn.DictEeVo;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.excel.write.ExcelBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-11-20:30
 */

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    //注意：ServiceImpl里已经自动装配了 protected M baseMapper; 此处 M为 DictMapper
    @Override
    public List<Dict> findChildData(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id); //找到一类
        List<Dict> dicts = baseMapper.selectList(wrapper);

        for(Dict dict:dicts){
            Long dictId = dict.getId();
            boolean hasChild = this.isHasChildren(dictId);
            dict.setHasChildren(hasChild);
        }
        return dicts;
    }




    //判断id下面是否有子节点

    private  boolean isHasChildren( Long id){
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id); //找到一类
        Integer count = baseMapper.selectCount(wrapper);

        return count>0;

    }

//写操作，从excel中导入数据
    @Override
    public void exportData(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");

            List<Dict> dictList = baseMapper.selectList(null);
            List<DictEeVo> dictVoList = new ArrayList<>(dictList.size());
            for(Dict dict : dictList) {
                DictEeVo dictVo = new DictEeVo();
                BeanUtils.copyProperties(dict,dictVo,DictEeVo.class);
                //BeanUtils.copyBean(dict, dictVo, DictEeVo.class);
                dictVoList.add(dictVo);

            }

            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 导入
     * allEntries = true: 方法调用后清空所有缓存
     *
     * 导入新数据后，原有的缓存可以清空了
     * @param file
     */
    @CacheEvict(value = "dict", allEntries=true)
    @Override
    public void importDictData(MultipartFile file) {


        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class,new DictListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    /**
     * 缓存应用
     * 根据上级id 获取子节点数据
     */

    @Cacheable(value ="dict" ,keyGenerator="keyGenerator")
    @Override
    public List<Dict> findByParentId(Long parentId) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",parentId);
        List<Dict> dicts = baseMapper.selectList(wrapper);

        //求解
        dicts.stream().forEach(dict -> {
            boolean isHaschildren = this.isHasChildren(dict.getId());
            dict.setHasChildren(isHaschildren);
        });
        return dicts;
    }

    //BigInt 默认自动转换为String？？？
    @Cacheable(value = "dict",keyGenerator = "keyGenerator") //开启redis缓存
    @Override
    public String getNameByParentDictCodeAndValue(String parentDictCode, String value) {

        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("value",value);
        //只有1级元素(parent 省，医院等级，证件类型 。。才存在编码)
        if(StringUtils.isEmpty(parentDictCode)){
            //dict_code为null 说明 此元素是child元素
            Dict dict = baseMapper.selectOne(wrapper);
            if(null!=dict){
                return dict.getName();
            }
        }else {
            //此元素为1级父元素( 省，医院等级，证件类型。。)
            Dict parentDict = this.getByDictsCode(parentDictCode);

            if (null == parentDict) return "";

            //parent_id 和 value相同 保证唯一
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("parent_id", parentDict.getId()).eq("value", value));
            if (null != dict) {
                return dict.getName();
            }
        }
            return "";

    }


    //查询的结果是子dict
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict codeDict = this.getByDictsCode(dictCode);
        if(dictCode==null)return null;
        System.out.println("findByDictCode 查询到的子数据  = "+this.findChildData(codeDict.getId()));
        return this.findChildData(codeDict.getId());

    }



    //针对选择市而提供的api
    public Dict findById(Long id) {
        Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("id", id));
        return dict;
    }

    private Dict getByDictsCode(String parentDictCode){

        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code",parentDictCode);
        Dict dict = baseMapper.selectOne(wrapper);
        return dict;
    }


}
