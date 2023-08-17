package com.rikkimikki.telegramgallery3.injection

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import com.rikkimikki.telegramgallery3.feature_node.data.data_source.InternalDatabase
import com.rikkimikki.telegramgallery3.feature_node.data.repository.MediaRepositoryImpl
import com.rikkimikki.telegramgallery3.feature_node.data.telegram.TelegramCredentials
import com.rikkimikki.telegramgallery3.feature_node.domain.repository.MediaRepository
import com.rikkimikki.telegramgallery3.feature_node.domain.use_case.MediaUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): InternalDatabase {
        return Room.databaseBuilder(app, InternalDatabase::class.java, InternalDatabase.NAME)
            .build()
    }

    @Provides
    @Singleton
    fun provideMediaUseCases(repository: MediaRepository, @ApplicationContext context: Context): MediaUseCases {
        return MediaUseCases(context, repository)
    }

    @Provides
    @Singleton
    fun provideMediaRepository(
        contentResolver: ContentResolver,
        database: InternalDatabase,
        telegramCredentials: TelegramCredentials
    ): MediaRepository {
        return MediaRepositoryImpl(contentResolver, database,telegramCredentials)
    }

    @Provides
    @Singleton
    fun provideTelegramCredentials(
        @ApplicationContext context: Context
    ): TelegramCredentials {
        return TelegramCredentials(context)
    }
}
