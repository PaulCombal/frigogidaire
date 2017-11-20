package fr.exia.main;

import fr.exia.controller.Controller;
import fr.exia.model.Model;
import fr.exia.view.*;
import org.gnome.gtk.Gtk;

//TODO sudo chmod 666 /dev/ttyACM*
//TODO sudo chmod 666 /dev/ttyUSB*
public class mainClass {

	public static void main(String[] args) {
		System.out.println("Starting GTK");
		Gtk.init(args);
		System.out.println("GTK Started, testing RXTX");
		
		Model model = new Model(); //Model
		Controller controller = new Controller(model); //Controller
		MainWindow view = new MainWindow(controller); //View
		
		
		System.out.println("All libraries loaded.");
		System.out.println("=============");
		
		if(!controller.connectToArduino()) {
			System.exit(-1);
		}
		
		view.showAll();
		
		Gtk.main();
	}

}