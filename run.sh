#!/bin/sh

docker run -p 127.0.0.1:8090:8090 --env-file=/home/fz-gallery/gallery-processor/.env -v /home/fz-gallery/gallery-processor/data:/app/data fz-gallery-processor
