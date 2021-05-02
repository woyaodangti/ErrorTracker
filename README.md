# ErrorTracker
## 功能简介
用于追踪SpringBoot系统中的错误信息，可以输出到console或者发送到钉钉
![image](https://user-images.githubusercontent.com/13415463/116814072-7ebb3780-ab89-11eb-99d6-4cd25aac44cd.png)
引入该插件，可以让错误告警自动发送到钉钉



## 使用方法
1.引入maven
```xml
  <dependency>
    <artifactId>ErrorTracker</artifactId>
    <groupId>com.mike.errortracker</groupId>
    <version>1.0-SNAPSHOT</version>
  </dependency>
```
2.使用相关注解
```java
@Service
@ErrorTrackerProxy
public class TestServiceImpl implements ITestService {

    @Override
    @ErrorTracker
    public void test(String test,Integer[] numbers) {
        System.out.println("test.......success");
        throw new RuntimeException("这个方法出错啦");
    }
}

```
3.目前支持的配置
```yml
tracker:
  dingTalk:
    enable: true
    webHook:  https://oapi.dingtalk.com/robot/send?access_token=e2d4b9311cae6ccd9742e776e74615da5d7cb0a3879a25bde4ec98c09a56ffc1
    title: 错误告警
```
说明：
*enable:是否发送钉钉消息
*title:消息的title
*webHook:消息的链接

## TODO
1. 发送到钉钉的错误告警格式通过配置自定义
2. 介入企业微信
3. 更灵活的错误处理机制（不限于发送消息），最好能使用者自己实现
