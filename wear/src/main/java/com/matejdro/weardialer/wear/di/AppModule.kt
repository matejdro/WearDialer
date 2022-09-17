package com.matejdro.weardialer.wear.di

import android.app.Application
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
   @Provides
   fun provideMessageClient(application: Application): MessageClient {
      return Wearable.getMessageClient(application)
   }

   @Provides
   fun provideNodeClient(application: Application): NodeClient {
      return Wearable.getNodeClient(application)
   }
}
