FROM toposoid/toposoid-scala-lib:0.5-SNAPSHOT

WORKDIR /app
ARG TARGET_BRANCH
ENV DEPLOYMENT=local
ENV _JAVA_OPTIONS="-Xms512m -Xmx4g"

RUN git clone https://github.com/toposoid/toposoid-knowledge-register-web.git \
&& cd toposoid-knowledge-register-web \
&& git fetch origin ${TARGET_BRANCH} \
&& git checkout ${TARGET_BRANCH} \
&& sbt playUpdateSecret 1> /dev/null \
&& sbt dist \
&& cd /app/toposoid-knowledge-register-web/target/universal \
&& unzip -o toposoid-knowledge-register-web-0.5-SNAPSHOT.zip

COPY ./docker-entrypoint.sh /app/
ENTRYPOINT ["/app/docker-entrypoint.sh"]

