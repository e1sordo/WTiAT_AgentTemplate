#!/bin/bash

# Prompt exapmle:
# ./start-unix-agent.sh --name="Agent 1" [--id="923420103"]

readonly COMMON_CONFIG_FILE=conf/common-config.conf
readonly COMMON_SERVICE_STARTER=bin/service-template
readonly MAIN_USER=$(ls -ld | awk '{print $3}')
readonly MAIN_GROUP=$(ls -ld | awk '{print $4}')

# require script to be run as root
if [[ $(/usr/bin/id -u) -ne 0 ]]; then
  printf "*************************************\n"
  printf "* Error: Script must be running as root \n"
  printf "*************************************\n"
  exit 1
fi

while [ $# -gt 0 ]; do
  case "$1" in
  --name=*)
    agent_name="${1#*=}"
    ;;
  --id=*)
    agent_id="${1#*=}"
    ;;
  *)
    printf "*************************************\n"
    printf "* Error: Invalid argument ${1#*=}\n"
    printf "*************************************\n"
    exit 1
    ;;
  esac
  shift
done

# TODO: AGENT_NAME is mandatory param

# check if string contains only alphabets, numbers and ' ', '-' or '_'
if [[ ! $agent_name =~ ^[0-9a-zA-Z\ _\-]+$ ]]; then
  printf "*************************************\n"
  printf "* Error: Agent name must contains only alphabets, numbers and these special characters: ' ', '-', '_' \n"
  printf "*************************************\n"
  exit 1
fi

# replace white spaces with underscores
agent_name=${agent_name// /_}

printf "Agent name is %s\n" "$agent_name"

if [ -n "$agent_id" ]; then
  printf "Agent ID is %s\n" "$agent_id"
else
  printf "Agent ID is null. Registration request will be sended\n"
fi

#
# prepare all needed for agent creation files
#

# create agent dir
agent_dir="running-agents/$agent_name"
#chmod 755 "running-agents"
#chown $MAIN_USER:$MAIN_GROUP "running-agents"

printf "$agent_dir \n"

printf "$PWD \n"

# TODO: remove me!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
rm -r $agent_dir

if [ ! -d $agent_dir ]; then
  mkdir -p $agent_dir
#  chmod 755 $agent_dir
  chown $MAIN_USER:$MAIN_GROUP $agent_dir
else
  printf "*************************************\n"
  printf "* Error: Agent with this name is already created\n"
  printf "*************************************\n"
  exit 1
fi

# create config file and fill placeholdes with real param values
agent_full_name=${agent_name}_Agent

config_file_name="config.conf"
config_file=$agent_dir/$config_file_name

log_file_name="service.log"
log_file_path=$(echo "$agent_dir/$log_file_name" | sed 's_/_\\/_g')

sed -e "s/\${app_name}/${agent_name}/" -e "s/\${app_long_name}/${agent_full_name}/" \
  -e "s/\${app_description}/Agent name: ${agent_name}, Agent ID: ${agent_id}/" \
  -e "s/\${log_file_path}/${log_file_path}/" \
  $COMMON_CONFIG_FILE >$config_file
chmod 644 $config_file
chown $MAIN_USER:$MAIN_GROUP $config_file

printf "Config file was created \n"

# copy service starter and fill placeholders with real param value
service_starter_file="${agent_dir}/${agent_name}.sh"
bin_absolute_path=$(echo "$PWD/bin/" | sed 's_/_\\/_g')
config_file_absolute_path=$(echo "$PWD/$config_file" | sed 's_/_\\/_g')
agent_dir_full_path=$(echo "$PWD/$agent_dir" | sed 's_/_\\/_g')

sed -e "s/\${app_name}/${agent_name}/" -e "s/\${app_long_name}/${agent_full_name}/" \
  $COMMON_SERVICE_STARTER >$service_starter_file
chmod 755 $service_starter_file
chown $MAIN_USER:$MAIN_GROUP $service_starter_file

## create log file
#touch "${agent_dir}/${log_file_name}"
#chmod 644 ${agent_dir}/${log_file_name}
#chown $MAIN_USER:$MAIN_GROUP ${agent_dir}/${log_file_name}

## create status checking files
#pid_status_file="${agent_dir}/${agent_name}.pid"
#touch $pid_status_file
#chmod 644 $pid_status_file
#chown $MAIN_USER:$MAIN_GROUP $pid_status_file
#
#status_file="${agent_dir}/${agent_name}.status"
#touch $status_file
#chmod 644 $status_file
#chown $MAIN_USER:$MAIN_GROUP $status_file
#
#java_status_file="${agent_dir}/${agent_name}.java.status"
#touch $java_status_file
#chmod 644 $java_status_file
##chown $MAIN_USER:$MAIN_GROUP $java_status_file

#
# launch new agent
#
/bin/bash $service_starter_file "installstart"
