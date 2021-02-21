FROM amazoncorretto:11-alpine-jdk

LABEL maintainer="Brian Schalme <bschalme@airspeed.ca>"

ADD build/libs/timesheet-fetcher-1.0.0-SNAPSHOT-all.jar app.jar
CMD ["java", "-jar", "app.jar", "-v"]  
