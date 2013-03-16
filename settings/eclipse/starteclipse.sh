#!/bin/bash

# SSH to the server
ssh -p15674 -t -t mosama@pwrdude.com -L 60000:192.168.2.10:60000 -L 60020:192.168.2.10:60020 -L 2181:192.168.2.10:2181 &

pid=$!

GTK2_RC_FILES=${HOME}/gtkrc-eclipse ${HOME}/programs/eclipse/eclipse

kill -9 $pid
