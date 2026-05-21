package com.example.cacciaaltesoro.utils

import androidx.annotation.StringRes
import com.example.cacciaaltesoro.R

enum class EventOrderType(@StringRes val stringResId: Int) {
    NAME(R.string.name_a_z),
    NAME_DESC(R.string.name_z_a),
    START_DATE(R.string.start_date),
    DISTANCE(R.string.distance_from_me),
    EVENT_DURATION(R.string.during_event);
}
