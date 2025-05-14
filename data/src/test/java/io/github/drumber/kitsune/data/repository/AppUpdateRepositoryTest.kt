package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.model.appupdate.UpdateCheckResult
import io.github.drumber.kitsune.data.source.jsonapi.appupdate.AppReleaseNetworkDataSource
import io.github.drumber.kitsune.data.source.jsonapi.appupdate.model.NetworkGitHubRelease
import io.github.drumber.kitsune.data.testutils.onSuspend
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@RunWith(Parameterized::class)
class AppUpdateRepositoryTest(
    private val localVersion: String,
    private val remoteVersion: String,
    private val isNewVersion: Boolean
) {

    companion object {
        @JvmStatic
        @Parameters(name = "{index}: localVersion={0}, remoteVersion={1}, isNewVersion={2}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                // localVersion, remoteVersion, isNewVersion
                arrayOf("1.0.0", "1.0.1", true),
                arrayOf("1.0.0", "v1.0.1", true),
                arrayOf("1.0.0", "v1.0.0", false),
                arrayOf("1.0", "v1.0.0", false),
                arrayOf("1.0.0", "v2.0", true),
                arrayOf("2.0", "v1.0.0", false),
                arrayOf("1.2.3", "v1.1.4", false),
                arrayOf("1.0.0", "v1.2.4", true),
                arrayOf("1.0.0", "v1.2.4", true),
                arrayOf("1.0.0", "v1.2.4-beta", true),
            )
        }
    }

    @Test
    fun shouldCheckForUpdates() = runTest {
        // given
        val appReleaseDataSource = mock<AppReleaseNetworkDataSource> {
            onSuspend { getLatestRelease() } doReturn NetworkGitHubRelease(
                version = remoteVersion,
                url = "",
                publishDate = ""
            )
        }
        val repository = AppUpdateRepository(appReleaseDataSource)

        // when
        val updateCheckResult = repository.checkForUpdates(localVersion)

        // then
        if (isNewVersion) {
            assertThat(updateCheckResult).isInstanceOf(UpdateCheckResult.NewVersion::class.java)
            assertThat((updateCheckResult as UpdateCheckResult.NewVersion).release.version)
                .isEqualTo(remoteVersion)
        } else {
            assertThat(updateCheckResult).isInstanceOf(UpdateCheckResult.NoNewVersion::class.java)
        }
    }
}