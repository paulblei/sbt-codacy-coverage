package com.codacy

import java.io.File

import com.codacy.api.CodacyAPIClient
import com.codacy.io.FileUtils
import com.codacy.parsers.CoverageParser
import com.codacy.vcs.GitClient
import play.api.libs.json.Json
import sbt.Keys._
import sbt._

import scala.util.Try

object CodacyCoveragePlugin extends AutoPlugin {

  object autoImport {
    val codacyCoverage = taskKey[Unit]("Upload coverage reports to Codacy.")
    val codacyProjectToken = settingKey[Option[String]]("Your project token.")
    val codacyProjectTokenFile = settingKey[Option[String]]("Path for file containing your project token.")
    val codacyApiBaseUrl = settingKey[Option[String]]("The base URL for the Codacy API.")

    lazy val baseSettings: Seq[Def.Setting[_]] = Seq(
      codacyCoverage := {
        codacyCoverageCommand(state.value, baseDirectory.value, crossTarget.value / _,
          crossTarget.value / "coverage-report" / "codacy-coverage.json",
          codacyProjectToken.value, codacyProjectTokenFile.value, codacyApiBaseUrl.value)
      },
      codacyProjectToken := None,
      codacyProjectTokenFile := None,
      codacyApiBaseUrl := None
    )
  }

  import com.codacy.CodacyCoveragePlugin.autoImport._

  override def trigger = allRequirements

  override val projectSettings = baseSettings

  private val publicApiBaseUrl = "https://www.codacy.com"

  private def codacyCoverageCommand(state: State, rootProjectDir: File, targetFolder: (String) => File, codacyCoverageFile: File,
                                    codacyToken: Option[String], codacyTokenFile: Option[String], codacyApiBaseUrl: Option[String]): Unit = {
    implicit val logger: Logger = state.log

    val reportFiles = Seq(
      targetFolder("coverage-report" + File.separator + "cobertura.xml"),
      targetFolder("jacoco" + File.separator + "jacoco.xml")
    )

    getProjectToken(codacyToken, codacyTokenFile).fold[State] {
      logger.error("Project token not defined.")
      state.exit(ok = false)
    } {
      projectToken =>
        Try {
          new GitClient(rootProjectDir).latestCommitUuid()
        }.toOption.fold[State] {
          logger.error("Could not get current commit.")
          state.exit(ok = false)
        } {
          commitUuid =>

            logger.info(s"Preparing coverage data for commit ${commitUuid.take(7)}...")

            CoverageParser.generateReport(reportFiles, rootProjectDir).fold[State] {
              state.exit(ok = false)
            } {
              report =>
                FileUtils.write(codacyCoverageFile, Json.toJson(report).toString())

                logger.info(s"Uploading coverage data...")

                new CodacyAPIClient().postCoverageFile(projectToken, commitUuid, codacyCoverageFile,
                  getApiBaseUrl(codacyApiBaseUrl)).fold[State](
                    error => {
                      logger.error(s"Failed to upload data. Reason: $error")
                      state.exit(ok = false)
                    },
                    response => {
                      logger.success(s"Coverage data uploaded. $response")
                      state
                    })
            }
        }
    }
  }

  private def getApiBaseUrl(codacyApiBaseUrl: Option[String]): String = {
    // Check for an environment variable to override the API URL.
    // If it doesn't exist, try the build options or default to the public API.
    sys.env.get("CODACY_API_BASE_URL")
      .orElse(codacyApiBaseUrl)
      .getOrElse(publicApiBaseUrl)
  }

  private def getProjectToken(codacyProjectToken: Option[String], codacyProjectTokenFile: Option[String]) = {
    sys.env.get("CODACY_PROJECT_TOKEN")
      .orElse(codacyProjectToken)
      .orElse(codacyProjectTokenFile.flatMap(FileUtils.read))
  }

}
