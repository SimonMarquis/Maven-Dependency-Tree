#!/usr/bin/env kotlin

@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("net.mbonnin.xoxo:xoxo:0.2")

import xoxo.XmlDocument
import xoxo.XmlElement
import xoxo.toXmlDocument
import xoxo.walkElements
import java.net.URL
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

//region Args
val help = "--help" in args || "-h" in args
val legend = "--no-legend" !in args
val quiet = "--quiet" in args || "-q" in args
//endregion

//region Main
Cli.run {
    if (help) return@run help()
    if (!quiet) printName()
    if (!quiet) printArgs(args)
    val artifact = askForArtifact(args)
    val repositories = askForRepositories(args)
    val tree = Resolver(artifact, repositories)()
    Printer(tree)(artifact)
}.also { exitProcess(0) }
//endregion

//region Classes
data class Artifact(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val scope: String? = null
) {

    val isResolvable = setOf(groupId, artifactId, version).none(String::isBlank)
            && version.firstOrNull() !in setOf('[', '(')

    fun pom(repo: URL) = repo.toString().trimEnd('/')
        .plus("/${groupId.replace(".", "/")}")
        .plus("/$artifactId/$version/$artifactId-$version.pom")
        .let(::URL)

    override fun toString() = buildString {
        append("$groupId:$artifactId:$version")
        if (scope != null) append(" ($scope)")
    }

    companion object {
        fun fromString(input: String?): Artifact = runCatching {
            input.orEmpty().split(":").let { (groupId, artifactId, version) ->
                Artifact(groupId = groupId, artifactId = artifactId, version = version)
            }
        }.getOrElse { throw IllegalArgumentException("Failed to parse artifact: [$input]", it) }
    }

}

@OptIn(ExperimentalTime::class)
class Resolver(private val artifact: Artifact, private val repositories: List<URL>) {
    private val cache = mutableMapOf<URL, Result<List<Artifact>>>()

    operator fun invoke(): Map<Artifact, List<Artifact>?> {
        if (!quiet) println("📡 Crawling POM files…")
        val (tree, duration) = measureTimedValue { artifact.resolve(repositories) }
        if (!quiet) println("⏱ Total duration: $duration\n")
        return tree
    }

    private fun Artifact.resolve(repos: List<URL>): Map<Artifact, List<Artifact>?> = buildMap {
        val queue = ArrayDeque(listOf(this@resolve))
        while (queue.isNotEmpty()) {
            val artifact = queue.removeFirst()
            val dependencies = getOrPut(artifact) {
                if (!artifact.isResolvable) emptyList()
                else artifact.resolveDependencies(repos).getOrNull()
            }
            queue.addAll(dependencies.orEmpty().distinct().filterNot(::contains))
        }
    }

    private fun Artifact.resolveDependencies(repositories: List<URL>): Result<List<Artifact>> =
        repositories.asSequence()
            .map { resolveDependencies(it) }
            .firstOrNull { it.isSuccess }
            ?: Result.failure(Exception("Not found in any of the repositories."))

    private fun Artifact.resolveDependencies(repository: URL): Result<List<Artifact>> {
        if (!isResolvable) return Result.failure(Exception("Not resolvable"))
        val pom = pom(repository)
        return cache.getOrPut(pom) { readAndParse(pom) }
    }

    private fun readAndParse(pom: URL): Result<List<Artifact>> = measureTimedValue {
        pom.runCatching(URL::readText)
    }.let { (result, duration) ->
        result
            .onSuccess { if (!quiet) println("✅ $pom in $duration".let(Cli::green)) }
            .onFailure { if (!quiet) println("❌ $pom ${it.javaClass.simpleName} in $duration".let(Cli::red)) }
            .mapCatching(String::toXmlDocument)
            .map { it.dependencies() }
    }

    private fun XmlDocument.dependencies() = root
        .namedOrNull("dependencies")?.walkElements().orEmpty()
        .filter { it.name == "dependency" }
        .map { it.childElements.toArtifact(this) }
        .toList()

    private fun List<XmlElement>.toArtifact(document: XmlDocument) = Artifact(
        groupId = named("groupId").resolvedTextContext(document),
        artifactId = named("artifactId").resolvedTextContext(document),
        version = namedOrNull("version")?.resolvedTextContext(document).orEmpty(),
        scope = namedOrNull("scope")?.resolvedTextContext(document),
    )

    // Does not support parent pom
    private fun XmlElement.resolvedTextContext(document: XmlDocument) =
        textContent.replace("""\$\{(.+?)\}""".toRegex()) {
            document.findProperty(it.groupValues[1]).orEmpty()
        }

    private fun XmlDocument.findProperty(name: String): String? = root
        .namedOrNull("properties")
        ?.namedOrNull(name)?.textContent

}

class Printer(private val tree: Map<Artifact, List<Artifact>?>) {

    operator fun invoke(root: Artifact) {
        tree.printRecursive(root)
        if (legend) legend()
    }

    private fun Map<Artifact, List<Artifact>?>.printRecursive(
        artifact: Artifact,
        printed: MutableSet<Artifact> = mutableSetOf(),
        seen: List<Artifact> = emptyList(),
        indent: String = "",
        isLast: Boolean = true,
    ) {
        print(indent + (if (seen.isEmpty()) "" else if (isLast) "└─ " else "├─ ") + artifact)
        val dependencies = get(artifact)
        when {
            artifact in seen -> return println(" 🔁")
            artifact in printed -> return println(" ↩️")
            !artifact.isResolvable -> return println(" ⛔")
            dependencies == null -> return println(" 💀")
            else -> println()
        }
        printed += artifact
        val childSeen by lazy { seen + artifact }
        val childIndent by lazy { indent + if (seen.isEmpty()) "" else if (isLast) "   " else "│  " }
        fun isLast(index: Int) = index == dependencies.lastIndex
        dependencies.forEachIndexed { idx, dep -> printRecursive(dep, printed, childSeen, childIndent, isLast(idx)) }
    }

    private fun legend() = """

        🔣 Legend:
        - 🔁 Cyclic dependency
        - ↩️ Already printed
        - ⛔ Non resolvable
        - 💀 Failed to resolve
    """.trimIndent().let(::println)

}
//endregion

//region Cli
object Cli {

    fun printName() = println("🌲 Maven Dependency Tree\n")

    fun printArgs(args: Array<String>) = args.toList().takeUnless { it.isEmpty() }?.let { println("⚙️ Arguments: $it") }

    private fun <T> ask(question: String, transform: (String?) -> T): T {
        while (true) {
            print("?".let(Cli::green) + " $question ")
            kotlin.runCatching {
                return transform(readLine())
            }.getOrElse(::println)
        }
    }

    fun askForArtifact(args: Array<String>): Artifact = args
        .firstOrNull { !it.startsWith("-") }?.let(Artifact.Companion::fromString)
        ?: ask("Artifact (groupId:artifactId:version)", Artifact.Companion::fromString)

    fun askForRepositories(args: Array<String>): List<URL> = args
        .filter { it.startsWith("--repository=") }
        .map { it.removePrefix("--repository=").removeSurrounding("\"") }
        .also { if (it.any(String::isBlank)) error("Invalid repository!") }
        .map(::URL)
        .ifEmpty {
            ask("Optional repositories (URLs)") { input ->
                input?.takeUnless(String::isNullOrBlank)?.split(" ").orEmpty()
                    .ifEmpty(Defaults::repositories)
                    .map(::URL)
            }
        }

    fun help() = println(
        """
        Usage: tree.main.kts [ARGUMENTS] [OPTIONS]
        Arguments: 
            artifact [groupId:artifactId:version] -> Name of the artifact to explore (optional)
        Options: 
            --help, -h          -> Usage info
            --quiet, -q [false] -> Hide debug message
            --no-legend [false] -> Hide the legend
            --repository=<URL>  -> Maven repository (vararg) defaults to Central & Google
        """.trimIndent()
    )

    fun red(string: String) = "\u001B[31m$string\u001B[0m"
    fun green(string: String) = "\u001B[32m$string\u001B[0m"

    object Defaults {
        val repositories = listOf("https://repo1.maven.org/maven2")
    }
}
//endregion

//region Extensions
fun XmlElement.named(name: String) = walkElements().asIterable().named(name)
fun XmlElement.namedOrNull(name: String) = walkElements().asIterable().namedOrNull(name)
fun Iterable<XmlElement>.named(name: String) = first { it.name == name }
fun Iterable<XmlElement>.namedOrNull(name: String) = firstOrNull { it.name == name }
//endregion
