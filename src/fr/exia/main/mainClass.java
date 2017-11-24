package fr.exia.main;

import fr.exia.controller.Controller;
import fr.exia.model.Model;
import fr.exia.view.*;
import org.gnome.gtk.Gtk;

//TODO sudo chmod 666 /dev/ttyACM*
//TODO sudo chmod 666 /dev/ttyUSB*
public class mainClass {

	public static void main(String[] args) {
		//Instance MVC objects
		Gtk.init(args);
		
		Model model = new Model(); //Model
		Controller controller = new Controller(model); //Controller
		MainWindow2 view = new MainWindow2(controller); //View
		
		//Make sure connection is successful
		if(!controller.connectToArduino()) {
			System.exit(-1);
		}
		
		//When the model updates, refreshes the view
		model.addObserver(view);
		
		//Shows the main window
		view.showAll();
		
		Gtk.main();
	}

}