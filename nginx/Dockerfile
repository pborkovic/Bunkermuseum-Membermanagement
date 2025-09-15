FROM ghcr.io/jqlang/jq:latest AS jq-stage

# Build stage
FROM eclipse-temurin:21-jdk AS build
COPY --from=jq-stage /jq /usr/bin/jq
RUN jq --version

ENV HOME=/app
RUN mkdir -p $HOME
WORKDIR $HOME
COPY . $HOME

# Build the application with production profile
RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=secret,id=proKey \
    --mount=type=secret,id=offlineKey \
    sh -c 'PRO_KEY=$(jq -r ".proKey // empty" /run/secrets/proKey 2>/dev/null || echo "") && \
    OFFLINE_KEY=$(cat /run/secrets/offlineKey 2>/dev/null || echo "") && \
    ./mvnw clean package -Pproduction -DskipTests -Dvaadin.proKey=${PRO_KEY} -Dvaadin.offlineKey=${OFFLINE_KEY}'

# Runtime stage with nginx
FROM nginx:alpine
COPY --from=build /app/target/*.jar /app/app.jar

# Install openjdk for running Spring Boot app
RUN apk add --no-cache openjdk21-jre

# Copy nginx configuration
COPY nginx/nginx.conf /etc/nginx/nginx.conf

# Create startup script
RUN echo '#!/bin/sh' > /start.sh && \
    echo 'java -jar /app/app.jar --spring.profiles.active=prod &' >> /start.sh && \
    echo 'nginx -g "daemon off;"' >> /start.sh && \
    chmod +x /start.sh

EXPOSE 80
CMD ["/start.sh"]