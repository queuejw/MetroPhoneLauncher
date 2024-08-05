package ru.dimon6018.metrolauncher

import android.graphics.drawable.Drawable
import androidx.collection.SparseArrayCompat
import androidx.lifecycle.ViewModel
import ru.dimon6018.metrolauncher.content.data.app.App

class MainViewModel : ViewModel() {

    private val icons: SparseArrayCompat<Drawable?> = SparseArrayCompat<Drawable?>()
    private var appList: ArrayList<App> = ArrayList()

    fun addIconToCache(appPackage: String, drawable: Drawable?) {
        icons.append(appPackage.hashCode(), drawable)
    }
    fun removeIconFromCache(appPackage: String) {
        icons.remove(appPackage.hashCode())
    }
    fun getIconFromCache(appPackage: String): Drawable? {
        return icons[appPackage.hashCode()]
    }
    fun addAppToList(app: App) {
        appList.add(app)
    }
    fun removeAppFromList(app: App) {
        appList.remove(app)
    }
    fun getAppList(): ArrayList<App> {
        return appList
    }
    fun setAppList(list: ArrayList<App>) {
        appList = list
    }
}