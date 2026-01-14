package com.knightboost.lancet.plugin.task

import com.didiglobal.booster.transform.AbstractTransformContext
import org.gradle.api.Project
import java.io.File

open class LancetBoosterContext(
    val project: Project,
    applicationId: String,
    name: String,
    bootClasspath: Collection<File>,
    compileClasspath: Collection<File>,
    runtimeClasspath: Collection<File>,
) : AbstractTransformContext(
    applicationId,
    name,
    bootClasspath,
    compileClasspath,
    runtimeClasspath,
) {

}