@echo off
REM Copyright (C) 2007 Google Inc.
REM
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with the License.
REM You may obtain a copy of the License at
REM
REM     http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.

REM don't modify the caller's environment
setlocal

REM Locate dx.jar in the directory where dx.bat was found and start it.

rem Set up prog to be the path of this script, including following symlinks,
rem and set up progdir to be the fully-qualified pathname of its directory.
set prog=%~f0

rem Change current directory to where dx is, to avoid issues with directories
rem containing whitespaces.
cd /d %~dp0

set jarfile=dx.jar
set frameworkdir=

if exist %frameworkdir%%jarfile% goto JarFileOk
    set frameworkdir=lib\

if exist %frameworkdir%%jarfile% goto JarFileOk
    set frameworkdir=..\framework\

:JarFileOk

set jarpath=%frameworkdir%%jarfile%

call java -Djava.ext.dirs=%frameworkdir% -jar %jarpath% %*

