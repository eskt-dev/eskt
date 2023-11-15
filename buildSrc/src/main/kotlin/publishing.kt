import gradle.kotlin.dsl.accessors._39fc684ecd34fd87f7f7b5af98275af6.publishing
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.withType
import java.net.URI

fun Project.setupPublishing(mavenArtifactId: String) {
    publishing {
        publications.withType<MavenPublication> {
            artifactId = if (name == "kotlinMultiplatform") {
                mavenArtifactId
            } else {
                "$mavenArtifactId-$name"
            }
        }
        repositories {
            // from https://docs.github.com/en/actions/publishing-packages/publishing-java-packages-with-gradle#publishing-packages-to-github-packages
            maven {
                url = URI("https://maven.pkg.github.com/eskt-dev/eskt")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}
