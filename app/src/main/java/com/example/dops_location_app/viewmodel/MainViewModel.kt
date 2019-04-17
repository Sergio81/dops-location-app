package com.example.dops_location_app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.dops_location_app.model.LocationResponse
import com.example.dops_location_app.service.LocationService

class MainViewModel(locationService: LocationService) : ViewModel() {
    private val locations = ArrayList<LocationResponse>()

    val lastLocation = locationService.lastLocation

    val lastLocations = Transformations.switchMap(locationService.lastLocation){location ->
        locations.add(location)

        if(locations.size > 20)
            locations.removeAt(locations.size-1)

        MutableLiveData<ArrayList<LocationResponse>>().apply { this.value = locations}
    }
}