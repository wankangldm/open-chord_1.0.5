@echo off
cls
java -cp .;..\dist\openchord_1.0.5.jar;..\config;..\lib\log4j.jar de.uniba.wiai.lspi.chord.console.Main %*
