package com.restopro.captain.di

import android.content.Context
import androidx.room.Room
import com.restopro.captain.data.local.CaptainDatabase
import com.restopro.captain.data.remote.api.AuthApi
import com.restopro.captain.data.remote.api.AuthInterceptor
import com.restopro.captain.data.remote.api.DynamicBaseUrlInterceptor
import com.restopro.captain.data.remote.api.KotApi
import com.restopro.captain.data.remote.api.MenuApi
import com.restopro.captain.data.remote.api.OrderApi
import com.restopro.captain.data.remote.api.RetryInterceptor
import com.restopro.captain.data.remote.api.SyncApi
import com.restopro.captain.data.remote.api.TableApi
import com.restopro.captain.utils.Constants
import com.restopro.captain.utils.ServerConfigStore
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CaptainDatabase =
        Room.databaseBuilder(context, CaptainDatabase::class.java, Constants.DATABASE_NAME)
            .build()

    @Provides fun provideMenuDao(db: CaptainDatabase) = db.menuDao()
    @Provides fun provideTableDao(db: CaptainDatabase) = db.tableDao()
    @Provides fun provideOrderDao(db: CaptainDatabase) = db.orderDao()
    @Provides fun provideSettingsDao(db: CaptainDatabase) = db.settingsDao()
    @Provides fun provideSyncDao(db: CaptainDatabase) = db.syncDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttp(
        authInterceptor: AuthInterceptor,
        dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor,
        retryInterceptor: RetryInterceptor
    ): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(retryInterceptor)
            .addInterceptor(logger)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson, store: ServerConfigStore): Retrofit =
        Retrofit.Builder()
            .baseUrl(store.blockingBaseUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
    @Provides fun provideMenuApi(retrofit: Retrofit): MenuApi = retrofit.create(MenuApi::class.java)
    @Provides fun provideTableApi(retrofit: Retrofit): TableApi = retrofit.create(TableApi::class.java)
    @Provides fun provideOrderApi(retrofit: Retrofit): OrderApi = retrofit.create(OrderApi::class.java)
    @Provides fun provideKotApi(retrofit: Retrofit): KotApi = retrofit.create(KotApi::class.java)
    @Provides fun provideSyncApi(retrofit: Retrofit): SyncApi = retrofit.create(SyncApi::class.java)
}
