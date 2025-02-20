plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.gonudayo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

intellij {
    version.set("2020.3") // 최소 버전 지정
    type.set("PY") // Ultimate + Community Edition 지원
    sandboxDir.set("${System.getProperty("user.home")}/.PyCharmSandbox")
}

//intellij {
//    localPath.set("C:/Program Files/JetBrains/PyCharm Community Edition 2020.3.5") // 로컬 PyCharm 지정
//    type.set("PC") // PyCharm Community Edition
//    sandboxDir.set("${System.getProperty("user.home")}/.PyCharmSandbox") // 샌드박스 설정
//}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("203")   // PyCharm 2020.3 지원
        untilBuild.set("243.*") // 최신 버전까지 호환
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
