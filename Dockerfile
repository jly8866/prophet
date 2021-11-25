FROM harbor.baijiahulian.com/basic/centos:jdk8
MAINTAINER op-notice op-notice@baijiahulian.com

copy ./target/prophet.jar /tmp/prophet.baijiahulian.com.jar
copy ./src/main/resources/application.properties /tmp/

EXPOSE 8080 22

CMD ["/bin/bash","/root/run.sh","prophet.baijiahulian.com"]
