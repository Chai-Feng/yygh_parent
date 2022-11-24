package com.alex.yygh.cmn.controller;

import com.alex.yygh.cmn.service.DictService;
import com.alex.yygh.cmn.service.impl.DictServiceImpl;
import com.alex.yygh.common.result.Result;
import com.alex.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-11-20:53
 */

@Api(tags = "数据字典接口")
@Slf4j
@RestController
@RequestMapping("/admin/cmn/dict")
 //@CrossOrigin //允许跨域访问  Access-Control-Allow-Origin'
public class DictController {

    @Autowired
    private DictService dictService;

    //根据id 查询子数据列表
    @ApiOperation(value = "根据数据id查询子数据列表")
    @GetMapping("findChildData/{id}")
    public Result findChildData(@PathVariable Long id){

       // List<Dict> childData = dictService.findChildData(id);
        //使用缓存
        List<Dict> childData = dictService.findByParentId(id);
        log.info("查询结果{}",childData);
        return Result.ok(childData);
    }

    @ApiOperation(value="导出")
    @GetMapping("exportData")
    public void exportData(HttpServletResponse response){
        dictService.exportData(response);
    }



    @ApiOperation(value="导入")
    @PostMapping("importData")
    public Result importData(MultipartFile file){
        dictService.importDictData(file);

        return Result.ok();
    }

    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{parentDictCode}/{value}")
    public String getName(@ApiParam(name = "parentDictCode", value = "上级编码", required = true)
                              @PathVariable("parentDictCode") String parentDictCode,

                          @ApiParam(name = "value", value = "值", required = true)
                              @PathVariable("value") String value) {

        String dictname = dictService.getNameByParentDictCodeAndValue(parentDictCode, value);
        return dictname;
    }


    //父级
    @ApiOperation(value = "获取数据字典名称")
    @ApiImplicitParam(name = "value", value = "值", required = true, dataType = "Long", paramType = "path")
    @GetMapping(value = "/getName/{value}")
    public String getName(
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {
        return dictService.getNameByParentDictCodeAndValue("", value);
    }


    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findByDictCode/{dictCode}")
    public Result<List<Dict>> findByDictCode(
            @ApiParam(name = "dictCode", value = "节点编码", required = true)
            @PathVariable String dictCode) {
        List<Dict> list = dictService.findByDictCode(dictCode);
        return Result.ok(list);
    }

    @GetMapping(value = "/findById/{id}")
    public Result<Dict> getById(@PathVariable Long id){

        Dict dict = dictService.findById(id);
        System.out.println("返回内容为== "+dict);
        return  Result.ok(dict);

    }

}
