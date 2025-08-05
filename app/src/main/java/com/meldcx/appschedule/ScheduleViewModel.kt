package com.meldcx.appschedule
import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).appScheduleDao()
    val allSchedules: LiveData<List<AppSchedule>> = dao.getAllSchedules()

    fun insert(schedule: AppSchedule) {
        viewModelScope.launch {
            dao.insertSchedule(schedule)
        }
    }

    fun update(schedule: AppSchedule) {
        viewModelScope.launch {
            dao.updateSchedule(schedule)
        }
    }

    fun delete(schedule: AppSchedule) {
        viewModelScope.launch {
            dao.deleteSchedule(schedule)
        }
    }

    fun getScheduleById(id: Long): LiveData<AppSchedule?> {
        val result = MutableLiveData<AppSchedule?>()
        viewModelScope.launch {
            result.postValue(dao.getScheduleById(id))
        }
        return result
    }

   /* fun scheduleAt(timeMillis: Long): LiveData<AppSchedule?> {
        val result = MutableLiveData<AppSchedule?>()
        viewModelScope.launch {
            result.postValue(dao.findByTime(timeMillis))
        }
        return result
    }*/
}
