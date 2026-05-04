#!/bin/sh
set -e #crashes the script on errors

echo "pulling from git"
#git pull origin master
echo "stopping service"
service fz-gallery-processor stop
echo "building docker"
docker pull amazoncorretto:23-alpine
docker build --no-cache -t fz-gallery-processor .
echo "Starting service"
service fz-gallery-processor start
service fz-gallery-processor status
