FROM eclipse-temurin:17.0.3_7-jre-alpine
LABEL maintainer homeworldshadow
COPY build/install/TeleGPT/ ./telegpt/
RUN chmod +x ./telegpt/bin/TeleGPT
ENTRYPOINT ["./telegpt/bin/TeleGPT"]

EXPOSE 80
EXPOSE 443

