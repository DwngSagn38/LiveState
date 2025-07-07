package com.example.livestate.data

import android.content.Context
import android.util.Log
import com.example.livestate.R
import com.example.livestate.model.CameraLiveModel
import com.example.livestate.model.FamousPlaceModel
import com.example.livestate.model.PlacesModel
import com.example.livestate.sharePreferent.PreferenceManager

object DataApp {
    val listCameraLive = mutableListOf<CameraLiveModel>()
    var listFamousPlace = mutableListOf<FamousPlaceModel>()

    fun setListCameraLive(context: Context, list: List<CameraLiveModel>) {
        listCameraLive.clear()
        listCameraLive.addAll(list)
        Log.d("DataApp", "setListCameraLive: $listCameraLive")
    }
    fun getListCameraLive(context: Context): List<CameraLiveModel> {
        return listCameraLive
    }

    fun getListPlaces(context: Context): List<PlacesModel> {
        return listOf(
            PlacesModel(0,"Hospital", R.drawable.icon_hospital,R.drawable.icon_hospital2),
            PlacesModel(1,"Police Station", R.drawable.icon_police_station,R.drawable.icon_police_station2),
            PlacesModel(2,"Gas Station", R.drawable.icon_gas_station, R.drawable.icon_gas_station2),
            PlacesModel(3,"Park", R.drawable.icon_park,R.drawable.icon_park2),
            PlacesModel(4,"Supermarket", R.drawable.icon_supermaket,R.drawable.icon_supermaket2),
            PlacesModel(5,"Post Office", R.drawable.icon_post_office, R.drawable.icon_post_office2),
            PlacesModel(6,"Hotel", R.drawable.icon_hotel, R.drawable.icon_hotel2),
            PlacesModel(7,"Library", R.drawable.icon_library, R.drawable.icon_library2),
            PlacesModel(8,"Pharmacy", R.drawable.icon_pharmacy, R.drawable.icon_pharmacy2),
            PlacesModel(9,"Coffee", R.drawable.icon_coffe, R.drawable.icon_coffe2),
            PlacesModel(10,"Veterinary", R.drawable.icon_veterinary, R.drawable.icon_veterinary2),
            PlacesModel(11,"ATM", R.drawable.icon_atm, R.drawable.icon_atm2),
            PlacesModel(12,"Stadium", R.drawable.icon_stadium, R.drawable.icon_stadium2),
            PlacesModel(13,"School", R.drawable.icon_school, R.drawable.icon_school),
            PlacesModel(14,"Airport", R.drawable.icon_airport, R.drawable.icon_airport2),
            PlacesModel(15,"Train Station", R.drawable.icon_train_station, R.drawable.icon_train_station2),
            PlacesModel(16,"Bookstore", R.drawable.icon_bookstore, R.drawable.icon_bookstore2),
            PlacesModel(17,"Bank", R.drawable.icon_bank, R.drawable.icon_bank2),
            PlacesModel(18,"Shopping Mall", R.drawable.icon_shopping_mall, R.drawable.icon_shopping_mall2),
            PlacesModel(19,"Cinema", R.drawable.icon_cinema, R.drawable.icon_cinema2),
            PlacesModel(20,"Museum", R.drawable.icon_museum, R.drawable.icon_museum2),
            PlacesModel(21,"Temple", R.drawable.icon_temple, R.drawable.icon_temple2),
            PlacesModel(22,"Pizza", R.drawable.icon_pizza, R.drawable.icon_pizza2),
            PlacesModel(23,"Spa",R.drawable.icon_spa,R.drawable.icon_spa2),
            PlacesModel(24,"Zoo", R.drawable.icon_zoo, R.drawable.icon_zoo2),
            PlacesModel(25,"Bus Station", R.drawable.icon_bus_station, R.drawable.icon_bus_station),
            PlacesModel(26,"Restaurant", R.drawable.icon_restaurant, R.drawable.icon_restaurant),
            PlacesModel(27,"University", R.drawable.icon_school, R.drawable.icon_school),

        )
    }

    fun getListFamousPlaceDefault(context: Context): List<FamousPlaceModel> {
        return listOf(
            FamousPlaceModel(0,"Vatican", R.drawable.img_vatican,false),
            FamousPlaceModel(1,"New York", R.drawable.img_new_york,false),
            FamousPlaceModel(2,"Tokyo", R.drawable.img_tokyo,false),
            FamousPlaceModel(3,"Paris", R.drawable.img_paris,false),
            FamousPlaceModel(4,"Hong Kong", R.drawable.img_hongkong,false),
            FamousPlaceModel(5,"Italy", R.drawable.img_italy,false),
            FamousPlaceModel(6,"Egyptian pyramids", R.drawable.img_egyptian_pyramids,false),
            FamousPlaceModel(7,"Ha Long Bay", R.drawable.img_halong,false),
            FamousPlaceModel(8,"Brazil", R.drawable.img_brazil,false),
            FamousPlaceModel(9,"China", R.drawable.img_china,false),
            FamousPlaceModel(10,"India", R.drawable.img_an_do,false),
            FamousPlaceModel(11,"Petra", R.drawable.img_petra,false),
            FamousPlaceModel(12,"Chichen Itza (Mexico)", R.drawable.img_chichen_itza_mexico,false),
            FamousPlaceModel(13,"Machu Picchu (Peru)", R.drawable.img_machu_picchu_peru,false),
            FamousPlaceModel(14,"Colosseum (Italy)", R.drawable.img_colosseum_italia,false),
        )
    }

    fun getListWithFavorite(context: Context): List<FamousPlaceModel> {
        val pref = PreferenceManager(context)
        listFamousPlace = getListFamousPlaceDefault(context).toMutableList()
        listFamousPlace.forEach {
            it.favorite = pref.isFavorite( it.id.toString())
        }
        Log.d("DataApp", "getListWithFavorite: $listFamousPlace")
        return listFamousPlace
    }
}