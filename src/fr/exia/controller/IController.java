package fr.exia.controller;

public interface IController {
	public boolean connectToArduino();
	public void disconnectArduino();
	public void sendValuesToModel();
	public void sendInstructionToArduino(float fInstruction);
}
