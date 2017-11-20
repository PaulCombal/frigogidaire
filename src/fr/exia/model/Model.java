package fr.exia.model;

import java.util.Observable;

//TODO Singleton?
public class Model extends Observable{
	
	private int m_temperature;
	private int m_humidity;
	
	public int getTemperature() {
		return this.m_temperature;
	}
	
	public int getHumidity() {
		return this.m_humidity;
	}
	
	public void setValues(int temperature, int humidity) {
		this.m_temperature = temperature;
		this.m_humidity = humidity;
		
		this.setChanged();
		notifyObservers();
	}

}
