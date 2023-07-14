import gradle.kotlin.dsl.accessors._3b500bc768bc147381ee2b86a0d3cfe7.publishing
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.withType

fun Project.setupPublishing(mavenArtifactId: String) {
    publishing {
        publications.withType<MavenPublication> {
            artifactId = if (name == "kotlinMultiplatform") {
                mavenArtifactId
            } else {
                "$mavenArtifactId-$name"
            }
        }
    }
}
