
echo INSTALLATION STARTED

call mvn install:install-file -DgroupId=com.ibm.tpox -DartifactId=tpox-workload -Dversion=2.1 -Dpackaging=jar -Dfile=./tpox-workload-2.1.jar
call mvn install:install-file -DgroupId=javax.xml.xquery -DartifactId=xqj-tck -Dversion=1.0 -Dpackaging=jar -Dfile=./xqj-tck-1.0.jar

echo INSTALLATION COMPLETED
echo CHECK THE LOGS TO MAKE SURE THAT EVERYTHING WENT FINE

pause
