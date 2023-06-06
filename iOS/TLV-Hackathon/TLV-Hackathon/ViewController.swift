//
//  ViewController.swift
//  TLV-Hackathon
//
//  Created by Golan Shoval Gil on 05/06/2023.
//

import UIKit

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
    }

    @IBAction func didTapMapButton(_ sender: UIButton) {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        let viewController = storyboard.instantiateViewController(identifier: "MapViewController")
        navigationController?.pushViewController(viewController, animated: true)
    }
}
