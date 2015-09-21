@echo off
@
setlocal

call set-tpox-env.cmd

rem When inserts are run with the workload driver, the combination of -u and
rem -tr options should be used.  For example, to insert 5M custacc docs,
rem you can use -u 100 and -tr 50000.  If each of 100 users inserts 50000 then
rem 5M of docs are inserted in total. A properties file with a single insert
rem transaction is useful to insert an exact number of documents.


rem insert customers to the cache
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XDM/insCustacc.xml -tr 5000 -u 10

rem get insert statistics
"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.client.tpox.StatisticsCollector %admin_addr% %schema% DocumentManagement storeDocumentFromString InsertCustacc ./stats.txt false

rem perform queries loopig by user count
for /l %%x in (50, 10, 100) do (
	echo %%x
	"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XDM/custaccs.xml -u %%x
	"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.client.tpox.StatisticsCollector %admin_addr% %schema% QueryManagement getXML Users=%%x ./stats.txt false
)


goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xdm-custacc.cmd
goto exit

:exit
endlocal
@echo on
