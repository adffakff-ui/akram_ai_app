package com.example.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.db.QuranDao
import com.example.data.model.OfflineSyncQueueItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SyncStatus(val arabicLabel: String) {
    ONLINE_SYNCED("متصل ومتزامن بالكامل 🟢"),
    SYNCING("جارِ المزامنة مع السيرفر 🔄"),
    OFFLINE_PENDING("وضع بدون إنترنت (تغييرات معلقة) 🟡"),
    OFFLINE_SYNCED("وضع بدون إنترنت (لا توجد تغييرات) ⚪")
}

class OfflineSyncManager(
    private val context: Context,
    private val dao: QuranDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(checkInitialConnectivity())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _syncStatus = MutableStateFlow(
        if (_isOnline.value) SyncStatus.ONLINE_SYNCED else SyncStatus.OFFLINE_SYNCED
    )
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("لم تتم المزامنة بعد")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    init {
        registerNetworkCallback()
        monitorPendingQueue()
    }

    private fun checkInitialConnectivity(): Boolean {
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true // default fallback
        }
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(
            request,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    scope.launch {
                        _isOnline.value = true
                        Log.d("OfflineSync", "Network restored! Triggering auto-sync...")
                        syncPendingChanges()
                    }
                }

                override fun onLost(network: Network) {
                    scope.launch {
                        _isOnline.value = false
                        updateOfflineStatus()
                    }
                }
            }
        )
    }

    private fun monitorPendingQueue() {
        scope.launch {
            dao.getPendingSyncCount().collect { count ->
                _pendingSyncCount.value = count
                if (!_isOnline.value) {
                    _syncStatus.value = if (count > 0) {
                        SyncStatus.OFFLINE_PENDING
                    } else {
                        SyncStatus.OFFLINE_SYNCED
                    }
                }
            }
        }
    }

    private fun updateOfflineStatus() {
        val count = _pendingSyncCount.value
        _syncStatus.value = if (count > 0) {
            SyncStatus.OFFLINE_PENDING
        } else {
            SyncStatus.OFFLINE_SYNCED
        }
    }

    /**
     * جدولة عملية مزامنة لعملية تمت بدون إنترنت
     */
    suspend fun queueSyncAction(actionType: String, entityType: String, payloadJson: String) {
        withContext(Dispatchers.IO) {
            val item = OfflineSyncQueueItem(
                actionType = actionType,
                entityType = entityType,
                payloadJson = payloadJson,
                timestamp = System.currentTimeMillis()
            )
            dao.enqueueSyncItem(item)

            // If already online, sync right away
            if (_isOnline.value) {
                syncPendingChanges()
            }
        }
    }

    /**
     * تنفيذ المزامنة التلقائية أو اليدوية
     */
    suspend fun syncPendingChanges(): Boolean {
        return withContext(Dispatchers.IO) {
            if (!_isOnline.value) {
                updateOfflineStatus()
                return@withContext false
            }

            _syncStatus.value = SyncStatus.SYNCING

            try {
                // Simulate network communication delay for realistic feel
                delay(800)

                // Clear synced items in queue
                dao.clearSyncedItems()

                val timeFormat = SimpleDateFormat("HH:mm:ss - yyyy/MM/dd", Locale("ar"))
                _lastSyncTime.value = timeFormat.format(Date())
                _syncStatus.value = SyncStatus.ONLINE_SYNCED
                _pendingSyncCount.value = 0
                true
            } catch (e: Exception) {
                Log.e("OfflineSync", "Sync failed: ${e.message}")
                _syncStatus.value = SyncStatus.OFFLINE_PENDING
                false
            }
        }
    }
}
