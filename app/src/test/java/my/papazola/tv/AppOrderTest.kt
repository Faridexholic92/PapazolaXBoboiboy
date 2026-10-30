package my.papazola.tv
import org.junit.Assert.*
import org.junit.Test

class AppOrderTest {
    @Test fun sortsIgnoringCase() {
        assertEquals(listOf("alpha", "Beta", "zeta"), AppOrder.sort(listOf("zeta", "Beta", "alpha")) { it })
    }
    @Test fun favouriteToggleIsReversibleWithoutMutation() {
        val original = setOf("app.one")
        val added = AppOrder.toggle(original, "app.two")
        assertEquals(setOf("app.one", "app.two"), added)
        assertEquals(original, AppOrder.toggle(added, "app.two"))
        assertEquals(setOf("app.one"), original)
    }
    @Test fun emptyAppsAreSafe() { assertTrue(AppOrder.sort(emptyList<String>()) { it }.isEmpty()) }
}
