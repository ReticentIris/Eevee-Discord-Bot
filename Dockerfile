FROM openjdk:8

ENV GOOGLE_APPLICATION_CREDENTIALS="conf/Eevee.Google.json"

RUN mkdir -p /eevee

ADD ./build/libs/* /eevee/

WORKDIR /eevee

CMD ["java", "-jar", "Eevee-1.0-SNAPSHOT.jar"]
