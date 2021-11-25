## Prophet是什么？ 
* 一个优秀的大数据查询平台，提供hive异步任务查询、LDAP用户、表级查询权限控制、历史查询任务与结果存储、邮件通知、excel下载等功能。
* 具有查询性能快、查询方便的特点

## 开发环境
* java 8
* springboot
* VUE + iview

## 准备工作
* 搭建hadoop集群、hive集群(兼容hive-server2-1.x和2.x版本，需要如下修改pom.xml)、metastore
* 搭建prophet会用到的mysql，推荐mysql 5.6及以上版本

## 安装步骤
* 1.安装jdk，强烈推荐使用jdk 1.8
    * 安装jdk
    * 修改PATH
* 2.下载文件（非编译安装）
    * 二进制文件下载地址：https://pan.baidu.com/s/1eSgx4fo
    * 或者下载ZIP包并解压，解压后会看到prophet_server、prophet_fe、prophet_sql三个目录
* 3.后端服务部署
    * prophet_sql目录：连接到prophet会用到的mysql里source prophet.sql这个文件将库表建好
    * prophet_server目录：后端服务，请部署在后端服务器适当目录下
        * 修改主配置文件：prophet_server/conf/application.properties
        * 启动服务：./bin/startup.sh
        * 检查日志：./logs/prophet.log
* 4.前端服务部署
    * prophet_fe目录：前端页面，请部署在nginx服务器或某个web服务器目录下例如/static/prophet_fe/，并参照下一步nginx配置
* 5.前端服务nginx配置
```javascript
upstream prophet{
    ip_hash;
    server 192.168.1.11:8090;
    #server 192.168.1.12:8090;
}

server {
    listen  80;
    server_name prophet.xxx.com;
    
    gzip    on;
    gzip_min_length 1k;
    gzip_proxied    expired no-cache no-store private auth;
    gzip_types      text/plain text/css application/xml application/json application/javascript application/xhtml+xml;
    
    client_max_body_size 300M;
    index index.php index.html index.htm;

    access_log /log/nginx/prophet.access.log main;
    error_log  /log/nginx/prophet.error.log;
 
    location ~ \.json$ {
        proxy_pass http://prophet;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        client_max_body_size 200m;
        client_body_buffer_size 128k;
        proxy_connect_timeout 86400;
        #因为后端hive任务执行时间较长，因此该项应该设置无限大，单位秒
        proxy_read_timeout 259200;    
        proxy_buffer_size 4k;
    }

    location / {
        root "/static/prophet_fe/";
    }
}
```
配置完重启nginx即可
* 6.配置域名解析prophet.xxx.com到该nginx所在ip
* 7.打开浏览器，访问http://prophet.xxx.com/，输入用户名和密码进行登录。
    * 如果配置了LDAP：则填写LDAP账号，prophet内置用户系统不生效。
    * 如果配置了prophet内置用户系统：则默认初始化管理账号为admin1，密码为admin1
* 8.开始使用吧！

## hive版本切换
* 如果想在hive 1.x和2.x之间切换，需要修改pom.xml里的hive-jdbc的version和具体hive版本一致即可：
```javascript
	<dependency>
      <groupId>org.apache.hive</groupId>
      <artifactId>hive-jdbc</artifactId>
      <version>1.1.0</version>
	....
```

## 系统截图
* 1.登录页面
    * ![image](https://github.com/jly8866/prophet/raw/master/screenshots/login.png)
* 2.主查询界面
    * ![image](https://github.com/jly8866/prophet/raw/master/screenshots/hive_query.png)
* 3.所有机密表展示
    * ![image](https://github.com/jly8866/prophet/raw/master/screenshots/all_secrets.png)
* 4.标记哪些表成为机密表
    * ![image](https://github.com/jly8866/prophet/raw/master/screenshots/config_secrets.png)
* 5.内置用户系统管理
    * ![image](https://github.com/jly8866/prophet/raw/master/screenshots/user_config.png)

## 性能调优
* prophet JVM能容纳的最大并发线程数NThreads = CPU核心数 * 总CPU利用率 * (1 + CPU等待时间/CPU处理时间)
    * 如果一个任务CPU处理时间为100ms，99ms是IO等待时间，系统8核心，CPU利用率50%，则NThreads = 8 * 50% * (1 + 99/100) = 7.96 ~ 8
    * 该指标可用于估算单进程prophet最大可运行的并发任务数
    * 如果指标不够则需要扩容

