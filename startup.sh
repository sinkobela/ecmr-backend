#!/bin/bash
shopt -s nullglob #do not iterate over empty directory
for f in /usr/local/cacerts/* ; do
  echo "Importing certificate $f"
  file_name=$(basename ${f})
  keytool -cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias $file_name -file $f
done

if [ -z "$1" ]
then
	echo "No path for jar file specified. Exiting"
	exit 1
fi

if [ -z "$JAVA_OPTS" ]
then
	echo "No JAVA_OPTS"
else
	echo "JAVA_OPTS: $JAVA_OPTS"
fi

java $JAVA_OPTS -jar $1
