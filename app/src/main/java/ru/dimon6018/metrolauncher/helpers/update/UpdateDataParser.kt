package ru.dimon6018.metrolauncher.helpers.update

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import java.io.IOException
import java.io.InputStream

class UpdateDataParser {

    fun parse(`in`: InputStream) {
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(`in`, null)
            parser.nextTag()
            readPreferences(parser)
            `in`.close()
        } catch (e: XmlPullParserException) {
            Log.e("update_parser", e.toString())
        } catch (e: IOException) {
            Log.e("update_parser", e.toString())
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPreferences(parser: XmlPullParser) {
        parser.require(XmlPullParser.START_TAG, ns, "map")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) { continue }
            val node = parser.name
            when (node) {
                "string" -> readName(parser)
                "int" -> readValueInt(parser)
                "boolean" -> readBool(parser)
                else -> skip(parser)
            }
        }
    }

    private fun readName(parser: XmlPullParser) {
        try {
            parser.require(XmlPullParser.START_TAG, ns, "string")
            val valueKey = parser.getAttributeValue(null, "name")
            val name = readTextName(parser)
            parser.require(XmlPullParser.END_TAG, ns, "string")
            if(valueKey == "versionName") {
                verName = name
            }
            if(valueKey == "message") {
                updateMsg = name
                PREFS!!.setUpdateMessage(name)
            }
            if(valueKey == "tag") {
                tag = name
            }
            Log.i("parserText", "key: $valueKey value: $name")
        } catch (exception: Exception) {
            Log.e("update_parser", exception.toString())
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTextName(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }
    private fun readBool(parser: XmlPullParser) {
        try {
            parser.require(XmlPullParser.START_TAG, ns, "boolean")
            val valueData = parser.getAttributeValue(null, "value")
            val valueKey = parser.getAttributeValue(null, "name")
            if(valueKey == "beta") {
                isBeta = valueData.toBoolean()
            }
            parser.nextTag()
            Log.i("parserBool", "key: $valueKey value: $valueData")
        } catch (exception: Exception) {
            Log.e("update_parser", exception.toString())
        }
    }
    private fun readValueInt(parser: XmlPullParser) {
        try {
            parser.require(XmlPullParser.START_TAG, ns, "int")
            val valueData = parser.getAttributeValue(null, "value")
            val valueKey = parser.getAttributeValue(null, "name")
            if(valueKey == "versionCode") {
                verCode = valueData.toInt()
            }
            parser.nextTag()
            Log.i("parserInt", "key: $valueKey value: $valueData")
        } catch (exception: Exception) {
            Log.e("update_parser", exception.toString())
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        check(parser.eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
    companion object {
        private val ns: String? = null
        var verCode: Int? = null
        var verName: String? = null
        var isBeta: Boolean? = null
        var updateMsg: String? = null
        var tag: String? = null
    }
}