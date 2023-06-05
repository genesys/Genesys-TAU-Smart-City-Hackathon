package com.genesys.tauhackathon

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import org.json.JSONObject
import java.lang.ref.WeakReference

class MapsActivity : AppCompatActivity() {

    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private val jsonString = """
    {
        "places": [
            {
                "uuid": "c2c0b47d-9db5-4fa0-82d3-9e25d5480ebb",
                "title": "Carmel Market",
                "subtitle": "Vibrant marketplace in Tel Aviv",
                "latitude": 32.0684,
                "longitude": 34.7691
            },
            {
                "uuid": "2d6a39cb-3e30-4b3b-b63f-1d3e8c8dfcc4",
                "title": "Jaffa Port",
                "subtitle": "Ancient port area",
                "latitude": 32.0525,
                "longitude": 34.7532
            },
            {
                "uuid": "8c9e535a-9fba-4d7d-99f1-b487a5723c41",
                "title": "Eretz Israel Museum",
                "subtitle": "Archaeology and history museum",
                "latitude": 32.1133,
                "longitude": 34.8056
            },
            {
                "uuid": "c5e44e96-31d1-4c8b-b207-09da5aae6fb1",
                "title": "Yitzhak Rabin Center",
                "subtitle": "Museum dedicated to the legacy of Yitzhak Rabin",
                "latitude": 32.1044,
                "longitude": 34.8062
            },
            {
                "uuid": "6f83401b-fa67-4b3d-8cfe-92ed71c53ad9",
                "title": "Rothschild Boulevard",
                "subtitle": "Historic street in Tel Aviv",
                "latitude": 32.0625,
                "longitude": 34.7759
            }
        ]
    }
""".trimIndent()

    private val placeList: ArrayList<Place> by lazy {

        val jsonObject = JSONObject(jsonString)
        val placesArray = jsonObject.getJSONArray("places")

        val tempList = ArrayList<Place>()

        for (i in 0 until placesArray.length()) {

            val placeObj = placesArray.getJSONObject(i)
            val place = Place(
                placeObj.getString("uuid"),
                placeObj.getString("title"),
                placeObj.getString("subtitle"),
                placeObj.getDouble("latitude"),
                placeObj.getDouble("longitude")
            )
            tempList.add(place)
        }
        tempList
    }

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView = MapView(this)
        setContentView(mapView)
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions { onMapReady() }
    }

    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )

        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
        }

        addAnnotationToMap()
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun addAnnotationToMap() {

        val annotationApi = mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()

        placeList.forEach { place ->

            bitmapFromDrawableRes(
                this@MapsActivity,
                com.mapbox.maps.plugin.attribution.R.drawable.abc_btn_radio_to_on_mtrl_015
            )?.let {
                val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(place.longitude, place.latitude))
                    .withIconImage(it)
                pointAnnotationManager.create(pointAnnotationOptions)
            }
        }
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        locationComponentPlugin.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.location.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
// copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

}