@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
set JAVA_TOOL_OPTIONS=-Duser.timezone=Asia/Jakarta
echo Using Java isolated runtime: %JAVA_HOME%
java -version
mvn -U spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Duser.timezone=Asia/Jakarta"
