@echo off
for /f "tokens=14" %%a in ('ipconfig ^| findstr IPv4') do set _IPaddr=%%a
echo YOUR IP ADDRESS IS: %_IPaddr%
java -jar archive\RadarProject.exe %_IPaddr% PortFilter %_IPaddr% Offline > logall.txt

pause