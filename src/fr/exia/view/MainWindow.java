package fr.exia.view;

import java.util.Observable;
import java.util.Observer;

import org.gnome.gdk.Event;
import org.gnome.gtk.*;

import fr.exia.controller.IController;
import fr.exia.model.Model;

//TODO Singleton
public class MainWindow extends Window implements Observer {
	
	private Label m_tempLabelValue = null;
	private Label m_humidityLabelValue = null;
	private HScale m_progressTemp = new HScale(0, 100, 1);
	private HScale m_progressHumidity = new HScale(0, 100, 1);
	private IController m_controller;
	
	public MainWindow(IController controller){
		super();
		
		if(controller == null) {
			System.out.println("Contrôlleur non instancié.");
			System.exit(-1);
		}
		
		this.m_controller = controller;
		this.m_progressHumidity.setSensitive(false);
		this.m_progressTemp.setSensitive(false);
		
		//this.setBorderWidth(20); //?
		this.setTitle("Moniteur du Frigogidaire");
		this.setDefaultSize(800, 300);
		this.setPosition(WindowPosition.CENTER);

		VBox layout = new VBox(true, 0);
		HBox tempLayout = new HBox(true, 0);
		HBox humidityLayout = new HBox(true, 0);
		HBox instructionLayout = new HBox(true, 0);
		HScale instructionField = new HScale(0, 30, 1);
		Button updateButton = new Button("Actualiser Consigne");
		
		this.m_tempLabelValue = new Label("Chargement..");
		this.m_humidityLabelValue = new Label("Chargement..");
		
		instructionField.setValue(15);
		
		tempLayout.packStart(new Label("Température (°C)"), true, true, 0);
		tempLayout.packStart(this.m_progressTemp, true, true, 0);
		tempLayout.packStart(this.m_tempLabelValue, true, false, 0);
		
		humidityLayout.packStart(new Label("Humidité (%)"), true, true, 0);
		humidityLayout.packStart(this.m_progressHumidity, true, true, 0);
		humidityLayout.packStart(this.m_humidityLabelValue, true, true, 0);
		
		instructionLayout.packStart(new Label("Consigne: "), true, true, 0);
		instructionLayout.packStart(instructionField, true, true, 0);
		instructionLayout.packStart(updateButton, false, false, 0);
		
		layout.packStart(new HSeparator(), false, false, 0);
		layout.packStart(tempLayout, true, true, 0);
		layout.packStart(new HSeparator(), false, false, 0);
		layout.packStart(humidityLayout, true, true, 0);
		layout.packStart(new HSeparator(), false, false, 0);
		layout.packStart(instructionLayout, true, false, 0);
		layout.packStart(new HSeparator(), false, false, 0);
		
		this.add(layout);
		
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
					//displayTemperature(dInstruction); // juste pour tester
					m_controller.sendInstructionToArduino((float)dInstruction);
				}
				catch (Exception e) {
					System.out.println(e.getMessage());
				}
				

			}
		});
	}
	
	public void displayTemperature(double newTemp) {
		if (newTemp < 0) {
			System.out.println("La température relevée est inférieure à 0:" + newTemp);
			newTemp = 0;
		}
		else if (newTemp > 100) {
			System.out.println("La température relevée est supérieure à 100:" + newTemp);
			newTemp = 100;
		}
		
		this.m_tempLabelValue.setLabel(newTemp + "°C");
		this.m_progressTemp.setValue(newTemp);
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
			this.displayTemperature(tmpMdl.getIntTemperature());
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
