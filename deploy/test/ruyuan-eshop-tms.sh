#!/bin/bash

# 修改APP_NAME为云效上的应用名
APP_NAME=ruyuan-eshop-tms


PROG_NAME=$0
ACTION=$1
APP_START_TIMEOUT=60    # 等待应用启动的时间
APP_PORT=8009          # 应用端口
HEALTH_CHECK_URL=http://127.0.0.1:${APP_PORT}/actuator/health  # 应用健康检查URL
APP_HOME=../../${APP_NAME}-service # 工程目录
JAR_NAME=${APP_HOME}/target/${APP_NAME}*.jar # jar包的名字
JAVA_OUT=${APP_HOME}/logs/start.log  #应用的启动日志
publicIp=$(curl -X GET  'ifconfig.co')
echo $publicIp
# 创建出相关目录
mkdir -p ${APP_HOME}
mkdir -p ${APP_HOME}/logs
usage() {
    echo "Usage: $PROG_NAME {start|stop|restart}"
    exit 2
}

health_check() {
    exptime=0
    echo "checking ${HEALTH_CHECK_URL}"
    while true
        do
            status_code=`/usr/bin/curl -L -o /dev/null --connect-timeout 5 -s -w %{http_code}  ${HEALTH_CHECK_URL}`
            if [ "$?" != "0" ]; then
               echo -n -e "\rapplication not started"
            else
                echo "code is $status_code"
                if [ "$status_code" == "200" ];then
                    break
                fi
            fi
            sleep 1
            ((exptime++))

            echo -e "\rWait app to pass health check: $exptime..."

            if [ $exptime -gt ${APP_START_TIMEOUT} ]; then
                echo 'app start failed'
               exit 1
            fi
        done
    echo "check ${HEALTH_CHECK_URL} success"
}
start_application() {
    echo "starting java process"
    nohup java -server -Xms6g -Xmx6g -Xss256k -XX:MetaspaceSize=256m -XX:NewRatio=2 -XX:+PrintTenuringDistribution -XX:+PrintHeapAtGC -XX:+UseConcMarkSweepGC -XX:+UseParNewGC  -XX:+UseCMSCompactAtFullCollection  -XX:CMSFullGCsBeforeCompaction=5 -XX:CMSInitiatingOccupancyFraction=70 -XX:+CMSClassUnloadingEnabled -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/home/admin/application/gc-%t-%p.log -XX:+HeapDumpBeforeFullGC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/admin/application/ -XX:+PrintClassHistogramBeforeFullGC -XX:+PrintClassHistogramAfterFullGC -XX:ErrorFile=/home/admin/application/hs_err_pid%p.log -javaagent:../../agent/skywalking-agent.jar -Dskywalking.collector.backend_service=172.19.16.67:11800 -Dskywalking.agent.service_name=${APP_NAME} -Dskywalking.trace.ignore_path=/actuator/** -jar ${JAR_NAME} --spring.cloud.nacos.config.server-addr=172.19.16.47:8848 --spring.profiles.active=test > ${JAVA_OUT} 2>&1 &
    echo "started java process"
}

stop_application() {
   checkjavapid=`ps -ef | grep java | grep ${APP_NAME} | grep -v grep |grep -v 'deploy.sh'| awk '{print$2}'`
   
   if [[ ! $checkjavapid ]];then
      echo -e "\rno java process"
      return
   fi

   echo "stop java process"
   times=60
   for e in $(seq 60)
   do
        sleep 1
        COSTTIME=$(($times - $e ))
        checkjavapid=`ps -ef | grep java | grep ${APP_NAME} | grep -v grep |grep -v 'deploy.sh'| awk '{print$2}'`
        if [[ $checkjavapid ]];then
            kill -9 $checkjavapid
            echo -e  "\r        -- stopping java lasts `expr $COSTTIME` seconds."
        else
            echo -e "\rjava process has exited"
            break;
        fi
   done
   echo ""
}
start() {
    start_application
    health_check
}
stop() {
    stop_application
}
case "$ACTION" in
    start)
        start
    ;;
    stop)
        stop
    ;;
    restart)
        stop
        start
    ;;
    *)
        usage
    ;;
esac