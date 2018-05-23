FROM openjdk:8

ENV GOOGLE_APPLICATION_CREDENTIALS="conf/Eevee.Google.json"

RUN mkdir -p /eevee
RUN mkdir -p /eevee/conf

ADD ./build/libs/* /eevee/
ADD ./conf /eevee/conf

WORKDIR /eevee

CMD ["java", "-jar", "Eevee-1.0-SNAPSHOT.jar"]
