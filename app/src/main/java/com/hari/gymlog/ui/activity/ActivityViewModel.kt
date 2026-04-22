package com.hari.gymlog.ui.activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hari.gymlog.data.db.GymLogDatabase
import com.hari.gymlog.data.db.entity.ActivityEntity
import com.hari.gymlog.data.repository.ActivityRepository
import com.hari.gymlog.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ActivityRepository
    private val currentDateFlow = MutableStateFlow(DateUtils.getCurrentDateForDb())

    val currentActivity: StateFlow<ActivityEntity?>

    init {
        val database = GymLogDatabase.getDatabase(application)
        repository = ActivityRepository(database.activityDao())

        // Our DAO returns Flow<List<ActivityEntity>>, but we know it's 1 per day max
        currentActivity = currentDateFlow
            .flatMapLatest { date -> repository.getActivityByDate(date) }
            .stateIn(
                scope = viewModelScope, 
                started = SharingStarted.WhileSubscribed(5000), 
                initialValue = emptyList()
            )
            .flatMapLatest { list -> MutableStateFlow(list.firstOrNull()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    fun saveActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.insertOrUpdate(activity)
        }
    }
}
