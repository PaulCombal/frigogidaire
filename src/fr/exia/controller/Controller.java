package fr.exia.controller;

import gnu.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;

import fr.exia.model.Model;

public class Controller implements IController, SerialPortEventListener {

	//Reference to model
	private Model m_model;
	
    //for containing the ports that will be found
    private Enumeration ports = null;
    
    //map the port names to CommPortIdentifiers
    private HashMap<String, CommPortIdentifier> portMap = new HashMap<String, CommPortIdentifier>();
    
    //The buffer for received data
    private String m_receivedData;
    private String m_lastReceivedData;

    //this is the object that contains the opened port
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort serialPort = null;

    //input and output streams for sending and receiving data
    private InputStream input = null;
    private OutputStream output = null;

    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    //some ascii values for for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    final static int NEW_LINE_ASCII = 10;
    
    public Controller(Model model) {
    	if(model == null) {
    		System.out.println("Model is not instanced");
    		System.exit(-1);
    	}
    	
    	
    	this.m_model = model;
    	this.searchForPorts();
    	this.m_receivedData = "";
    	this.m_lastReceivedData = "";
    }
    
	//connect to the selected port in the combo box
    //pre style="font-size: 11px;": ports are already found by using the searchForPorts
    //method
    //post: the connected comm port is stored in commPort, otherwise,
    //an exception is generated
	@Override
	public boolean connectToArduino() {
		if (portMap.size() != 1) {
			System.out.println("TODO: Nombre de ports incorrect: " + portMap.size()); //TODO
			return false;
		}
		
        //String selectedPort = (String)window.cboxPorts.getSelectedItem();
        selectedPortIdentifier = (CommPortIdentifier)portMap.get("/dev/ttyACM0"); //TODO

        CommPort commPort = null;

        try
        {
            //the method below returns an object of type CommPort
            commPort = selectedPortIdentifier.open("TigerControlPanel", TIMEOUT);
            
            //the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort)commPort;
            
            if(this.initIOStream() && this.initListener()){
            	System.out.println("Successfully connected");	
            }
        }
        catch (PortInUseException e)
        {
        	System.out.println("Port in use: " + e.getMessage());
        	return false;
        }
        catch (Exception e)
        {
            System.out.println("Failed to open " + "/dev/ttyACM0" + "(" + e.toString() + ")"); //TODO
            return false;
        }
        
        return true;
	}

	@Override
	public void disconnectArduino() {
        try
        {
            //writeData(0, 0);

            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();

            System.out.println("Disconnected.");
        }
        catch (Exception e)
        {
            System.out.println("Failed to close " + serialPort.getName());
        }
		
	}

	@Override
	public void sendValuesToModel() {
		// TODO First, we have to retrieve the values
		int humidity = this.m_lastReceivedData;
		int temperature = this.m_lastReceivedData;
		
		//We send this to the model, it will notify observers
		this.m_model.setValues(temperature, humidity);
	}

	//what happens when data is received
    //pre style="font-size: 11px;": serial event is triggered
    //post: processing on the data it reads
	@Override
    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            try
            {
                byte singleData = (byte)input.read();

                if (singleData != NEW_LINE_ASCII)
                {
                	this.m_receivedData += new String(new byte[] {singleData});
                    //System.out.print(new String(new byte[] {singleData}));
                }
                else
                {
                    this.m_lastReceivedData = this.m_receivedData;
                    this.m_receivedData = "";
                    
                    this.sendValuesToModel();
                    
                }
            }
            catch (Exception e)
            {
                System.out.println("Failed to read data. (" + e.toString() + ")");
            }
        }
    }
	
	//search for all the serial ports
    //pre style="font-size: 11px;": none
    //post: adds all the found ports to a combo box on the GUI
    public void searchForPorts()
    {
        ports = CommPortIdentifier.getPortIdentifiers();

        while (ports.hasMoreElements())
        {
            CommPortIdentifier curPort = (CommPortIdentifier)ports.nextElement();

            //get only serial ports
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                //window.cboxPorts.addItem(curPort.getName());
                portMap.put(curPort.getName(), curPort);
                System.out.println("Found port:" + curPort.getName());
            }
        }
        
    }
    
    //open the input and output streams
    //pre style="font-size: 11px;": an open port
    //post: initialized input and output streams for use to communicate data
    private boolean initIOStream()
    {
        //return value for whether opening the streams is successful or not
        boolean successful = false;

        try {
            //
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            //writeData(0, 0);

            successful = true;
            return successful;
        }
        catch (IOException e) {
            System.out.println("I/O Streams failed to open. (" + e.toString() + ")");
            return successful;
        }
    }
    
    //starts the event listener that knows whenever data is available to be read
    //pre style="font-size: 11px;": an open serial port
    //post: an event listener for the serial port that knows when data is received
    private boolean initListener()
    {
        try
        {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        }
        catch (TooManyListenersException e)
        {
            System.out.println("Too many listeners. (" + e.toString() + ")");
            return false;
        }
        return true;
    }

}
