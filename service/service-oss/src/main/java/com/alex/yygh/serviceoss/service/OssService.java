package com.alex.yygh.serviceoss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Title:
 * @Description:
 * 本质上也是进行增删改查，只是操作对象是OSS
 * @author: Alex
 * @Version:
 * @date 2022-11-21-13:15
 */
public interface OssService {


    //上文件到阿里云
    String upload(MultipartFile file);
}
