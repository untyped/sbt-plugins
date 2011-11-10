java -Xmx1024M -Xss2M -XX:+CMSClassUnloadingEnabled -jar `dirname $0`/sbt-launch.jar "$@"
