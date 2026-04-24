package com.example.cacciaaltesoro.data.repositories

import android.util.Log
import com.google.android.gms.maps.model.LatLng

interface MapEditorRepository {
    suspend fun saveMarkers(eventId: String, markers: List<LatLng>)
    suspend fun getMarkers(eventId: String): List<LatLng>
}

// TODO Add SupabaseClient as a dependency
class MapEditorRepositoryImpl() : MapEditorRepository {
    override suspend fun saveMarkers(eventId: String, markers: List<LatLng>) {
        // TODO
        Log.d("MAP_EDITOR", "Saved $markers for event $eventId")
    }

    override suspend fun getMarkers(eventId: String): List<LatLng> {
        // TODO
        Log.d("MAP_EDITOR", "Got markers for event $eventId")
        return emptyList()
    }
}
