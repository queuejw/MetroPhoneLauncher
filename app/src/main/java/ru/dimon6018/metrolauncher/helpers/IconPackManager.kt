package ru.dimon6018.metrolauncher.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.util.Locale
import java.util.Random

class IconPackManager {
    private var mContext: Context? = null
    fun setContext(c: Context?) {
        mContext = c
    }

    inner class IconPack {
        var packageName: String? = null
        var name: String? = null
        private var mLoaded = false
        private val mPackagesDrawables = HashMap<String?, String?>()
        private val mBackImages: MutableList<Bitmap> = ArrayList()
        private var mMaskImage: Bitmap? = null
        private var mFrontImage: Bitmap? = null
        private var mFactor = 1.0f

        @get:Suppress("unused")
        private var totalIcons = 0
        private var iconPackRes: Resources? = null
        private fun load() {
            // load appfilter.xml from the icon pack package
            val pm = mContext!!.packageManager
            try {
                var xpp: XmlPullParser? = null
                iconPackRes = pm.getResourcesForApplication(packageName!!)
                val appFilterId = iconPackRes!!.getIdentifier("appfilter", "xml", packageName)
                if (appFilterId > 0) {
                    xpp = iconPackRes!!.getXml(appFilterId)
                } else {
                    // no resource found, try to open it from assests folder
                    try {
                        val appFilterStream = iconPackRes!!.assets.open("appfilter.xml")
                        val factory = XmlPullParserFactory.newInstance()
                        factory.isNamespaceAware = true
                        xpp = factory.newPullParser()
                        xpp.setInput(appFilterStream, "utf-8")
                    } catch (e1: IOException) {
                        //Ln.d("No appfilter.xml file");
                    }
                }
                if (xpp != null) {
                    var eventType = xpp.eventType
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            if (xpp.name == "iconback") {
                                for (i in 0 until xpp.attributeCount) {
                                    if (xpp.getAttributeName(i).startsWith("img")) {
                                        val drawableName = xpp.getAttributeValue(i)
                                        val iconback = loadBitmap(drawableName)
                                        if (iconback != null) mBackImages.add(iconback)
                                    }
                                }
                            } else if (xpp.name == "iconmask") {
                                if (xpp.attributeCount > 0 && xpp.getAttributeName(0) == "img1") {
                                    val drawableName = xpp.getAttributeValue(0)
                                    mMaskImage = loadBitmap(drawableName)
                                }
                            } else if (xpp.name == "iconupon") {
                                if (xpp.attributeCount > 0 && xpp.getAttributeName(0) == "img1") {
                                    val drawableName = xpp.getAttributeValue(0)
                                    mFrontImage = loadBitmap(drawableName)
                                }
                            } else if (xpp.name == "scale") {
                                // mFactor
                                if (xpp.attributeCount > 0 && xpp.getAttributeName(0) == "factor") {
                                    mFactor = xpp.getAttributeValue(0).toFloat()
                                }
                            } else if (xpp.name == "item") {
                                var componentName: String? = null
                                var drawableName: String? = null
                                for (i in 0 until xpp.attributeCount) {
                                    if (xpp.getAttributeName(i) == "component") {
                                        componentName = xpp.getAttributeValue(i)
                                    } else if (xpp.getAttributeName(i) == "drawable") {
                                        drawableName = xpp.getAttributeValue(i)
                                    }
                                }
                                if (!mPackagesDrawables.containsKey(componentName)) {
                                    mPackagesDrawables[componentName] = drawableName
                                    totalIcons += 1
                                }
                            }
                        }
                        eventType = xpp.next()
                    }
                }
                mLoaded = true
            } catch (e: PackageManager.NameNotFoundException) {
                //Ln.d("Cannot load icon pack");
            } catch (e: XmlPullParserException) {
                //Ln.d("Cannot parse icon pack appfilter.xml");
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun loadBitmap(drawableName: String): Bitmap? {
            val id = iconPackRes!!.getIdentifier(drawableName, "drawable", packageName)
            if (id > 0) {
                val bitmap = ResourcesCompat.getDrawable(iconPackRes!!, id, null)
                if (bitmap is BitmapDrawable) return bitmap.bitmap
            }
            return null
        }

        private fun loadDrawable(drawableName: String): Drawable? {
            val id = iconPackRes!!.getIdentifier(drawableName, "drawable", packageName)
            return if (id > 0) {
                ResourcesCompat.getDrawable(iconPackRes!!, id, null)
            } else null
        }

        fun getDrawableIconForPackage(appPackageName: String?, defaultDrawable: Drawable?): Drawable? {
            if (!mLoaded) load()
            val pm = mContext!!.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(appPackageName!!)
            var componentName: String? = null
            if (launchIntent != null) componentName = pm.getLaunchIntentForPackage(appPackageName)!!.component.toString()
            var drawable = mPackagesDrawables[componentName]
            if (drawable != null) {
                return loadDrawable(drawable)
            } else {
                // try to get a resource with the component filename
                if (componentName != null) {
                    val start = componentName.indexOf("{") + 1
                    val end = componentName.indexOf("}", start)
                    if (end > start) {
                        drawable = componentName.substring(start, end).lowercase(Locale.getDefault()).replace(".", "_").replace("/", "_")
                        if (iconPackRes!!.getIdentifier(drawable, "drawable", packageName) > 0) return loadDrawable(drawable)
                    }
                }
            }
            return defaultDrawable
        }

        @Suppress("unused")
        fun getIconForPackage(appPackageName: String?, defaultBitmap: Bitmap): Bitmap? {
            if (!mLoaded) load()
            val pm = mContext!!.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(appPackageName!!)
            var componentName: String? = null
            if (launchIntent != null) componentName = pm.getLaunchIntentForPackage(appPackageName)!!.component.toString()
            var drawable = mPackagesDrawables[componentName]
            if (drawable != null) {
                val bmp = loadBitmap(drawable)
                return bmp ?: generateBitmap(defaultBitmap)
            } else {
                // try to get a resource with the component filename
                if (componentName != null) {
                    val start = componentName.indexOf("{") + 1
                    val end = componentName.indexOf("}", start)
                    if (end > start) {
                        drawable = componentName.substring(start, end).lowercase(Locale.getDefault()).replace(".", "_").replace("/", "_")
                        if (iconPackRes!!.getIdentifier(drawable, "drawable", packageName) > 0) return loadBitmap(drawable)
                    }
                }
            }
            return generateBitmap(defaultBitmap)
        }

        private fun generateBitmap(defaultBitmap: Bitmap): Bitmap {
            // if no support images in the icon pack return the bitmap itself
            if (mBackImages.size == 0) return defaultBitmap
            val r = Random()
            val backImageInd = r.nextInt(mBackImages.size)
            val backImage = mBackImages[backImageInd]
            val w = backImage.getWidth()
            val h = backImage.getHeight()

            // create a bitmap for the result
            val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val mCanvas = Canvas(result)

            // draw the background first
            mCanvas.drawBitmap(backImage, 0f, 0f, null)

            // create a mutable mask bitmap with the same mask
            val scaledBitmap: Bitmap = if (defaultBitmap.getWidth() > w || defaultBitmap.getHeight() > h) {
                Bitmap.createScaledBitmap(defaultBitmap, (w * mFactor).toInt(), (h * mFactor).toInt(), false)
            } else {
                Bitmap.createBitmap(defaultBitmap)
            }
            val mutableMask = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val maskCanvas = Canvas(mutableMask)
            if (mMaskImage != null) {
                // draw the scaled bitmap with mask
                maskCanvas.drawBitmap(mMaskImage!!, 0f, 0f, Paint())

                // paint the bitmap with mask into the result
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_OUT))
                mCanvas.drawBitmap(scaledBitmap, (w - scaledBitmap.getWidth()).toFloat() / 2, (h - scaledBitmap.getHeight()).toFloat() / 2, null)
                mCanvas.drawBitmap(mutableMask, 0f, 0f, paint)
                paint.setXfermode(null)
            } else  // draw the scaled bitmap with the back image as mask
            {
                maskCanvas.drawBitmap(backImage, 0f, 0f, Paint())

                // paint the bitmap with mask into the result
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))
                mCanvas.drawBitmap(scaledBitmap, (w - scaledBitmap.getWidth()).toFloat() / 2, (h - scaledBitmap.getHeight()).toFloat() / 2, null)
                mCanvas.drawBitmap(mutableMask, 0f, 0f, paint)
                paint.setXfermode(null)
            }

            // paint the front
            if (mFrontImage != null) {
                mCanvas.drawBitmap(mFrontImage!!, 0f, 0f, null)
            }

            // store the bitmap in cache
//            BitmapCache.getInstance(mContext).putBitmap(key, result);

            // return it
            return result
        }

        override fun equals(other: Any?): Boolean {
            return if (other is IconPack) {
                other.packageName == packageName
            } else false
        }
    }
    private var iconPacks: ArrayList<IconPack>? = null

    fun getAvailableIconPacks(forceReload: Boolean): ArrayList<IconPack> {
        if (iconPacks == null || forceReload) {
            iconPacks = ArrayList()

            // find apps with intent-filter "com.gau.go.launcherex.theme" and return build the HashMap
            val pm = mContext!!.packageManager
            val adwLauncherThemes = pm.queryIntentActivities(Intent("org.adw.launcher.THEMES"), PackageManager.GET_META_DATA)
            val goLauncherThemes = pm.queryIntentActivities(Intent("com.gau.go.launcherex.theme"), PackageManager.GET_META_DATA)

            // merge those lists
            val resolveInfo: MutableList<ResolveInfo> = ArrayList(adwLauncherThemes)
            resolveInfo.addAll(goLauncherThemes)
            for (ri in resolveInfo) {
                val ip = IconPack()
                ip.packageName = ri.activityInfo.packageName
                var ai: ApplicationInfo
                try {
                    ai = pm.getApplicationInfo(ip.packageName!!, PackageManager.GET_META_DATA)
                    ip.name = mContext!!.packageManager.getApplicationLabel(ai).toString()
                    if (!iconPacks!!.contains(ip)) iconPacks!!.add(ip)
                } catch (e: PackageManager.NameNotFoundException) {
                    // shouldn't happen
                    e.printStackTrace()
                }
            }
        }
        return iconPacks!!
    }

    fun getIconPackWithName(packageName: String?): IconPack? {
        val pm = mContext!!.packageManager
        val targetPack = IconPack()
        targetPack.packageName = packageName
        if(iconPacks == null) {
            iconPacks = getAvailableIconPacks(true)
        }
        if (iconPacks!!.contains(targetPack)) {
            val ai: ApplicationInfo
            try {
                ai = pm.getApplicationInfo(targetPack.packageName!!, PackageManager.GET_META_DATA)
                targetPack.name = mContext!!.packageManager.getApplicationLabel(ai).toString()
                return targetPack
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
        return null
    }
}