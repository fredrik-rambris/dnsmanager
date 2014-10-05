#!/bin/sh

JAR="$( find WebContent/WEB-INF/lib -name '*.jar' -printf ':%p' | awk '{ print substr($0, 2); }' )"

java -cp $JAR:build/classes -DCONFIG_FILE=dnsmanager.xml com.rambris.dnsmanager.DNS $@
