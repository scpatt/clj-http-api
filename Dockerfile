FROM clojure:tools-deps

WORKDIR /app

COPY deps.edn ./

RUN clojure -P

COPY src ./src
COPY resources ./resources

EXPOSE 8080

CMD ["clojure", "-M", "-m", "core"]