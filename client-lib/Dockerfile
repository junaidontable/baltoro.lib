
FROM ubuntu:18.04

MAINTAINER junaid syed <junaid.syed@gmail.com>


RUN apt-get update \
    && apt-get install -y dnsutils iputils-ping curl nano zip unzip net-tools tcpdump lsof psmisc vim mysql-client \
    && apt-get autoremove -y \
    && apt-get clean

RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-11-jdk \
    && apt-get autoremove -y \
    && apt-get clean \
    && echo rm -rf /var/lib/dpkg/info/* /var/lib/apt/lists/* /var/cache/debconf/* /usr/share/doc/*

RUN export DEBIAN_FRONTEND="noninteractive" \
	&& apt-get update \
	&& apt-get install -y sudo wget apt-utils lsb-release gnupg telnet\
	&& wget -c https://repo.mysql.com/mysql-apt-config_0.8.14-1_all.deb \ 
	&& apt-get update \ 
	&& echo "mysql-apt-config mysql-apt-config/select-server select mysql-8.0" | sudo debconf-set-selections \ 
	&& echo "mysql-apt-config mysql-apt-config select OK" | sudo debconf-set-selections  \ 
	&& echo 4 | sudo dpkg -i mysql-apt-config_0.8.14-1_all.deb \
	&& sudo apt-get update && apt-get upgrade -y \
	&& echo "mysql-community-server mysql-server/default-auth-override select Use Legacy Authentication Method (Retain MySQL 5.x Compatibility)" | sudo debconf-set-selections \
	&& echo "mysql-community-server mysql-community-server/root-pass password baltoro6.DB2" | sudo debconf-set-selections \ 
	&& echo "mysql-community-server mysql-community-server/re-root-pass password baltoro6.DB2" | sudo debconf-set-selections  \ 
	&& echo 2 | sudo apt-get install -q -y mysql-server
	

#VOLUME /var/lib/mysql

EXPOSE 3306

RUN sudo echo "127.0.0.1 dbhost" >> /etc/hosts
RUN sudo mkdir /var/run/mysqld
RUN sudo chmod 777 /var/run/mysqld
RUN rm mysql-apt-config_0.8.14-1_all.deb
RUN sudo mv /var/lib/mysql /var/lib/mysql1



ENV conType=local
ENV embeddedDB=true
ENV JVM_OPTS=" -Xms10G -Xmx15G "
 
CMD ["bash", "-c", "./start.sh && bash "]
     
    
EXPOSE 8000

ARG JAR_FILE
ADD start.sh start.sh
ADD mysqld.cnf /etc/mysql/mysql.conf.d
ADD target/${JAR_FILE} /
