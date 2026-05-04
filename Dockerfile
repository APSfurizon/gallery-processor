
FROM amazoncorretto:23-alpine-jdk

ENV APP_HOME=/app

COPY gallery-processor-*.jar $APP_HOME/application.jar
#COPY libs/ $APP_HOME/libs/


RUN apk --no-cache -s upgrade && apk --no-cache upgrade && apk add ffmpeg imagemagick \
    darktable libraw

WORKDIR $APP_HOME

RUN addgroup --system --gid 1001 fz-gallery && adduser --system --uid 1001 fz-gallery #&& chown -R fz-gallery:fz-gallery $APP_HOME/libs/ && chmod +x -R $APP_HOME/libs/
USER fz-gallery

EXPOSE 9091

CMD ["java",  "-XX:+UseG1GC", "-Xms2048m", "-Xmx2048m", "-XX:MaxGCPauseMillis=500",  "-jar", "./application.jar"]
