package com.alex.yygh.mongodb;

import com.alex.yygh.mongodb.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@SpringBootTest
class MongodbApplicationTests {


    @Autowired
    private MongoTemplate mongoTemplate;

    //添加
    @Test
    void createUser() {
        User user = new User();
        user.setName("qqq");
        user.setAge(33);
        user.setEmail("66666@gmail.com");
        mongoTemplate.insert(user,"teacher"); //不写collection 默认collection 为类名User
        System.out.println(user);
    }
    //查询所有
    @Test
    public void findUser() {
        List<User> userList = mongoTemplate.findAll(User.class);
        List<User> teacher = mongoTemplate.findAll(User.class, "teacher");
        System.out.println(userList);
        System.out.println(teacher);
    }

    //根据id查询
    @Test
    public void getById() {
        User user = mongoTemplate.findById("6370ea8fcf951155cace2741",User.class);
        System.out.println(user);
    }


    //条件查询
    @Test
    public  void findUserList(){
        Query query = new Query(Criteria.where("name").is("www").and("age").is(30));
        List<User> teacher = mongoTemplate.find(query, User.class, "teacher");
        System.out.println(teacher);
    }

    //模糊查询
    @Test
    public void findUsersLikeName(){
        String name="w";
        String regex=String.format("%s%s%s","^.*",name,".*$");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE); //启用不区分大小写策略
        Query query = new Query(Criteria.where("name").regex(pattern));
        List<User> userList = mongoTemplate.find(query, User.class,"teacher");
        System.out.println(userList);

    }



    //分页查询
    @Test
    public void findUsersPage() {
        int pageNo=1;
        int pageSize=10;

        String name="q";
        String regex=String.format("%s%s%s","^.*",name,".*$");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE); //启用不区分大小写策略
        Query query = new Query(Criteria.where("name").regex(pattern));

        List<User> users = mongoTemplate.find(query.skip((pageNo - 1) * pageSize).limit(pageSize), User.class);
        long count = mongoTemplate.count(query, User.class);
        Map<String, Object> pageMap = new HashMap<>();
        pageMap.put("list", users);
        pageMap.put("totalCount",count);
        System.out.println(pageMap);

    }



}
