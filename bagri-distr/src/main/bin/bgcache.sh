#!/bin/bash
#
# $Id: bgcache.sh 195 2014-12-12 13:32:00+04 denis_sukhoroslov $
#
# vim:ft=sh:
#

appname="`basename $0`"
appname=${appname/\.sh/}
apphome="`cd \`dirname $0\`/.. && pwd && cd - >/dev/null`"

case "`uname -s`" in
        CYGWIN_NT-5.*)
                file_separator="\\"
                path_separator=';'
                java_apphome="`cygpath -ws ${apphome}`"
                ;;
        *)
                file_separator='/'
                path_separator=':'
                java_apphome="${apphome}"
                ;;
esac

nodeName=first
if [ $# -gt 1 ]
then
   nodeName=$2
fi

nodeNum=0
if [ $# -gt 2 ]
then
   nodeNum=$3
fi

libdir="${java_apphome}${file_separator}lib"

CLASSPATH="${java_apphome}${file_separator}config${file_separator}*"
CLASSPATH="${CLASSPATH}${path_separator}${libdir}${file_separator}*"

. "${apphome}/bin/${appname}.conf"

export CLASSPATH

logdir="${apphome}/logs/${nodeName}"
rundir="${apphome}/run"

mkdir -p "${logdir}"
mkdir -p "${logdir}/gc"
mkdir -p "${rundir}"

stdoutfile="${logdir}/${appname}_${HOSTNAME}_${nodeName}_${nodeNum}.out"
stderrfile="${logdir}/${appname}_${HOSTNAME}_${nodeName}_${nodeNum}.err"
pidfile="${rundir}/${appname}_${HOSTNAME}_${nodeName}_${nodeNum}.pid"

#
# Under Cygwin, ps itself won't return a nonzero code if PID is not found.
#
pid_exists() {
        if [ $# -ne 1 ]
        then
                echo "${BASH_SOURCE[0]}:${BASH_LINENO[0]}: ${FUNCNAME[0]}($@): exactly one argument required."
                return 1
        fi

        ps -p $1 | grep $1 >/dev/null
        return $?
}

start() {
        if status >/dev/null
        then
                echo "${appname}-${HOSTNAME}-${nodeName}-${nodeNum} is already running"
                return 1
        fi

        JAVA_OPTS="-server -showversion ${JAVA_OPTS}"
#        ${JAVA_HOME}/bin/java ${JAVA_OPTS} "${main}" </dev/null >>"${stdoutfile}" 2>>"${stderrfile}" &
#        java ${JAVA_OPTS} "${main}" </dev/null >>"${stdoutfile}" 2>>"${stderrfile}" &
        java ${JAVA_OPTS} "${main}" &
#        echo $! >"${pidfile}"

#        status
#        return $?
}

kill_node() {
        red='\E[31;40;1m'
        green='\E[32;40;1m'
        default='\E[0m'
        if [ $# -ne 1 ]
        then
                echo "${BASH_SOURCE[0]}:${BASH_LINENO[0]}: ${FUNCNAME[0]}($@): exactly one argument required."
                return 1
        fi
        myPidfile=$1
        pid="`cat ${myPidfile}`"
        if pid_exists "${pid}"
        then
                echo -e "${red}stopping process (pid ${pid})...${default}"
                kill -15 "${pid}"
                sleep 5
                kill -9 "${pid}" 2>/dev/null
                rm -f "${myPidfile}"
        fi
}

kill_cluster() {

        for nodePidfile in `find ${rundir} -type f -name '*\.pid'`
        do
                kill_node "${nodePidfile}"
        done
}

stop() {
        red='\E[31;40;1m'
        green='\E[32;40;1m'
        default='\E[0m'
        if [ -z $1 ]
        then
          echo -e "${red}stopping all nodes${default}"
          kill_cluster
          return $?
        fi
        status >/dev/null
        if [ $? -ne 0 ]
        then
                status
                return 1
        fi

        pid="`cat ${pidfile}`"
        if pid_exists "${pid}"
        then
                kill -15 "${pid}"
                sleep 5
                kill -9 "${pid}" 2>/dev/null
                rm -f "${pidfile}"
        fi
        status && return 1 || return 0
}

restart() {
        stop && start
        return $?
}

# 0, if running;
# 1 otherwise.
status() {
        red='\E[31;40;1m'
        green='\E[32;40;1m'
        default='\E[0m'

        if [ -f "${pidfile}" ]
        then
                pid="`cat ${pidfile}`"
                if pid_exists "${pid}"
                then
                        echo -e "${appname}-${HOSTNAME}-${nodeName}-${nodeNum} (pid ${pid}) is ${green}running${default}."
                        return 0
                else
                        echo -e "${appname}-${HOSTNAME}-${nodeName}-${nodeNum} (pid ${pid}) is ${red}dead${default}."
                        return 1
                fi
        fi
        echo -e "${appname}-${HOSTNAME}-${nodeName}-${nodeNum} is ${red}stopped${default}."
        return 1
}

usage() {
        cat <<EOF
Usage:
${appname} (start|stop|restart|status|usage) [config-name] [node-number]
EOF
        return 0
}

main() {
        case "$1" in
                'star'|'start')
                        start
                        return $?
                        ;;
                'sto'|'stop')
                        stop "$2"
                        return $?
                        ;;
                'r'|'re'|'res'|'rest'|'resta'|'restar'|'restart')
                        restart
                        return $?
                        ;;
                'stat'|'statu'|'status')
                        status
                        return $?
                        ;;
                'u'|'us'|'usa'|'usag'|'usage')
                        usage
                        return $?
                        ;;
                *)
                        usage
                        return 1
                        ;;
        esac
}

main "$@"
exit $?