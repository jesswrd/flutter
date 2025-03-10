package com.flutter.gradle

import com.flutter.gradle.BaseFlutterTaskHelperTest.BaseFlutterTaskPropertiesTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.process.ExecSpec
import org.gradle.process.ProcessForkOptions
import org.junit.jupiter.api.assertDoesNotThrow
import java.nio.file.Paths
import kotlin.test.Test

class BaseFlutterTaskTest {
    @Test
    fun `getDependencyFiles returns a FileCollection of dependency file(s)`() {
        val baseFlutterTask = mockk<BaseFlutterTask>()
        val project = mockk<Project>()
        val configFileCollection = mockk<ConfigurableFileCollection>()
        every { baseFlutterTask.sourceDir } returns BaseFlutterTaskPropertiesTest.sourceDirTest

        val helper = BaseFlutterTaskHelper(baseFlutterTask)

        every { baseFlutterTask.project } returns project
        every { baseFlutterTask.intermediateDir } returns BaseFlutterTaskPropertiesTest.intermediateDirFileTest

        val projectIntermediary = baseFlutterTask.project
        val interDirFile = baseFlutterTask.intermediateDir

        every { projectIntermediary.files() } returns configFileCollection
        every { projectIntermediary.files("$interDirFile/flutter_build.d") } returns configFileCollection
        every { configFileCollection.plus(configFileCollection) } returns configFileCollection

        helper.getDependenciesFiles()
        verify { projectIntermediary.files() }
        verify { projectIntermediary.files("${BaseFlutterTaskPropertiesTest.intermediateDirFileTest}/flutter_build.d") }
    }

    @Test
    fun `buildBundle builds a Flutter application bundle for Android`() {
        val buildModeString = "debug"

        // Create necessary mocks.
        val baseFlutterTask = mockk<BaseFlutterTask>()
        val mockExecSpec = mockk<ExecSpec>()
        val mockProcessForkOptions = mockk<ProcessForkOptions>()

        // Check preconditions
        every { baseFlutterTask.sourceDir } returns BaseFlutterTaskPropertiesTest.sourceDirTest
        every { baseFlutterTask.sourceDir!!.isDirectory } returns true

        every { baseFlutterTask.intermediateDir } returns BaseFlutterTaskPropertiesTest.intermediateDirFileTest
        every { baseFlutterTask.intermediateDir.mkdirs() } returns false

        val helper = BaseFlutterTaskHelper(baseFlutterTask)
        assertDoesNotThrow { helper.checkPreConditions() }

        // Create action to be executed.
        val execSpecActionFromTask = helper.createExecSpecActionFromTask()

        // Mock return values of properties.
        every { baseFlutterTask.flutterExecutable } returns BaseFlutterTaskPropertiesTest.flutterExecutableTest
        every {
            baseFlutterTask.flutterExecutable!!.absolutePath
        } returns BaseFlutterTaskPropertiesTest.FLUTTER_EXECUTABLE_ABSOLUTE_PATH_TEST
        every { baseFlutterTask.sourceDir } returns BaseFlutterTaskPropertiesTest.sourceDirTest

        every { baseFlutterTask.localEngine } returns BaseFlutterTaskPropertiesTest.LOCAL_ENGINE_TEST
        every { baseFlutterTask.localEngineSrcPath } returns BaseFlutterTaskPropertiesTest.LOCAL_ENGINE_SRC_PATH_TEST

        every { baseFlutterTask.localEngineHost } returns BaseFlutterTaskPropertiesTest.LOCAL_ENGINE_HOST_TEST
        every { baseFlutterTask.verbose } returns true
        every { baseFlutterTask.intermediateDir } returns BaseFlutterTaskPropertiesTest.intermediateDirFileTest
        every { baseFlutterTask.performanceMeasurementFile } returns BaseFlutterTaskPropertiesTest.PERFORMANCE_MEASUREMENT_FILE_TEST

        every { baseFlutterTask.fastStart } returns true
        every { baseFlutterTask.buildMode } returns buildModeString
        every { baseFlutterTask.flutterRoot } returns BaseFlutterTaskPropertiesTest.flutterRootTest
        every { baseFlutterTask.flutterRoot!!.absolutePath } returns BaseFlutterTaskPropertiesTest.FLUTTER_ROOT_ABSOLUTE_PATH_TEST

        every { baseFlutterTask.trackWidgetCreation } returns true
        every { baseFlutterTask.splitDebugInfo } returns BaseFlutterTaskPropertiesTest.SPLIT_DEBUG_INFO_TEST
        every { baseFlutterTask.treeShakeIcons } returns true

        every { baseFlutterTask.dartObfuscation } returns true
        every { baseFlutterTask.dartDefines } returns BaseFlutterTaskPropertiesTest.DART_DEFINES_TEST
        every { baseFlutterTask.bundleSkSLPath } returns BaseFlutterTaskPropertiesTest.BUNDLE_SK_SL_PATH_TEST

        every { baseFlutterTask.codeSizeDirectory } returns BaseFlutterTaskPropertiesTest.CODE_SIZE_DIRECTORY_TEST
        every { baseFlutterTask.flavor } returns BaseFlutterTaskPropertiesTest.FLAVOR_TEST
        every { baseFlutterTask.extraGenSnapshotOptions } returns BaseFlutterTaskPropertiesTest.EXTRA_GEN_SNAPSHOT_OPTIONS_TEST

        every { baseFlutterTask.frontendServerStarterPath } returns BaseFlutterTaskPropertiesTest.FRONTEND_SERVER_STARTER_PATH_TEST
        every { baseFlutterTask.extraFrontEndOptions } returns BaseFlutterTaskPropertiesTest.EXTRA_FRONTEND_OPTIONS_TEST

        every { baseFlutterTask.targetPlatformValues } returns BaseFlutterTaskPropertiesTest.targetPlatformValuesList

        every { baseFlutterTask.minSdkVersion } returns BaseFlutterTaskPropertiesTest.MIN_SDK_VERSION_TEST

        // Mock the actual method calls. We don't make real calls because we cannot create a real
        // ExecSpec object.
        val taskAbsolutePath = baseFlutterTask.flutterExecutable!!.absolutePath
        every { mockExecSpec.executable(taskAbsolutePath) } returns mockProcessForkOptions

        val sourceDirFile = baseFlutterTask.sourceDir
        every { mockExecSpec.workingDir(sourceDirFile) } returns mockProcessForkOptions

        val localEngine = baseFlutterTask.localEngine
        every { mockExecSpec.args("--local-engine", localEngine) } returns mockExecSpec

        val localEngineSrcPath = baseFlutterTask.localEngineSrcPath
        every { mockExecSpec.args("--local-engine-src-path", localEngineSrcPath) } returns mockExecSpec

        val localEngineHost = baseFlutterTask.localEngineHost
        every { mockExecSpec.args("--local-engine-host", localEngineHost) } returns mockExecSpec
        every { mockExecSpec.args("--verbose") } returns mockExecSpec
        every { mockExecSpec.args("assemble") } returns mockExecSpec
        every { mockExecSpec.args("--no-version-check") } returns mockExecSpec

        val intermediateDir = baseFlutterTask.intermediateDir.toString()
        val depfilePath = "$intermediateDir/flutter_build.d"
        every { mockExecSpec.args("--depfile", depfilePath) } returns mockExecSpec
        every { mockExecSpec.args("--output", intermediateDir) } returns mockExecSpec

        val performanceMeasurementFile = baseFlutterTask.performanceMeasurementFile
        every { mockExecSpec.args("--performance-measurement-file=$performanceMeasurementFile") } returns mockExecSpec
        val taskRootAbsolutePath = baseFlutterTask.flutterRoot!!.absolutePath
        val targetFilePath = Paths.get(taskRootAbsolutePath, "examples", "splash", "lib", "main.dart")
        every { mockExecSpec.args("-dTargetFile=$targetFilePath") } returns mockExecSpec

        every { mockExecSpec.args("-dTargetPlatform=android") } returns mockExecSpec

        val buildModeTaskString = baseFlutterTask.buildMode
        every { mockExecSpec.args("-dBuildMode=$buildModeTaskString") } returns mockExecSpec

        val trackWidgetCreationBool = baseFlutterTask.trackWidgetCreation
        every { mockExecSpec.args("-dTrackWidgetCreation=$trackWidgetCreationBool") } returns mockExecSpec

        val splitDebugInfo = baseFlutterTask.splitDebugInfo
        every { mockExecSpec.args("-dSplitDebugInfo=$splitDebugInfo") } returns mockExecSpec
        every { mockExecSpec.args("-dTreeShakeIcons=true") } returns mockExecSpec

        every { mockExecSpec.args("-dDartObfuscation=true") } returns mockExecSpec
        val dartDefines = baseFlutterTask.dartDefines
        every { mockExecSpec.args("--DartDefines=$dartDefines") } returns mockExecSpec
        val bundleSkSLPath = baseFlutterTask.bundleSkSLPath
        every { mockExecSpec.args("-dBundleSkSLPath=$bundleSkSLPath") } returns mockExecSpec

        val codeSizeDirectory = baseFlutterTask.codeSizeDirectory
        every { mockExecSpec.args("-dCodeSizeDirectory=$codeSizeDirectory") } returns mockExecSpec
        val flavor = baseFlutterTask.flavor
        every { mockExecSpec.args("-dFlavor=$flavor") } returns mockExecSpec
        val extraGenSnapshotOptions = baseFlutterTask.extraGenSnapshotOptions
        every { mockExecSpec.args("--ExtraGenSnapshotOptions=$extraGenSnapshotOptions") } returns mockExecSpec

        val frontServerStarterPath = baseFlutterTask.frontendServerStarterPath
        every { mockExecSpec.args("-dFrontendServerStarterPath=$frontServerStarterPath") } returns mockExecSpec
        val extraFrontEndOptions = baseFlutterTask.extraFrontEndOptions
        every { mockExecSpec.args("--ExtraFrontEndOptions=$extraFrontEndOptions") } returns mockExecSpec

        val joinTestList = BaseFlutterTaskPropertiesTest.targetPlatformValuesList.joinToString(" ")
        every { mockExecSpec.args("-dAndroidArchs=$joinTestList") } returns mockExecSpec

        val minSdkVersionInt = baseFlutterTask.minSdkVersion.toString()
        every { mockExecSpec.args("-dMinSdkVersion=$minSdkVersionInt") } returns mockExecSpec

        val ruleNamesArray: Array<String> = helper.generateRuleNames(baseFlutterTask)
        every { mockExecSpec.args(ruleNamesArray) } returns mockExecSpec

        // The exec function will be deprecated in gradle 8.11 and will be removed in gradle 9.0
        // https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.kotlin.dsl/-kotlin-script/exec.html?query=abstract%20fun%20exec(configuration:%20Action%3CExecSpec%3E):%20ExecResult
        // The actions are executed.
        execSpecActionFromTask.execute(mockExecSpec)

        // After execution, we verify the functions are actually being
        // called with the expected argument passed in.
        verify { mockExecSpec.executable(BaseFlutterTaskPropertiesTest.FLUTTER_EXECUTABLE_ABSOLUTE_PATH_TEST) }
        verify { mockExecSpec.workingDir(BaseFlutterTaskPropertiesTest.sourceDirTest) }
        verify { mockExecSpec.args("--local-engine", BaseFlutterTaskPropertiesTest.LOCAL_ENGINE_TEST) }
        verify { mockExecSpec.args("--local-engine-src-path", BaseFlutterTaskPropertiesTest.LOCAL_ENGINE_SRC_PATH_TEST) }
        verify { mockExecSpec.args("--local-engine-host", BaseFlutterTaskPropertiesTest.LOCAL_ENGINE_HOST_TEST) }
        verify { mockExecSpec.args("--verbose") }
        verify { mockExecSpec.args("assemble") }
        verify { mockExecSpec.args("--no-version-check") }
        verify { mockExecSpec.args("--depfile", "${BaseFlutterTaskPropertiesTest.intermediateDirFileTest}/flutter_build.d") }
        verify { mockExecSpec.args("--output", "${BaseFlutterTaskPropertiesTest.intermediateDirFileTest}") }
        verify { mockExecSpec.args("--performance-measurement-file=${BaseFlutterTaskPropertiesTest.PERFORMANCE_MEASUREMENT_FILE_TEST}") }
        verify { mockExecSpec.args("-dTargetFile=${BaseFlutterTaskPropertiesTest.FLUTTER_TARGET_FILE_PATH}") }
        verify { mockExecSpec.args("-dTargetPlatform=android") }
        verify { mockExecSpec.args("-dBuildMode=$buildModeString") }
        verify { mockExecSpec.args("-dTrackWidgetCreation=${true}") }
        verify { mockExecSpec.args("-dSplitDebugInfo=${BaseFlutterTaskPropertiesTest.SPLIT_DEBUG_INFO_TEST}") }
        verify { mockExecSpec.args("-dTreeShakeIcons=true") }
        verify { mockExecSpec.args("-dDartObfuscation=true") }
        verify { mockExecSpec.args("--DartDefines=${BaseFlutterTaskPropertiesTest.DART_DEFINES_TEST}") }
        verify { mockExecSpec.args("-dBundleSkSLPath=${BaseFlutterTaskPropertiesTest.BUNDLE_SK_SL_PATH_TEST}") }
        verify { mockExecSpec.args("-dCodeSizeDirectory=${BaseFlutterTaskPropertiesTest.CODE_SIZE_DIRECTORY_TEST}") }
        verify { mockExecSpec.args("-dFlavor=${BaseFlutterTaskPropertiesTest.FLAVOR_TEST}") }
        verify { mockExecSpec.args("--ExtraGenSnapshotOptions=${BaseFlutterTaskPropertiesTest.EXTRA_GEN_SNAPSHOT_OPTIONS_TEST}") }
        verify { mockExecSpec.args("-dFrontendServerStarterPath=${BaseFlutterTaskPropertiesTest.FRONTEND_SERVER_STARTER_PATH_TEST}") }
        verify { mockExecSpec.args("--ExtraFrontEndOptions=${BaseFlutterTaskPropertiesTest.EXTRA_FRONTEND_OPTIONS_TEST}") }
        verify { mockExecSpec.args("-dAndroidArchs=${BaseFlutterTaskPropertiesTest.TARGET_PLATFORM_VALUES_JOINED_LIST}") }
        verify { mockExecSpec.args("-dMinSdkVersion=${BaseFlutterTaskPropertiesTest.MIN_SDK_VERSION_TEST}") }
        verify { mockExecSpec.args(ruleNamesArray) }
    }
}
