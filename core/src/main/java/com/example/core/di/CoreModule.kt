package com.example.core.di

import androidx.room.Room
import com.example.core.data.Repository
import com.example.core.data.local.AppDatabase
import com.example.core.data.local.LocalSource
import com.example.core.data.remote.ApiConfig
import com.example.core.data.remote.RemoteSource
import com.example.core.domain.IRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    factory {
        get<AppDatabase>().bookmarkDao()
    }
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "app_db").build()
    }
}

val networkModule = module {
    single {
        ApiConfig.getApiService()
    }
}

val repositoryModule = module {
    single { LocalSource(get()) }
    single { RemoteSource(get()) }
    single<IRepository> {
        Repository(
            get(), get()
        )
    }
}