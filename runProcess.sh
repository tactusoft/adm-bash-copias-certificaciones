#!/bin/sh
ScriptFile=${0##*/}             # basename of current script
RunDir=`dirname $0`
export LogDir=$RunDir/log/
export ScriptDir="`(cd $RunDir; pwd)`/"
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-17.0.15.0.6-2.el9.x86_64
cd $RunDir

LOG_FILE=$ScriptDir/`echo $ScriptFile | sed -e "s,sh,$LOG_DATE_STAMP.log,"`

if [  -z $JAVA_HOME ]; then
	echo "La variable JAVA_HOME no esta definida"
	exit
fi

[ ! -f $ScriptDir/.functions ] && echo ".functions no encontrado" && exit 1
. $ScriptDir/.functions && Init_static_var_common 

Start_log


printf "Ejecutando Proceso bash\n" >> $LOG_FILE

$JAVA_HOME/bin/java -jar $ScriptDir/bashcopiascertificaciones-3.0.jar


printf "Fin Proceso bash\n" >> $LOG_FILE