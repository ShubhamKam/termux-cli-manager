package com.termux.climanager.di

import android.content.Context
import com.termux.climanager.repository.CLIManagerRepository
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
    @Singleton
    fun provideCLIManagerRepository(
        @ApplicationContext context: Context
    ): CLIManagerRepository {
        return CLIManagerRepository(context)
    }
}