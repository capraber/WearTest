package com.globant.watchface

import java.util.*

const val SECOND_CHOOSER = 1
const val BACKGROUND_CHOOSER = 2
const val HOUR_CHOOSER = 3
const val MINUTE_CHOOSER = 4
const val BACKGROUND_AMBIENT = 5
const val HOUR_AMBIENT = 6
const val MINUTE_AMBIENT = 7
const val PATH_CONFIGURATION = "/configuration/"
const val FILE_CONFIGURATION = "configuration.json"
const val DRAWABLE_CONFIGURATION = "/drawable-nodpi/"
const val PATH_EXTENSION = ".png"

object Constants {

    var resourceKeyMap: MutableMap<Int, String> = HashMap()

    init {
        resourceKeyMap[SECOND_CHOOSER] = "tick"
        resourceKeyMap[BACKGROUND_CHOOSER] = "bg"
        resourceKeyMap[HOUR_CHOOSER] = "hrs"
        resourceKeyMap[MINUTE_CHOOSER] = "min"
        resourceKeyMap[BACKGROUND_AMBIENT] = "bg_ambient"
        resourceKeyMap[HOUR_AMBIENT] = "hrs_ambient"
        resourceKeyMap[MINUTE_AMBIENT] = "min_ambient"
    }
}
