package ru.dimon6018.metrolauncher.content.data.apps

data class App(
    var appLabel: String? = null,
    var appPackage: String? = null,
    var selected: Boolean = false,
    var type: Int = 0
)