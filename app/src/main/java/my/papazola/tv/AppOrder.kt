package my.papazola.tv

import java.util.Locale

internal object AppOrder {
    fun <T> sort(items: List<T>, label: (T) -> String): List<T> =
        items.sortedBy { label(it).lowercase(Locale.ROOT) }
    fun toggle(current: Set<String>, packageName: String): Set<String> =
        if (packageName in current) current - packageName else current + packageName
}
