package com.flutter.gradle

import androidx.annotation.VisibleForTesting
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.OutputFiles
import org.gradle.process.ExecSpec
import java.nio.file.Paths

class BaseFlutterTaskHelper(
    private var baseFlutterTask: BaseFlutterTask
) {
    @VisibleForTesting
    internal var gradleErrorMessage = "Invalid Flutter source directory: ${baseFlutterTask.sourceDir}"

    /**
     * Gets the dependency file based on the path from the intermediate directory.
     *
     * @return the dependency file based on the current intermediate directory path.
     */
    @OutputFiles
    @VisibleForTesting
    internal fun getDependenciesFiles(): FileCollection {
        var depfiles: FileCollection = baseFlutterTask.project.files()

        // Includes all sources used in the flutter compilation.
        depfiles += baseFlutterTask.project.files("${baseFlutterTask.intermediateDir}/flutter_build.d")
        return depfiles
    }

    /**
     * Checks precondition to ensures sourceDir is not null and is a directory.
     *
     * @throws GradleException if sourceDir is null or is not a directory
     */
    @VisibleForTesting
    internal fun checkPreConditions() {
        if (baseFlutterTask.sourceDir == null || !baseFlutterTask.sourceDir!!.isDirectory) {
            throw GradleException(gradleErrorMessage)
        }
    }

    /**
     * Computes the rule names for flutter assemble. To speed up builds that contain
     * multiple ABIs, the target name is used to communicate which ones are required
     * rather than the TargetPlatform. This allows multiple builds to share the same
     * cache.
     *
     * @param baseFlutterTask is a BaseFlutterTask to access its properties
     * @return the list of rule names for flutter assemble.
     */
    @VisibleForTesting
    internal fun generateRuleNames(baseFlutterTask: BaseFlutterTask): List<String> {
        val ruleNames: List<String> =
            when {
                baseFlutterTask.buildMode == "debug" -> listOf("debug_android_application")
                baseFlutterTask.deferredComponents!! ->
                    baseFlutterTask.targetPlatformValues!!.map {
                        "android_aot_deferred_components_bundle_${baseFlutterTask.buildMode}_$it"
                    }
                else -> baseFlutterTask.targetPlatformValues!!.map { "android_aot_bundle_${baseFlutterTask.buildMode}_$it" }
            }
        return ruleNames
    }

    /**
     * Creates and configures the build processes of an Android Flutter application to be executed.
     * The configuration includes setting the executable to the Flutter command-line tool (Flutter CLI)
     * setting the working directory to the Flutter project's source directory, adding command-line arguments and build rules
     * to configure various build options.
     *
     * @return an Action<ExecSpec> of build processes and options to be executed.
     */
    @VisibleForTesting
    internal fun createExecSpecActionFromTask(): Action<ExecSpec> =
        Action<ExecSpec> {
            executable(baseFlutterTask.flutterExecutable!!.absolutePath)
            workingDir(baseFlutterTask.sourceDir)
            baseFlutterTask.localEngine?.let {
                args("--local-engine", baseFlutterTask.localEngine)
                args("--local-engine-src-path", baseFlutterTask.localEngineSrcPath)
            }
            baseFlutterTask.localEngineHost?.let {
                args("--local-engine-host", baseFlutterTask.localEngineHost)
            }
            if (baseFlutterTask.verbose == true) {
                args("--verbose")
            } else {
                args("--quiet")
            }
            args("assemble")
            args("--no-version-check")
            args("--depfile", "${baseFlutterTask.intermediateDir}/flutter_build.d")
            args("--output", "${baseFlutterTask.intermediateDir}")
            baseFlutterTask.performanceMeasurementFile?.let {
                args("--performance-measurement-file=${baseFlutterTask.performanceMeasurementFile}")
            }
            if (!baseFlutterTask.fastStart!! || baseFlutterTask.buildMode != "debug") {
                args("-dTargetFile=${baseFlutterTask.targetPath}")
            } else {
                args("-dTargetFile=${Paths.get(baseFlutterTask.flutterRoot!!.absolutePath, "examples", "splash", "lib", "main.dart")}")
            }
            args("-dTargetPlatform=android")
            args("-dBuildMode=${baseFlutterTask.buildMode}")
            baseFlutterTask.trackWidgetCreation?.let {
                args("-dTrackWidgetCreation=${baseFlutterTask.trackWidgetCreation}")
            }
            baseFlutterTask.splitDebugInfo?.let {
                args("-dSplitDebugInfo=${baseFlutterTask.splitDebugInfo}")
            }
            if (baseFlutterTask.treeShakeIcons == true) {
                args("-dTreeShakeIcons=true")
            }
            if (baseFlutterTask.dartObfuscation == true) {
                args("-dDartObfuscation=true")
            }
            baseFlutterTask.dartDefines?.let {
                args("--DartDefines=${baseFlutterTask.dartDefines}")
            }
            baseFlutterTask.bundleSkSLPath?.let {
                args("-dBundleSkSLPath=${baseFlutterTask.bundleSkSLPath}")
            }
            baseFlutterTask.codeSizeDirectory?.let {
                args("-dCodeSizeDirectory=${baseFlutterTask.codeSizeDirectory}")
            }
            baseFlutterTask.flavor?.let {
                args("-dFlavor=${baseFlutterTask.flavor}")
            }
            baseFlutterTask.extraGenSnapshotOptions?.let {
                args("--ExtraGenSnapshotOptions=${baseFlutterTask.extraGenSnapshotOptions}")
            }
            baseFlutterTask.frontendServerStarterPath?.let {
                args("-dFrontendServerStarterPath=${baseFlutterTask.frontendServerStarterPath}")
            }
            baseFlutterTask.extraFrontEndOptions?.let {
                args("--ExtraFrontEndOptions=${baseFlutterTask.extraFrontEndOptions}")
            }

            args("-dAndroidArchs=${baseFlutterTask.targetPlatformValues!!.joinToString(" ")}")
            args("-dMinSdkVersion=${baseFlutterTask.minSdkVersion}")
            args(generateRuleNames(baseFlutterTask))
        }
}
