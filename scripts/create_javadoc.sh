#!/bin/bash

PATH_TO_JAR=../../java-advanced-2023/artifacts/info.kgeorgiy.java.advanced.concurrent.jar
PATH_TO_JAVA_FILE=./../../java-advanced/java-solutions/info/kgeorgiy/ja/kim/concurrent/IterativeParallelism.java

javadoc -d javadoc -private -classpath $PATH_TO_JAR $PATH_TO_JAVA_FILE ../../java-advanced-2023/modules/info.kgeorgiy.java.advanced.concurrent/info/kgeorgiy/java/advanced/concurrent/ScalarIP.java