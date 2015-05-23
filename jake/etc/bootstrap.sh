#!/bin/sh
################################################################################
# bootstrap.sh - Launches an executable jar directly.  This script should be
# prepended to an executable jar:
#     cat bootstrap.sh something.jar > something
#     chmod +x something
#     ./something --cool-option=yes
################################################################################

exec java -jar "$0" "$@"





