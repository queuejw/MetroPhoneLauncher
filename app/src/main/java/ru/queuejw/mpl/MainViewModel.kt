package ru.queuejw.mpl

import android.app.Application
import android.graphics.Bitmap
import androidx.collection.SparseArrayCompat
import androidx.lifecycle.AndroidViewModel
import ru.queuejw.mpl.content.data.app.App
import ru.queuejw.mpl.content.data.tile.TileDao
import ru.queuejw.mpl.content.data.tile.TileData

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val icons: SparseArrayCompat<Bitmap?> = SparseArrayCompat<Bitmap?>()
    private var appList: MutableList<App> = ArrayList()

    private val tileDao: TileDao by lazy {
        TileData.getTileData(application.applicationContext).getTileDao()
    }

    fun addIconToCache(appPackage: String, bitmap: Bitmap?) {
        icons.append(appPackage.hashCode(), bitmap)
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

    fun getAppList(): MutableList<App> {
        return appList
    }

    fun setAppList(list: MutableList<App>) {
        appList = list
    }

    fun getViewModelTileDao(): TileDao {
        return tileDao
    }
}