package com.receitas.app

import android.app.Application
import com.receitas.app.data.AppDatabase

class ReceitASApp : Application() {
    val database: AppDatabase get() = AppDatabase.getInstance(this)
}
