package fr.exia.view;

import java.util.Observable;
import java.util.Observer;

import org.gnome.gdk.Event;
import org.gnome.gtk.*;

import fr.exia.controller.IController;
import fr.exia.model.Model;

//TODO Singleton
public class MainWindow2 extends Window implements Observer {
	
	private Label m_int_tempLabelValue = null;
	private Label m_ext_tempLabelValue = null;
	private Label m_pel_tempLabelValue = null;
	private Label m_humidityLabelValue = null;
	private HScale m_int_progressTemp = new HScale(0, 100, 1);
	private HScale m_ext_progressTemp = new HScale(0, 100, 1);
	private HScale m_pel_progressTemp = new HScale(0, 100, 1);
	private HScale m_progressHumidity = new HScale(0, 100, 1);
	private IController m_controller;
	
	public MainWindow2(IController controller){
		super();
		
		if(controller == null) {
			System.out.println("Contrôlleur non instancié.");
			System.exit(-1);
		}
		
		this.m_controller = controller;
		this.m_progressHumidity.setSensitive(false);
		this.m_int_progressTemp.setSensitive(false);
		this.m_ext_progressTemp.setSensitive(false);
		this.m_pel_progressTemp.setSensitive(false);
		
		//this.setBorderWidth(20); //?
		this.setTitle("Moniteur du Frigogidaire");
		this.setDefaultSize(800, 300);
		this.setPosition(WindowPosition.CENTER);
		this.setFullscreen(true);
		
		this.m_int_tempLabelValue = new Label("Chargement..");
		this.m_ext_tempLabelValue = new Label("Chargement..");
		this.m_pel_tempLabelValue = new Label("Chargement..");
		this.m_humidityLabelValue = new Label("Chargement..");

		Notebook note = new Notebook();
		
		VBox vIns = new VBox(false, 0);
		VBox vTemps = new VBox(false, 0);
		VBox vHumidity = new VBox(false, 0);
		
		HScale instructionField = new HScale(0, 30, 1);
		Button updateButton = new Button("Actualiser Consigne");
		
		vIns.packStart(instructionField, true, true, 0);
		vIns.packStart(updateButton, true, true, 0);
		
		vTemps.packStart(new Label("Température du réfrigérateur:"), true, true, 0);
		vTemps.packStart(this.m_int_progressTemp, true, true, 0);
		vTemps.packStart(this.m_int_tempLabelValue, true, true, 0);
		vTemps.packStart(new Label("Température extérieure:"), false, false, 10);
		vTemps.packStart(this.m_ext_progressTemp, true, true, 0);
		vTemps.packStart(this.m_ext_tempLabelValue, true, true, 0);
		vTemps.packStart(new Label("Température du module de refroidissement:"), true, true, 10);
		vTemps.packStart(this.m_pel_progressTemp, true, true, 0);
		vTemps.packStart(this.m_pel_tempLabelValue, true, true, 0);
		
		vHumidity.packStart(new Label("Humidité de l'air:"), true, true, 0);
		vHumidity.packStart(this.m_progressHumidity, true, true, 0);
		vHumidity.packStart(this.m_humidityLabelValue, true, true, 0);
		
		note.appendPage(vIns, new Label("Consigne"));
		note.appendPage(vTemps, new Label("Températures"));
		note.appendPage(vHumidity, new Label("Humidité"));
		
		instructionField.setValue(15);
		
		this.add(note);
		
		//Connexions
		this.connect(new Window.DeleteEvent() {
			@Override
			public boolean onDeleteEvent(Widget source, Event evt) {
				m_controller.disconnectArduino();
				Gtk.mainQuit();
				return false;
			}
        });
		
		updateButton.connect(new Button.Clicked() {
			@Override
			public void onClicked(Button source) {
				try {
					double dInstruction = instructionField.getValue();
					m_controller.sendInstructionToArduino((float)dInstruction);
				}
				catch (Exception e) {
					System.out.println(e.getMessage());
				}
				

			}
		});
	}
	
	private void updateTemp(double value, Label label, HScale slider) {
		if (value < 0) {
			System.out.println("La température relevée est inférieure à 0:" + value);
			value = 0;
		}
		else if (value > 100) {
			System.out.println("La température relevée est supérieure à 100:" + value);
			value = 100;
		}
		
		label.setLabel(value + "°C");
		slider.setValue(value);
	}
	
	public void displayTemperature(double interior, double exterior, double peltier) {
		//INTERIOR
		
		this.updateTemp(Math.round(interior * 100.0) / 100.0, this.m_int_tempLabelValue, this.m_int_progressTemp);
		
		//EXTERIOR
		
		this.updateTemp(Math.round(exterior * 100.0) / 100.0, this.m_ext_tempLabelValue, this.m_ext_progressTemp);
		
		//PELTIER
		
		this.updateTemp(Math.round(peltier * 100.0) / 100.0, this.m_pel_tempLabelValue, this.m_pel_progressTemp);
	}
	
	public void displayHumidity(double newHumidity) {
		if (newHumidity < 0) {
			System.out.println("L'humidité relevée est inférieure à 0:" + newHumidity);
			newHumidity = 0;
		}
		else if (newHumidity > 100) {
			System.out.println("L'humidité relevée est supérieure à 100:" + newHumidity);
			newHumidity = 100;
		}
		
		this.m_humidityLabelValue.setLabel(newHumidity + "%");
		this.m_progressHumidity.setValue(newHumidity);
	}

	@Override
	public void update(Observable model, Object obj) {
		if(obj instanceof Model) {
			Model tmpMdl;
			tmpMdl = (Model)obj;
			this.displayTemperature(tmpMdl.getIntTemperature(), tmpMdl.getExtTemperature(), tmpMdl.getPelTemperature());
			this.displayHumidity(tmpMdl.getHumidity());
		}
		else if (obj instanceof String && (String)obj == "opened"){
			//Info
			InfoMessageDialog msgBox = new InfoMessageDialog(this, "Porte ouverte", "Attention, la porte a été ouverte!");
			msgBox.setSecondaryText("Veuillez fermer la porte.");
			msgBox.run();
			msgBox.hide();
			msgBox = null;
		}
		else {
			System.out.println("La view a été notifiée d'un évènement non pris.");
		}
	}
}
