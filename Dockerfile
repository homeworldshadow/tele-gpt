FROM eclipse-temurin:17.0.3_7-jre-alpine
LABEL maintainer homeworldshadow
#COPY build/install/tele-gpt/ ./tele-gpt/
COPY bin/ ./tele-gpt/bin/
COPY lib/ ./tele-gpt/lib/
RUN chmod +x ./tele-gpt/bin/tele-gpt
ENTRYPOINT ["./tele-gpt/bin/tele-gpt"]

EXPOSE 80
EXPOSE 443

