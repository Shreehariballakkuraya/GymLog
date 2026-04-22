package com.hari.gymlog.ui.food

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hari.gymlog.data.db.GymLogDatabase
import com.hari.gymlog.data.db.entity.FoodEntity
import com.hari.gymlog.data.repository.FoodRepository
import com.hari.gymlog.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodRepository
    
    // Default to today
    private val currentDateFlow = MutableStateFlow(DateUtils.getCurrentDateForDb())

    val foods: StateFlow<List<FoodEntity>>

    init {
        val database = GymLogDatabase.getDatabase(application)
        repository = FoodRepository(database.foodDao())

        foods = currentDateFlow
            .flatMapLatest { date -> repository.getFoodsByDate(date) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun setDate(date: String) {
        currentDateFlow.value = date
    }

    fun addFood(food: FoodEntity) {
        viewModelScope.launch {
            repository.insert(food)
        }
    }

    fun updateFood(food: FoodEntity) {
        viewModelScope.launch {
            repository.update(food)
        }
    }

    fun deleteFood(food: FoodEntity) {
        viewModelScope.launch {
            repository.delete(food)
        }
    }
}
