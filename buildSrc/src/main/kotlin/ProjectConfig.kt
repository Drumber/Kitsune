import org.gradle.api.JavaVersion

object ProjectConfig {
    const val MIN_SDK = 26
    const val TARGET_SDK = 35
    const val COMPILE_SDK = 35
    val JAVA_VERSION = JavaVersion.VERSION_11
    const val KOTLIN_JVM_TARGET = "11"
}