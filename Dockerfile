FROM toposoid/toposoid-core:0.1.0

WORKDIR /app

ENV DEPLOYMENT=local
ENV _JAVA_OPTIONS="-Xms2g -Xmx4g"

RUN git clone https://github.com/toposoid/toposoid-knowledge-register-web.git \
&& cd toposoid-knowledge-register-web \
&& sbt playUpdateSecret 1> /dev/null \
&& sbt dist \
&& cd /app/toposoid-knowledge-register-web/target/universal \
&& unzip -o toposoid-knowledge-register-web-0.1.0.zip

COPY ./docker-entrypoint.sh /app/
ENTRYPOINT ["/app/docker-entrypoint.sh"]

