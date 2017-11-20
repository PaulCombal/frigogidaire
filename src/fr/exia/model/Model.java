package fr.exia.model;

import java.util.Observable;

//TODO Singleton?
public class Model extends Observable{
	
	private float m_temperature;
	private float m_humidity;
	
	public float getTemperature() {
		return this.m_temperature;
	}
	
	public float getHumidity() {
		return this.m_humidity;
	}
	
	public void setValues(float temperature, float humidity) {
		this.m_temperature = temperature;
		this.m_humidity = humidity;
		
		this.setChanged();
		notifyObservers(this);
	}

}
