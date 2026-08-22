package com.genas.app

import android.app.Application
import com.genas.app.data.AppDatabase
import com.genas.app.data.Repository

class GenASApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { Repository(database) }
}
