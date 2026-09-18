import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias).get().requiredVersion

internal fun VersionCatalog.lib(alias: String) = findLibrary(alias).get()

internal fun VersionCatalog.bundle(alias: String) = findBundle(alias).get()

/** Enable Apple targets by default on macOS; Linux ARM64 cannot link Apple frameworks. */
internal val Project.iosEnabled: Boolean
    get() = providers.gradleProperty("kmptemplate.enableIos").orNull?.toBoolean() ?: (System.getProperty("os.name") == "Mac OS X")

/** Reverse-DNS namespace derived from the module path, keeping `common` as a filesystem boundary rather than a package. */
internal val Project.moduleNamespace: String
    get() = "com.kmptemplate." + path
        .removePrefix(":common:")
        .removePrefix(":")
        .replace(":", ".")
        .replace("-", "")
