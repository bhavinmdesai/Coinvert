package dev.bhavindesai.coinvert.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.bhavindesai.coinvert.BuildConfig
import dev.bhavindesai.data.local.CoinvertDataDao
import dev.bhavindesai.data.local.CoinvertDatabase
import dev.bhavindesai.data.preferences.NetworkLogManager
import dev.bhavindesai.data.remote.services.CurrencyLayerService
import dev.bhavindesai.data.repositories.CoinvertRepository
import dev.bhavindesai.data.repositories.CoinvertRepositoryImpl
import dev.bhavindesai.data.utils.InternetUtil
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreManager {

   @Provides
   fun providesNetworkLogManager(@ApplicationContext appContext: Context) =
      NetworkLogManager(appContext)
}

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

   @Provides
   fun providesCoinvertDatabase(application: Application): CoinvertDatabase = Room.databaseBuilder(
      application.applicationContext,
      CoinvertDatabase::class.java,
      "coinvert_database"
   ).build()

   @Provides
   fun providesWeatherDataDao(coinvertDatabase: CoinvertDatabase): CoinvertDataDao {
      return coinvertDatabase.coinvertDataDao()
   }
}

@Module
@InstallIn(SingletonComponent::class)
internal object ServiceModule {

   @Provides
   fun providesCurrencyLayerService(retrofit: Retrofit): CurrencyLayerService =
      retrofit.create(CurrencyLayerService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
internal object RepositoryModule {

   @Provides
   fun providesCoinvertRepository(
      currencyLayerService: CurrencyLayerService,
      internetUtil: InternetUtil,
      coinvertDataDao: CoinvertDataDao,
      networkLogManager: NetworkLogManager
   ): CoinvertRepository {
      return CoinvertRepositoryImpl(currencyLayerService, internetUtil, coinvertDataDao, networkLogManager)
   }
}

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

   @Provides
   fun providesInternetUtil(application: Application): InternetUtil = InternetUtil.apply {
      init(application)
   }

   @Provides
   fun providesGsonConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()

   @Provides
   fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BASIC
   }

   @Provides
   fun providesOkHttp(
      httpLoggingInterceptor: HttpLoggingInterceptor,
   ): OkHttpClient {
      return OkHttpClient()
         .newBuilder()
         .addInterceptor(httpLoggingInterceptor)
         .build()
   }

   @Provides
   fun providesRetrofit(okHttpClient: OkHttpClient, gsonConverterFactory: GsonConverterFactory): Retrofit =
      Retrofit.Builder()
         .client(okHttpClient)
         .baseUrl(BuildConfig.BASE_URL)
         .addConverterFactory(gsonConverterFactory)
         .build()
}
