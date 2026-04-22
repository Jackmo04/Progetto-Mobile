package com.example.cacciaaltesoro

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class CacciaAlTesoroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@CacciaAlTesoroApplication)
            modules(appModule)
        }

        MapsInitializer.initialize(
            applicationContext,
            MapsInitializer.Renderer.LATEST
        ) { renderer ->
            println("The latest version of the renderer is $renderer")
        }
    }
}
