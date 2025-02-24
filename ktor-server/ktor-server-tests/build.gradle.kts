description = ""

val jansi_version: String by project.extra

kotlin.sourceSets {
    val jvmMain by getting {
        dependencies {
            api(project(":ktor-server"))
            api(project(":ktor-server:ktor-server-test-host"))
        }
    }
    val jvmTest by getting {
        dependencies {
            api(project(":ktor-server"))
            api(project(":ktor-server:ktor-server-core", configuration = "testOutput"))
            implementation("org.fusesource.jansi:jansi:$jansi_version")
        }
    }
}
