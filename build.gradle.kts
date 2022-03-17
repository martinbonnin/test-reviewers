plugins {
  id("org.jetbrains.kotlin.jvm").version("1.6.10")
  id("com.apollographql.apollo3").version("3.1.0")
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.apollographql.apollo3:apollo-normalized-cache-sqlite:3.1.0")
  implementation("com.apollographql.apollo3:apollo-normalized-cache:3.1.0")
  implementation("com.apollographql.apollo3:apollo-runtime:3.1.0")
}

apollo {
  packageName.set("com.example")
}