#!/bin/bash
# {{app_name}}{{test_tag}} daemon
# description: {{app_name}}{{test_tag}} daemon
# processname: {{app_name}}{{test_tag}}

DAEMON_PATH="{{root_path}}{{app_name}}-{{app_version}}/bin"
DAEMON_SCRIPT="{{root_path}}{{app_name}}-{{app_version}}/bin/{{app_name}}"
DAEMON="{{root_path}}{{app_name}}-{{app_version}}/bin/{{app_name}} {{jvm_opts}} {{extra_configs}}"
DAEMONOPTS=""

NAME={{app_name}}{{test_tag}}
DESC="{{app_description}}"
PIDFILE=/var/run/$NAME.pid
SCRIPTNAME=/etc/init.d/$NAME

case "$1" in
start)
        printf "%-50s" "Starting $NAME..."
        export API_ENV="production"
        cd $DAEMON_PATH
        chmod 755 $DAEMON_SCRIPT
        PID=`$DAEMON $DAEMONOPTS > /var/log/{{app_name}} 2>&1 & echo $!`
        #echo "Saving PID" $PID " to " $PIDFILE
        if [ -z $PID ]; then
            printf "%s\n" "Fail"
            exit 1
        else
            echo $PID > $PIDFILE
            printf "%s\n" "Ok"
        fi
;;
status)
        printf "%-50s" "Checking $NAME..."
        if [ -f $PIDFILE ]; then
            PID=`cat $PIDFILE`
            if [ -z "`ps axf | grep ${PID} | grep -v grep`" ]; then
                printf "%s\n" "Process dead but pidfile exists"
                exit 1
            else
                echo "Running"
            fi
        else
            printf "%s\n" "Service not running"
            exit 3
        fi
;;
stop)
        printf "%-50s" "Stopping $NAME"
            PID=`cat $PIDFILE`
            cd $DAEMON_PATH
        if [ -f $PIDFILE ]; then
            kill -HUP $PID
            printf "%s\n" "Ok"
            rm -f $PIDFILE
        else
            printf "%s\n" "pidfile not found"
        fi
;;

restart)
        $0 stop
        $0 start
;;

*)
        echo "Usage: $0 {status|start|stop|restart}"
        exit 1
esac

exit 0