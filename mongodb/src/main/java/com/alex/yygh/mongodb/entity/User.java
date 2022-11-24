package com.alex.yygh.mongodb.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-13-20:54
 */

@Data
@Document("User")  //指定了User集合 collection
public class User {

    @Id  //表示mongodb中 id 的生成策略
    private String  id;
    private String name;
    private Integer age;
    private String email;
    private String createDate;
}
