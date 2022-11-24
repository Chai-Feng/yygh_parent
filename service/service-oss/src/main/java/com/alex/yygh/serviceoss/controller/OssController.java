package com.alex.yygh.serviceoss.controller;

import com.alex.yygh.common.result.Result;
import com.alex.yygh.serviceoss.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-21-13:49
 */

@RestController
@RequestMapping("api/oss/file")
public class OssController {


    @Autowired
    private  OssService ossService;

    //上传文件到阿里云Oss
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file){
        String url = ossService.upload(file);

        return Result.ok(url);

    }
}
