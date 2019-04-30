package com.globant.watchface.manager

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import com.globant.watchface.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*

class ConfigurationManager {

    companion object {
        private const val DEFAULT_JSON_CONFIGURATION = "{seconds_offset:0,minutes_offset:0,hours_offset:0}"
        private const val BACKGROUND = "bg"
        private const val BG_AMBIENT = "bg_ambient"
        private const val TICK = "tick"
        private const val MINUTE = "minute"
        private const val HOURS = "hours"
        private const val HOURS_AMBIENT = "hrs_ambient"
        private const val MIN_AMBIENT = "min_ambient"
        private const val ITEM_CONFIG = 0
        private const val PNG_QUALITY = 90
    }

    private var configMap: MutableMap<String, Bitmap> = HashMap()
    private lateinit var mJsonObjectConfig: JSONObject
    private lateinit var mJsonFile: File

    private val jsonString : JSONObject get(){
            val builder = StringBuilder()
            var jsonObject = JSONObject()
            try {
                val sdcard = File(Environment.getExternalStorageDirectory().absolutePath + PATH_CONFIGURATION)
                if (!sdcard.exists()) {
                    sdcard.mkdirs()
                }
                mJsonFile = File(sdcard, FILE_CONFIGURATION)
                jsonObject = if (!mJsonFile.exists()) {
                    val writer = FileWriter(mJsonFile)
                    writer.append(DEFAULT_JSON_CONFIGURATION)
                    writer.flush()
                    writer.close()
                    JSONObject(DEFAULT_JSON_CONFIGURATION)
                } else {
                    val bufferedReader = BufferedReader(FileReader(mJsonFile))
                    for (str in bufferedReader.readLine()) {
                        builder.append(str)
                    }
                    JSONObject(builder.toString())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return jsonObject
        }

    fun init(context: Context) {
        fillMap(context.resources)
        mJsonObjectConfig = jsonString
    }

    private fun fillMap(resources: Resources) {
        configMap[BACKGROUND] = BitmapFactory.decodeResource(resources, R.drawable.bg)
        configMap[BG_AMBIENT] = BitmapFactory.decodeResource(resources, R.drawable.bg_ambient)
        configMap[TICK] = BitmapFactory.decodeResource(resources, R.drawable.tick)
        configMap[MINUTE] = BitmapFactory.decodeResource(resources, R.drawable.min)
        configMap[HOURS] = BitmapFactory.decodeResource(resources, R.drawable.hrs)
        configMap[HOURS_AMBIENT] = BitmapFactory.decodeResource(resources, R.drawable.hrs_ambient)
        configMap[MIN_AMBIENT] = BitmapFactory.decodeResource(resources, R.drawable.min_ambient)
    }

    fun updateField(name: Int, bitmap: Bitmap) {
        Constants.resourceKeyMap[name]?.let { configMap[it] = bitmap }
    }

    fun saveConfiguration() {
        for ((key, value) in configMap) {
            try {
                val fos = FileOutputStream(getOutputMediaFile(key))
                value.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, fos)
                fos.close()
                updateConfigurationFile()
            } catch (e: FileNotFoundException) {
                //TODO not implemented
            } catch (e: IOException) {
                //TODO not implemented
            }
        }
    }

    @Throws(IOException::class)
    private fun updateConfigurationFile() {
        val writer = FileWriter(mJsonFile)
        writer.append(mJsonObjectConfig.toString())
        writer.flush()
        writer.close()
    }

    private fun getOutputMediaFile(fileName: String): File {
        val mediaStorageDir = File(Environment.getExternalStorageDirectory().absolutePath + DRAWABLE_CONFIGURATION)
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
        }
        return File(mediaStorageDir.path + File.separator + fileName + PATH_EXTENSION)
    }

    fun getConfigItem(key: String): Int {
        var configItem = ITEM_CONFIG
        try {
            configItem = mJsonObjectConfig.getInt(key)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return configItem
    }
}
