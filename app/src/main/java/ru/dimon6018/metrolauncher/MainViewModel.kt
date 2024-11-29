package ru.dimon6018.metrolauncher

import android.app.Application
import android.graphics.Bitmap
import androidx.collection.SparseArrayCompat
import androidx.lifecycle.AndroidViewModel
import ru.dimon6018.metrolauncher.content.data.app.App
import ru.dimon6018.metrolauncher.content.data.tile.TileDao
import ru.dimon6018.metrolauncher.content.data.tile.TileData

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val icons: SparseArrayCompat<Bitmap?> = SparseArrayCompat<Bitmap?>()
    private val tileDao: TileDao = TileData.getTileData(application.applicationContext).getTileDao()
    private var appList: MutableList<App> = ArrayList()

    fun addIconToCache(appPackage: String, bitmap: Bitmap?) {
        if (icons[appPackage.hashCode()] == null) icons.append(appPackage.hashCode(), bitmap)
    }

    fun removeIconFromCache(appPackage: String) {
        icons.remove(appPackage.hashCode())
    }

    fun getIconFromCache(appPackage: String): Bitmap? {
        return icons[appPackage.hashCode()]
    }

    fun addAppToList(app: App) {
        if (!appList.contains(app)) appList.add(app)
    }

    fun removeAppFromList(app: App) {
        appList.remove(app)
    }

    fun getAppList(): MutableList<App> {
        return appList
    }

    fun setAppList(list: MutableList<App>) {
        appList = list
    }

    fun getTileDao(): TileDao {
        return tileDao
    }
}