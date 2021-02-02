@Library('k8s-jenkins-pipeline-library@1.3.18')

import no.ok.build.k8s.jenkins.pipeline.stages.*
import no.ok.build.k8s.jenkins.pipeline.stages.gradle.*
import no.ok.build.k8s.jenkins.pipeline.pipeline.*
import no.ok.build.k8s.jenkins.pipeline.common.*

Closure buildAndTest = {
    sh "./gradlew --no-daemon build"
}

GradleConfiguration.instance().setImage("container-registry.oslo.kommune.no/java-11-serverless:0.1.0")
GradleConfiguration.instance().setResourceLimitMemory("2Gi")

new Pipeline(this)
        .addStage(new ScmCheckoutStage())
        .addStage(new GradleContainerTemplate("Build and test", buildAndTest))
        .execute()
