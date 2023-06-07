//
//  MapViewController.swift
//  TLV-Hackathon
//
//  Created by Golan Shoval Gil on 05/06/2023.
//

import UIKit
import MapKit

class MapViewController: UIViewController {

    @IBOutlet weak var mapView: MKMapView!
    private var locationManager = CLLocationManager()
    
    var pins = [String: MKPointAnnotation]()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Map View"
        setupLocationManager()
        setupMapView()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if let coordinate = locationManager.location?.coordinate {
            mapView.setCenter(coordinate, animated: true)
            let region = mapView.regionThatFits(MKCoordinateRegion(center: coordinate, latitudinalMeters: 10000, longitudinalMeters: 10000))
            mapView.setRegion(region, animated: true)
        }
    }
    
    func setupMapView() {
        mapView.showsUserLocation = true

        if let mapPlaces = loadJson(filename: "MapPlaces") {
            let newPlaces = mapPlaces.map{ ($0.uuid, buildPin(from: $0)) }
            pins = Dictionary(uniqueKeysWithValues: newPlaces)
            mapView.addAnnotations(Array(pins.values))
        }
    }
    
    func updatePin(value: MKPointAnnotation, uuid: String) {
        /// Get pin from pins using the uuid
        /// remove annotation from mapView
        /// update pin coordinate using value given as parameter
        /// add annotation back to mapView
    }
    
    func setupLocationManager() {
        locationManager.requestWhenInUseAuthorization()
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = kCLDistanceFilterNone
        locationManager.startUpdatingLocation()
    }
    
    func buildPin(from place: Place) -> MKPointAnnotation {
        let pin = MKPointAnnotation()
        pin.coordinate = CLLocationCoordinate2D(latitude: place.latitude, longitude: place.longitude)
        pin.title = place.title
        pin.subtitle = place.subtitle
        return pin
    }
    
    func loadJson(filename fileName: String) -> [Place]? {
        if let url = Bundle.main.url(forResource: fileName, withExtension: "json") {
            do {
                let data = try Data(contentsOf: url)
                let decoder = JSONDecoder()
                let jsonData = try decoder.decode(MapPlaces.self, from: data)
                return jsonData.places
            } catch {
                print("error:\(error)")
            }
        }
        return nil
    }
}

struct MapPlaces: Codable {
    let places: [Place]
}

// MARK: - Place
struct Place: Codable {
    let uuid, title, subtitle: String
    let latitude, longitude: Double
}

