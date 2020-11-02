package com.atguigu.gmall.common.test.stream;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamTest {
    public static void main(String[] args) {
    // map filter reduce
        List<User> users = Arrays.asList(
                new User("柳岩",20,true),
                new User("马蓉",18,true),
                new User("冰冰",24,true),
                new User("老王",55,false),
                new User("老贾",33,false)
        );
        //只有女性用户的users   collect 重新收集
        //List<User> userList = users.stream().filter(user -> user.getSex()).collect(Collectors.toList());
        //User::getSex方法引用
        List<User> userList = users.stream().filter(User::getSex).collect(Collectors.toList());
        System.out.println(userList);

        //把User对象转坏成person对象
        //map() 可以实现对象属性赋值,返回值是 stream 流
        List<Person> personList = users.stream().map(user -> {
            Person person = new Person();
            person.setUserName(user.getName());
            person.setAge(user.getAge());
            return person;
        }).collect(Collectors.toList());
        System.out.println(personList);

        //求年龄的和
        Integer integer = users.stream().map(User::getAge).reduce((a, b) -> a + b).get();
        System.out.println(integer);
    }
}
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
class  User{
    String name;
    Integer age;
    Boolean sex;
}
@Data
class Person {
    String userName;
    Integer age;
}
