package dev.bhavindesai.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "network_log")
class NetworkLogManager @Inject constructor(@ApplicationContext appContext: Context) {

   private val networkLogDataStore = appContext.dataStore
   private val keyQuotesFetchedAt = longPreferencesKey("quotes_fetched_at")

   suspend fun storeQuotesFetchedTimestamp(timeStamp: Long) {
      networkLogDataStore.edit { settings ->
         settings[keyQuotesFetchedAt] = timeStamp
      }
   }

   val flowQuotesFetchedAt: Flow<Long> = appContext.dataStore.data.map {
         preferences -> preferences[keyQuotesFetchedAt] ?: 0
   }
}