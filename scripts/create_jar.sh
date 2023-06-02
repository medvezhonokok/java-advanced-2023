#!/bin/bash

PATH_TO_JAR=./../../java-advanced-2023/artifacts/info.kgeorgiy.java.advanced.implementor.jar
PATH_TO_JAVA_FILE=./../../java-advanced/java-solutions/info/kgeorgiy/ja/kim/implementor/Implementor.java

javac -cp $PATH_TO_JAR $PATH_TO_JAVA_FILE -d .
jar cmf MANIFEST.MF Implementor.jar info
rm -rf info
