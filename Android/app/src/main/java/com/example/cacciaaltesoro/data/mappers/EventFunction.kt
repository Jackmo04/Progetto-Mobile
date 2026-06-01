package com.example.cacciaaltesoro.data.mappers

import android.content.Context
import android.location.Location
import com.example.cacciaaltesoro.BuildConfig
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.data.domain.Event
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun Event.getImageUrl() : String{
    return "https://maps.googleapis.com/maps/api/staticmap?" +
            "center=${lat},${lon}" +
            "&zoom=15" +
            "&size=600x300" +
            "&markers=color:red%7C${lat},${lon}" +
            "&key=${BuildConfig.MAPS_KEY}"
}

@OptIn(ExperimentalTime::class)
fun Event.getStartTime(): String {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
        .withLocale(Locale.getDefault())

    return Instant.ofEpochSecond(startTime.epochSeconds)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

@OptIn(ExperimentalTime::class)
fun Event.isAvailableTheEvent(): Boolean {
    val now = Clock.System.now().toEpochMilliseconds()
    val extraTime = 15 * 60 * 1000
    return (startTime.epochSeconds * 1000L - now - extraTime) <= 0 && (endTime.epochSeconds *1000L - now)>=0
}

@OptIn(ExperimentalTime::class)
fun Event.isEditableTheEvent(): Boolean {
    val now = Clock.System.now().toEpochMilliseconds()
    return (startTime.epochSeconds * 1000L) > now
}

@OptIn(ExperimentalTime::class)
fun Event.getGameDuration(context: Context): String {
    val diffInSeconds = endTime.epochSeconds - startTime.epochSeconds
    val minutes = diffInSeconds / 60

    return if (minutes >= 60) {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        if (remainingMinutes > 0) "$hours h e $remainingMinutes min" else "$hours h"
    } else {
        "$minutes"+ context.getString(R.string.minute)
    }
}

@OptIn(ExperimentalTime::class)
fun Event.shareTextBuilder(resolvedAddress: String): String {
    val dateTime = getStartTime()
    return """
        *📍 NUOVA CACCIA AL TESORO!*
        
        Ciao! Sei stato invitato a partecipare a un nuovo evento. Ecco i dettagli:
        
        *🏆 Nome:* ${name}
        *📅 Data:* $dateTime
        *📍 Punto di ritrovo:* $resolvedAddress
        
        *Codice di accesso:* `${code}`
        
        ---
        
        *📲 Come partecipare:*
        Scarica l'app, inserisci il codice qui sopra e preparati a trovare tutti i Tag!
        
        *Mappa:* https://maps.google.com/?q=${lat},${lon}
    """.trimIndent()
}

fun Event.getDistanceFromPointString(myLocation: Location?): String {

    val eventLocation = Location("").apply {
        latitude = lat
        longitude = lon
    }
    val distanceInMeters = getDistanceFromPoint(myLocation) ?: return ""
    return if (distanceInMeters < 1000) {
        "${distanceInMeters.toInt()} m"
    } else {
        "%.1f km".format(distanceInMeters / 1000)
    }
}

fun Event.getDistanceFromPoint(myLocation: Location?): Float? {
    if (myLocation == null) return null
    val eventLocation = Location("").apply {
        latitude = lat
        longitude = lon
    }
 return  myLocation.distanceTo(eventLocation)

}