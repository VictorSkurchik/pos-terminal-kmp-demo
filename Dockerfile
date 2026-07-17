# syntax=docker/dockerfile:1

# ---- Build stage: JDK 21 + Android SDK ----
# The Android SDK is required because :core is a KMP module with an Android target,
# which Gradle evaluates while configuring :server.
FROM eclipse-temurin:21-jdk AS build

ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    GRADLE_OPTS=-Dorg.gradle.daemon=false

RUN apt-get update \
 && apt-get install -y --no-install-recommends unzip curl ca-certificates \
 && rm -rf /var/lib/apt/lists/*

# Android command-line tools + packages matching compileSdk 37
RUN mkdir -p "$ANDROID_HOME/cmdline-tools" \
 && curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o /tmp/cli.zip \
 && unzip -q /tmp/cli.zip -d "$ANDROID_HOME/cmdline-tools" \
 && mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest" \
 && yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null \
 && "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" \
      "platform-tools" "platforms;android-37" "build-tools;37.0.0" > /dev/null \
 && rm /tmp/cli.zip

WORKDIR /app
COPY . .
RUN chmod +x gradlew \
 && ./gradlew :server:buildFatJar --no-daemon --no-configuration-cache

# ---- Run stage: slim JRE ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/server/build/libs/*-all.jar /app/app.jar
# Render injects $PORT; default 8080 for local `docker run`.
ENV PORT=8080
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]
