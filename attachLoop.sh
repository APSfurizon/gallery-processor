#!/bin/bash

while true; do docker attach fz-gallery-processor.service; sleep 1; echo "WAITING 5 SECS"; sleep 5; service fz-gallery-processor start; sleep 2; done
