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

/**
 * Apple targets need a macOS host to *link* a .framework, and Kotlin/Native
 * publishes no linux-aarch64 host compiler at all -- so on an aarch64 Linux dev
 * box (the profile this template was bootstrapped on), configuring an iOS
 * target fails before any of your code even compiles. Keep iOS opt-in with
 * -Pkmptemplate.enableIos=true on a Mac or in macOS CI. See CLAUDE.md.
 */
internal val Project.iosEnabled: Boolean
    get() = providers.gradleProperty("kmptemplate.enableIos").orNull?.toBoolean() ?: false

/** Reverse-DNS namespace derived from the module path, e.g. :core:model -> com.kmptemplate.core.model */
internal val Project.moduleNamespace: String
    get() = "com.kmptemplate." + path.removePrefix(":").replace(":", ".").replace("-", "")
