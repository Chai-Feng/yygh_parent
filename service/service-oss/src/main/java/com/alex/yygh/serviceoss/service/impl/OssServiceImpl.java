package com.alex.yygh.serviceoss.service.impl;

import com.alex.yygh.serviceoss.service.OssService;
import com.alex.yygh.serviceoss.utils.ConstantOssPropertiesUtils;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-21-13:22
 */
@Service
public class OssServiceImpl implements OssService {
    @Override
    public String upload(MultipartFile file) {
        String endpoint = ConstantOssPropertiesUtils.ENDPOINT;
        String accessKeyId = ConstantOssPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantOssPropertiesUtils.ACCESS_KEY_SECRECT;
        String bucket = ConstantOssPropertiesUtils.BUCKET;


        // 创建OSSClient实例。

        // 创建PutObjectRequest对象。



        //上传流文件
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            //构建文件路径和文件名
            String fileOriginalFilename = file.getOriginalFilename(); //获取原文件名
            System.out.println("getOriginalFilename "+fileOriginalFilename);
            String[] split = fileOriginalFilename.split("\\.");
            String filename = split[0];
            String suffix= split[1];


            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            String fileName = filename + uuid+"."+suffix;
            //按照当前日期，创建文件夹，上传到创建文件夹里面
            //  2021/02/02/01.jpg
            String timeUrl = new DateTime().toString("yyyy/MM/dd");
            fileName = timeUrl + "/" + fileName;
            System.out.println("fileName "+fileName);

            //实现上传
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName, inputStream);

            ossClient.putObject(putObjectRequest);

            //关闭OSSClient
            ossClient.shutdown();

            //上传之后的文件路径
            String url= "https://"+bucket+"."+endpoint+"/"+fileName;

            return url;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;


    }


}
