@echo off
REM Project name: Windows下的java启动脚本
REM Author:       Jason
REM Date:         2013-4-16
REM Version:      1.0


IF "%JAVA_HOME%" EQU "" (
echo JAVA_HOME PARARMS IS NULL
pause
goto END
)


REM 叠加classpath method
IF "%1"=="##" goto ENVSET


REM 库文件所在的目录，相对于当前路径
SET LIBDIR=../lib


REM 设定CLSPATH 变量，并初始化为系统的classpath
SET CLSPATH=.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar;../config;


REM 读取指定Libs目录下的*.jar，并执行ENVSET函数进行classpath叠加
FOR %%c IN (%LIBDIR%\*.jar) DO CALL %0 ## %%c

REM 从app.pro 资源文件中读取启动脚本所需的信息，如：title、app launcher、jvm params...
REM app.pro是N个键值对，重复的键将被最后一个替换，不会叠加
FOR /F "tokens=1,2 delims==" %%A IN (.\app.properties) DO set %%A=%%B


title "jabberRobot"


REM 指定logback配置文件所在位置
REM 废弃
REM set LogArgs=-Dlogback.configurationFile=conf/logback.xml


REM 要启动的类名
SET CLSNAME=cn.shadowsong.eve.pla.JabberQQAdapter


REM JVM参数设定
IF "%app.xmx%" NEQ "" (
	set App.xmx=%app.xmx%
) else (
	set App.xmx=-Xmx128M
)

IF "%app.xms%" NEQ "" (
	set App.xms=%app.xms%
) else (
	set App.xms=-Xms128M
)

IF "%app.xmn%" NEQ "" (
	set App.xmn=%app.xmn%
) else (
	set App.xmn=-Xmn16M
)

IF "%app.xss%" NEQ "" (
	set App.xss=%app.xss%
) else (
	set App.xss=-Xss256K
)
set MemoryArgs=%App.xmx% %App.xms% %App.xmn% %App.xss% 


REM 运行app
GOTO RUN


:RUN
echo %CLSPATH%
echo %CLSNAME% 
java -server -Dfile.encoding=UTF-8 -cp %CLSPATH% %MemoryArgs% %CLSNAME%
goto END


:ENVSET
set CLSPATH=%CLSPATH%;%2
goto END


:END
