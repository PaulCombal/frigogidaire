package fr.exia.model;

import java.util.Observable;

public class Model extends Observable {
	
	private float m_int_temperature;
	private float m_ext_temperature;
	private float m_pel_temperature;
	private float m_humidity;
	private float m_instruction = 15;
	
	private int m_numberOfTempIncreasesInARow;
	private boolean m_notified = false;
	
	public float getIntTemperature() {
		return this.m_int_temperature;
	}
	
	public float getExtTemperature() {
		return this.m_ext_temperature;
	}
	
	public float getPelTemperature() {
		return this.m_pel_temperature;
	}
	
	public float getHumidity() {
		return this.m_humidity;
	}
	
	public void notifyDoorOpened() {
		boolean opened = this.m_int_temperature > this.m_instruction && this.m_numberOfTempIncreasesInARow > 5;
		
		if(opened && !this.m_notified){
			this.setChanged();
			this.notifyObservers("opened");
			this.m_notified = true;
		} else {
			this.m_notified = false;
			this.m_numberOfTempIncreasesInARow = 0; //The increase is not an increase above instruction
		}
	}
	
	public void setValues(float int_temperature, float ext_temperature, float pel_temperature, float humidity) {
		if(int_temperature > this.m_int_temperature) {
			this.m_numberOfTempIncreasesInARow++;
		} 
		else {
			this.m_numberOfTempIncreasesInARow = 0;
		}
		
		this.m_int_temperature = int_temperature;
		this.m_ext_temperature = ext_temperature;
		this.m_pel_temperature = pel_temperature;
		this.m_humidity = humidity;
		
		this.setChanged();
		this.notifyObservers(this);
	}
	
	//We do not need to notify the view when the instruction is changed.
	public void setInstruction(float instruction) {
		this.m_instruction = instruction;
	}
	
	public String toString() {
		return "T°: " + this.m_int_temperature + "\t\tH%: " + this.m_humidity + "\tI°: " + this.m_instruction;
	}

}
