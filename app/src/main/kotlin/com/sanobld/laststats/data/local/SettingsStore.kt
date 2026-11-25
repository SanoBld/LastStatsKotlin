package com.sanobld.laststats.data.local

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.toMutablePreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

// ── Typed preference descriptors ─────────────────────────────────────────────
// Names are the same `ls_*` keys the Flutter version used, so old settings,
// backups and restores keep working.

sealed class Pref<T>(val name: String, val default: T) {
    abstract fun read(p: Preferences): T
    abstract fun write(p: MutablePreferences, v: T)
    abstract fun remove(p: MutablePreferences)
    abstract fun has(p: Preferences): Boolean
}

class BoolPref(name: String, default: Boolean = false) : Pref<Boolean>(name, default) {
    val key = booleanPreferencesKey(name)
    override fun read(p: Preferences) = p[key] ?: default
    override fun write(p: MutablePreferences, v: Boolean) { p[key] = v }
    override fun remove(p: MutablePreferences) { p.remove(key) }
    override fun has(p: Preferences) = p.contains(key)
}

/** Int stored as a long, because Flutter stored every int as a long. */
class IntPref(name: String, default: Int = 0) : Pref<Int>(name, default) {
    val key = longPreferencesKey(name)
    override fun read(p: Preferences) = p[key]?.toInt() ?: default
    override fun write(p: MutablePreferences, v: Int) { p[key] = v.toLong() }
    override fun remove(p: MutablePreferences) { p.remove(key) }
    override fun has(p: Preferences) = p.contains(key)
}

class LongPref(name: String, default: Long = 0L) : Pref<Long>(name, default) {
    val key = longPreferencesKey(name)
    override fun read(p: Preferences) = p[key] ?: default
    override fun write(p: MutablePreferences, v: Long) { p[key] = v }
    override fun remove(p: MutablePreferences) { p.remove(key) }
    override fun has(p: Preferences) = p.contains(key)
}

class DoublePref(name: String, default: Double = 0.0) : Pref<Double>(name, default) {
    val key = doublePreferencesKey(name)
    override fun read(p: Preferences) = p[key] ?: default
    override fun write(p: MutablePreferences, v: Double) { p[key] = v }
    override fun remove(p: MutablePreferences) { p.remove(key) }
    override fun has(p: Preferences) = p.contains(key)
}

class StringPref(name: String, default: String = "") : Pref<String>(name, default) {
    val key = stringPreferencesKey(name)
    override fun read(p: Preferences) = p[key] ?: default
    override fun write(p: MutablePreferences, v: String) { p[key] = v }
    override fun remove(p: MutablePreferences) { p.remove(key) }
    override fun has(p: Preferences) = p.contains(key)
}

/** Ordered list of strings, stored as a JSON array (same format the migration produces). */
class ListPref(name: String, default: List<String> = emptyList()) : Pref<List<String>>(name, default) {
    val key = stringPreferencesKey(name)
    override fun read(p: Preferences): List<String> {
        val raw = p[key] ?: return default
        return try {
            Json.decodeFromString(ListSerializer(String.serializer()), raw)
        } catch (_: Exception) {
            default
        }
    }
    override fun write(p: MutablePreferences, v: List<String>) {
        p[key] = Json.encodeToString(ListSerializer(String.serializer()), v)
    }
    override fun remove(p: MutablePreferences) { p.remove(key) }
    override fun has(p: Preferences) = p.contains(key)
}

// ── Old Flutter settings ─────────────────────────────────────────────────────

private const val FLUTTER_FILE = "FlutterSharedPreferences"
private const val FLUTTER_PREFIX = "flutter."
private const val LIST_PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu"
private const val DOUBLE_PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBEb3VibGUu"
private const val BIG_INT_PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBCaWdJbnRlZ2Vy"

/**
 * One-time import of the settings written by the Flutter version of the app
 * (same application id), so an update keeps every setting.
 */
class FlutterPrefsMigration(private val context: Context) : DataMigration<Preferences> {
    private val doneKey = booleanPreferencesKey("ls_flutter_migrated")

    private fun old() = context.getSharedPreferences(FLUTTER_FILE, Context.MODE_PRIVATE)

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        currentData[doneKey] != true && old().all.keys.any { it.startsWith(FLUTTER_PREFIX) }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val m = currentData.toMutablePreferences()
        for ((k, v) in old().all) {
            if (!k.startsWith(FLUTTER_PREFIX)) continue
            val name = k.removePrefix(FLUTTER_PREFIX)
            when (v) {
                is Boolean -> m[booleanPreferencesKey(name)] = v
                is Long -> m[longPreferencesKey(name)] = v
                is Int -> m[longPreferencesKey(name)] = v.toLong()
                is Float -> m[doublePreferencesKey(name)] = v.toDouble()
                is String -> when {
                    v.startsWith(LIST_PREFIX) -> m[stringPreferencesKey(name)] = v.removePrefix(LIST_PREFIX)
                    v.startsWith(DOUBLE_PREFIX) ->
                        v.removePrefix(DOUBLE_PREFIX).toDoubleOrNull()?.let { m[doublePreferencesKey(name)] = it }
                    v.startsWith(BIG_INT_PREFIX) ->
                        v.removePrefix(BIG_INT_PREFIX).toLongOrNull()?.let { m[longPreferencesKey(name)] = it }
                    else -> m[stringPreferencesKey(name)] = v
                }
                is Set<*> -> m[stringPreferencesKey(name)] =
                    Json.encodeToString(ListSerializer(String.serializer()), v.filterIsInstance<String>())
            }
        }
        m[doneKey] = true
        return m
    }

    override suspend fun cleanUp() {}
}

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "laststats_settings",
    produceMigrations = { ctx -> listOf(FlutterPrefsMigration(ctx)) },
)

// ── Store ────────────────────────────────────────────────────────────────────

/**
 * All app settings. Reads are instant (an in-memory snapshot), writes update the
 * snapshot at once and are saved to disk in order, one after the other.
 */
class SettingsStore private constructor(
    private val ds: DataStore<Preferences>,
    private val scope: CoroutineScope,
    initial: Preferences,
) {
    private val _snapshot = MutableStateFlow(initial)
    val snapshot: StateFlow<Preferences> = _snapshot

    private val queue = Channel<(MutablePreferences) -> Unit>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (op in queue) {
                try {
                    ds.edit { op(it) }
                } catch (_: Exception) {
                }
            }
        }
    }

    fun <T> get(pref: Pref<T>): T = pref.read(_snapshot.value)

    fun <T> contains(pref: Pref<T>): Boolean = pref.has(_snapshot.value)

    fun <T> flow(pref: Pref<T>): Flow<T> = _snapshot.map { pref.read(it) }.distinctUntilChanged()

    fun <T> stateFlow(pref: Pref<T>, scope: CoroutineScope): StateFlow<T> =
        flow(pref).stateIn(scope, SharingStarted.Eagerly, get(pref))

    fun <T> put(pref: Pref<T>, value: T) = mutate { pref.write(it, value) }

    fun <T> remove(pref: Pref<T>) = mutate { pref.remove(it) }

    /** Several writes saved together. */
    fun putAll(block: (MutablePreferences) -> Unit) = mutate(block)

    /** Every stored key and value, for backups. */
    fun asMap(): Map<String, Any> =
        _snapshot.value.asMap().entries.associate { it.key.name to it.value }

    /** Removes every stored setting. */
    fun clearAll() = mutate { m ->
        @Suppress("UNCHECKED_CAST")
        m.asMap().keys.toList().forEach { m.remove(it as Preferences.Key<Any>) }
    }

    /** Waits until everything written so far is on disk. */
    suspend fun flush() {
        val done = CompletableDeferred<Unit>()
        queue.send { done.complete(Unit) }
        done.await()
        ds.edit { }
    }

    private fun mutate(op: (MutablePreferences) -> Unit) {
        _snapshot.update { cur -> cur.toMutablePreferences().also(op) }
        queue.trySend(op)
    }

    companion object {
        /** Loads the saved settings once. Call from Application.onCreate. */
        suspend fun create(context: Context, scope: CoroutineScope): SettingsStore {
            val ds = context.applicationContext.settingsDataStore
            return SettingsStore(ds, scope, ds.data.first())
        }
    }
}

/** Compose state of one setting. */
@Composable
fun <T> SettingsStore.collectAsState(pref: Pref<T>): State<T> {
    val f = remember(pref) { flow(pref) }
    return f.collectAsState(initial = get(pref))
}
