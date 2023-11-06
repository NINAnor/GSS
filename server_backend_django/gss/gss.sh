#!/bin/bash

set -ueo pipefail
# set -x

echo "###################################"
echo "#   Starting gss server."
echo "###################################"

export DEBUG=true
echo "DEBUG=$DEBUG"
# exec "$@"

# source activate venv if the file exists
if [ -f ../env/bin/activate ]; then
    echo "Activating virtual environment"
    source ../env/bin/activate
fi

# source config file if it exists
if [ -f ./config.sh ]; then
    echo "Sourcing config.sh"
    source ./config.sh
fi

start_collectstatic() {
    python manage.py collectstatic --noinput
}


start_gunicorn() {
    gunicorn gss.wsgi --bind 0.0.0.0:8000 --timeout 0
}

stop_processes() {
    pkill -f "python manage.py collectstatic --noinput" &
    pkill -f "gunicorn gss.wsgi" 
}

# if no argument was passed to the script print usager
if [ $# -eq 0 ]; then
    echo "Usage: $0 [start|stop]"
    exit 1
fi
if [ "$1" == "start" ]; then
    start_collectstatic &
    start_gunicorn
elif [ "$1" == "stop" ]; then
    stop_processes
fi
