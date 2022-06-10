FROM postgres:9.3
ENV POSTGRES_PASSWORD postgrespw
ENV POSTGRES_DB postgres
ENV TZ=Asia/Seoul
ENV POSTGRES_DB=oim
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_INITDB_ARGS="--encoding=UTF-8"
ENV ALLOW_IP_RANGE=0.0.0.0/0
WORKDIR /etc/postgressql/9.3/main
RUN echo "host all all 0.0.0.0/0 password" >> pg_hba.conf
COPY ./init.sh /docker-entrypoint-initdb.d/