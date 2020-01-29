# Tango
This library provides a simple framework for a swing-based database application.

## Building

To build, type `mvn clean install`
This uses the checkerframework, which can be finicky. If maven will not build be sure of three things:

1. Maven should be 3.6 or later

1. The maven runner should use java 1.8, but not later versions.

1. JAVA_HOME should be defined, and point to JDK 1.8. 

If you want to use JDK 1.11 or later, you will need to make changes to the pom.xml file. See the checker framework for instructions. It's not clear if JDK 1.9 or 1.10 are supported, and I haven't tried them.