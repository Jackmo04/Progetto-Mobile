package com.example.cacciaaltesoro.data.repositories

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.datetime.LocalDateTime

interface EventRepository {
    suspend fun insertEvent(
        name: String,
        location: LatLng,
        startDateTime: String,
        description: String
    )
}

class EventRepositoryImpl : EventRepository {
    override suspend fun insertEvent(
        name: String,
        location: LatLng,
        startDateTime: String,
        description: String
    ) {
        // TODO: Implement network call to save the event
        Log.d("NEW_EVENT", "Saved new event: $name, $location, $startDateTime, $description")
    }
}
