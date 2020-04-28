



echo "checking local mysql server "
#export dbhost="$(getent hosts dbhost | awk '{ print $1 }')"
#dbhostvar="$(getent hosts dbhost | awk '{ print $1 }')"
export dbhost="127.0.0.1"
dbhostvar="127.0.0.1"
lhostvar="127.0.0.1"

echo "embeddedDB ==  ${embeddedDB}    >>>>>>>>>> dbhost = $dbhostvar"

if [ "${embeddedDB}" = "true" ]; then

    sudo echo "127.0.0.1 dbhost" >> /etc/hosts
    dbhostvar="$(getent hosts dbhost | awk '{ print $1 }')"
    echo "db-host =>>>>>>> $dbhostvar "    

    if [ -d "/var/lib/mysql" ]; then
        echo "1 >>>> mysql data folder exists "
    else
        echo "mount volumn with e.g docker -v /data/mysql:/var/lib/mysql"
        exit 0
    fi

    if [ -f "/var/lib/mysql/auto.cnf" ]; then
        echo "mysql db exists ..."
        #sudo service mysql start
        sudo mysqld --user=mysql & 
    else
        echo "init mysql db  ..."
        #mkdir -p /root/mck/mysql
        #sudo chown -R mysql:mysql /root/mck/mysql
        sudo mysqld --initialize-insecure --user=mysql
        echo "starting mysql db  ..."
        #sudo service mysql start
        sudo mysqld --user=mysql & 
        sleep 3
        sudo mysql -e "CREATE USER 'baltoro'@'localhost' IDENTIFIED BY 'baltoro6.DB2'";
        sudo mysql -e "GRANT ALL ON *.* TO 'baltoro'@'localhost'";
        sudo mysql -e "CREATE USER 'baltoro'@'%' IDENTIFIED BY 'baltoro6.DB2'";
        sudo mysql -e "GRANT ALL ON *.* TO 'baltoro'@'%'";
        sudo mysql -e "flush privileges"
        echo "init mysql db  ... done"
    fi
else
    echo "local database not needed"
fi


sleep 2

cd $HOME

mkdir -p $HOME/logs

JAVA_CMD="-DdbHost=${dbhost} -DapiKey=${apiKey} -DauthCode=${authCode} -Dschema=${schema} -Ddebug=${debug} ${JVM_OPTS} -cp /root/${jar}:/baltoro-jar-with-dependencies.jar io.baltoro.Baltoro"

echo $JAVA_CMD
nohup java  $JAVA_CMD > ~/logs/outpost.log &


sleep 2

tail -f ~/logs/outpost.log &





