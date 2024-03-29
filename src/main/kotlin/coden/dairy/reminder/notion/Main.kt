package coden.dairy.reminder.notion

import notion.api.v1.NotionClient
import notion.api.v1.model.databases.*
import notion.api.v1.model.pages.PageParent
import notion.api.v1.model.pages.PageProperty
import notion.api.v1.request.search.SearchRequest
import java.io.Closeable
import java.nio.file.Path
import java.time.LocalDate
import java.util.stream.Stream
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

interface DairyRepository {
    fun entries(): Stream<DairyEntry>

    fun get(index: Int): DairyEntry
    fun first(): DairyEntry
    fun last(): DairyEntry

    fun insert(entry: DairyEntry)
    fun delete(index: Int)

    fun create()
    fun purge()

    fun clear()
}

data class DairyEntry(
    val month: LocalDate,
    val description: String,
)

data class NotionPath(private val absolute: String) {

    private val path: Path = Path(absolute)

    fun path(): String {
        return path.toString()
    }

    fun filename(): String {
        return path.fileName.toString()
    }

    fun parents(): List<NotionPath> {
        val parent = parent() ?: return emptyList()
        return mutableListOf(parent) + parent.parents()
    }

    fun parent(): NotionPath? {
        return path.parent?.let { NotionPath(it.toString()) }
    }

    fun isRoot(): Boolean {
        return parents().isEmpty()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NotionPath) return false

        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}

class NotionDairyTable(
    private val client: NotionClient,
    private val table: NotionPath
) : DairyRepository,
    Closeable by client {

    private val parentPageId = findPage()

    fun findPage() = (client.search(
        query = table.filename(),
        filter = SearchRequest.SearchFilter("page", property = "object")
    )
        .results
        .filter { it.asPage().properties["title"]?.title?.get(0)?.plainText != null}
        .firstOrNull { it.asPage().properties["title"]?.title?.get(0)?.plainText == table.filename() }
        ?.id
        ?: throw IllegalStateException("Create database $table and add connection"))

    override fun entries(): Stream<DairyEntry> {
        TODO("Not yet implemented")
    }

    override fun get(index: Int): DairyEntry {
        TODO("Not yet implemented")
    }

    override fun first(): DairyEntry {
        TODO("Not yet implemented")
    }

    override fun last(): DairyEntry {
        TODO("Not yet implemented")
    }

    override fun insert(entry: DairyEntry) {
        TODO("Not yet implemented")
    }

    override fun delete(index: Int) {
        TODO("Not yet implemented")
    }

    override fun create() {
        client.createDatabase(
            parent = DatabaseParent.workspace(),
            title = "Table".asDatabaseRichText(),
            properties = mapOf(
                "Title" to  TitlePropertySchema()
            )
        )
    }

    override fun purge() {
        TODO("Not yet implemented")
    }


    private fun findParent(path: NotionPath): PageParent {
        return PageParent.workspace()
    }


    override fun clear() {
        TODO("Not yet implemented")
    }
}

fun main() {
    NotionClient(token = "secret_onegyirr9ANiY7fs3lp5uUjNjXuehh8AxrIzbVdPNGJ").use { client ->
        // Find the "Test Database" from the list
//        val database = client
//            .search(
//                query = "Test Database",
//                filter = SearchRequest.SearchFilter("database", property = "object")
//            )
//            .results
//            .find { it.asDatabase().properties.containsKey("Severity") }
//            ?.asDatabase()
//            ?: error("Create a database named 'Test Database' and invite this app's user!")
        // Alternatively if you know the UUID of the Database, use `val database = client.retrieveDatabase("...")`.

        val database = client.retrieveDatabase("c14f4828-2090-4e5b-aa64-e290a18a181d")

//        database.properties[""]
        // All the options for "Severity" property (select type)
//        val severityOptions = database.properties["Severity"]!!.select!!.options!!
//         All the options for "Tags" property (multi_select type)
//        val tagOptions = database.properties["Tags"]!!.multiSelect!!.options!!
//         A user object for "Assignee" property (people type)
//        val assignee = client.listUsers().results.first() // Just picking a random user.

        // Create a new page in the database
        val newPage = client.createPage(
            // Use the "Test Database" as this page's parent
            parent = PageParent.database(database.id),
            // Set values to the page's properties
            // (Values of referenced options, people, and relations must be pre-defined before this API call!)
            properties = mapOf(
                "Month" to PageProperty(title = "2022/01".asRichText()),
                "Description" to PageProperty(richText = "hello".asRichText()),
                "URL" to PageProperty(url = "https://www.example.com"),
            )
        )

//        // Properties can be addressed by their ID too.
//        val severityId = newPage.properties["Severity"]!!.id
//
//        // Update properties in the page
//        val updatedPage = client.updatePage(
//            pageId = newPage.id,
//            // Update only "Severity" property
//            properties = mapOf(
//                severityId to PageProperty(select = severityOptions.single { it.name == "Medium" }),
//            )
//        )
//
//        // Fetch the latest data of the page
        val retrievedPage = client.retrievePage(newPage.id)

        println(retrievedPage)
    }
}

private fun String.asRichText(): List<PageProperty.RichText> =
    listOf(PageProperty.RichText(text = PageProperty.RichText.Text(content = this)))

private fun String.asDatabaseRichText(): List<DatabaseProperty.RichText> =
    listOf(DatabaseProperty.RichText(text = DatabaseProperty.RichText.Text(content = this)))