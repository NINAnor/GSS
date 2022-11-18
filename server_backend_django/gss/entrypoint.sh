#!/bin/bash

set -ueo pipefail

pdm run ./manage.py migrate
pdm run ./manage.py populate_for_gss
pdm run ./manage.py collectstatic --noinput
exec "$@"
