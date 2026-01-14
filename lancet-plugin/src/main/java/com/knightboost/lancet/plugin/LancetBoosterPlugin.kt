package com.knightboost.lancet.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppExtension
import com.android.tools.r8.internal.ms
import com.knightboost.lancet.internal.log.Impl.BaseLogger
import com.knightboost.lancet.internal.log.WeaverLog
import com.knightboost.lancet.plugin.task.LancetXBoosterTransformTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.configurationcache.extensions.capitalized

class LancetBoosterPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit {
        val extension = project.extensions.create("LancetX", LancetExtension::class.java ,project.objects)
        val context = LancetContext(
            project,
            project.extensions.getByType(AppExtension::class.java),
            extension
        )
        project.extensions.extraProperties.set("lancetx.context", context)
        registerTransform(project)
        project.logger.quiet("config LancetX $extension")
        WeaverLog.setImpl(object : BaseLogger() {
            override fun write(
                level: LogLevel?,
                prefix: String?,
                msg: String?,
                t: Throwable?
            ) {
                project.logger.quiet("$level [$prefix][${Thread.currentThread().name}] $msg", t)
            }
        })
        project.afterEvaluate {
            WeaverLog.setLevel(if (extension.debug) WeaverLog.Level.DEBUG else WeaverLog.Level.INFO)
        }
    }

    private fun registerTransform(project: Project) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val transform = project.tasks.register(
                "transform${variant.name.capitalized()}ClassesWithLancetX",
                LancetXBoosterTransformTask::class.java
            ) {
                it.classTransformers = listOf(LancetTransformer())
                it.variant = variant
                it.applicationId = variant.namespace.get()
                it.bootClasspath = androidComponents.sdkComponents.bootClasspath
            }
            variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                .use(transform).toTransform(
                    ScopedArtifact.CLASSES,
                    LancetXBoosterTransformTask::allJars,
                    LancetXBoosterTransformTask::allDirectories,
                    LancetXBoosterTransformTask::output
                )
        }
    }

}

