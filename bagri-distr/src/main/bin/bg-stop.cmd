@echo off
@
@rem This will start XDM server application
@
setlocal

rem specify application home
set app_home=../

if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

if "%1"=="" goto usage
if "%2"=="" goto usage

"%java_exec%" -cp "%app_home%\lib\*" com.bagri.xdm.cache.hazelcast.XDMCacheStopper %*

goto exit

:usage

echo Usage:
echo ^<app_home^>\bin\bg-stop.cmd ^<comma-separated host list^> ^<comma-separated schema list or ALL for complete node shutdown^>
echo for example: ^<app_home^>\bin\bg-stop.cmd 127.0.0.1:5871,127.0.0.1:5872 default
goto exit

:exit
endlocal
@echo on
