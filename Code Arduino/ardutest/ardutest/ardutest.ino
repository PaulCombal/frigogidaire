#include <elapsedMillis.h>
#include "DHT.h"   // Librairie des capteurs DHT 

#define DHTPIN 2    // Changer le pin sur lequel est branché le DHT
#define PELTIER_PIN 5
#define DHTTYPE DHT22 // DHT 22  (AM2302)
#define DEBUG false
#define VERBOSE false

const byte numChars = 32;
char receivedChars[numChars]; // an array to store the received data
elapsedMillis elapsed;

float int_temperature = 0;
float ext_temperature = 0;
float pel_temperature = 0;
float humidity = 0;
float instruction = 15;
float dewPoint = 15;

boolean newData = false;
boolean overrideStop = false;

const double A = 0.002758188184454833;
const double B = 0.000823965492134896;
const double C = -0.00019141205692228488;

double result;
float readingAnalog;

DHT dht(DHTPIN, DHTTYPE); 
  
// ENTRY POINTS

void setup() {
 Serial.begin(9600);
 Serial.println("<Arduino is ready>");
 dht.begin();
}

void loop() {
 //Takes account of the new instruction if there is one
 handleInput();

 //Takes a new temperature and humidity record
 takeNewRecord();

 //Modulates the temperature depending on the readings
 modulatePeltier();
 
 //Outputs the data to the serial port
 showNewData();

 //Wait in milliseconds
 delay(500);
}

void handleInput() {
 static byte ndx = 0;
 static char endMarker = ';';
 char rc;

  //If nothing has been received within 500ms, then return 
  elapsed = 0;
  
 while (Serial.available() > 0 && newData == false || (ndx == 0 && elapsed > 500)) {
   rc = Serial.read();
  
   if (rc != endMarker) {
     receivedChars[ndx] = rc;
     ndx++;
     if (ndx >= numChars) {
      ndx = numChars - 1;
     }
   }
   else {
     receivedChars[ndx] = '\0'; // terminate the string
     ndx = 0;
     newData = true;

     //The received data is the instruction, nothing more
     instruction = atoi(receivedChars);
   }
 }
}

/**
 * Hygromètre -> pin pwm 2
 * thermistance exterirur -> pin A0
 * thermisatnce peltier -> pin A1
 */
void takeNewRecord() {
  if (DEBUG) {
    humidity = instruction * 2;
    int_temperature = instruction / 2;
  } 
  else {
    // Hygromètre
    // Délai de 2 secondes entre chaque mesure. La lecture prend 250 millisecondes
    delay(2000);
   
    // Lecture du taux d'humidité
    humidity = dht.readHumidity();
    // Lecture de la température en Celcius
    ext_temperature = dht.readTemperature();
    // Pour lire la température en Fahrenheit
    //float f = dht.readTemperature(true);
    
    // Stop le programme et renvoie un message d'erreur si le capteur ne renvoie aucune mesure
    if (isnan(humidity) || isnan(ext_temperature)/* || isnan(f)*/) {
      if(VERBOSE) {
        Serial.println("Echec de lecture des informations !"); 
      }
      
      return; //on garde les anciennes valeurs
    }
   
    // Calcul la température ressentie. Il calcul est effectué à partir de la température en Fahrenheit
    // On fait la conversion en Celcius dans la foulée
    //float hi = dht.computeHeatIndex(f, h);

    // THERMISTANCES

    //THERMI 1

    readingAnalog = analogRead(0);
    readingAnalog = readingAnalog * 5 / 1023;
  
    //Serial.print("Voltage: ");
    //Serial.print(reading);
    //Serial.print("\t");
    
    //1/T = A + B*ln(R) + C*ln(R)3
    //T = 1/ ( A + B*ln(R) + C*ln(R)3)
    result = 1 / (A + B*log(readingAnalog) + C*pow(log(readingAnalog), 3));
  
    //Conversion de Kelvin en Celsius
    result -= 273.15;
    
    //Serial.print("Température: ");
    //Serial.println(result);

    int_temperature = result;

    //THERMI 2

    readingAnalog = analogRead(1);
    readingAnalog = readingAnalog * 5 / 1023;
  
    //Serial.print("Voltage: ");
    //Serial.print(reading);
    //Serial.print("\t");
    
    //1/T = A + B*ln(R) + C*ln(R)3
    //T = 1/ ( A + B*ln(R) + C*ln(R)3)
    result = 1 / (A + B*log(readingAnalog) + C*pow(log(readingAnalog), 3));
  
    //Conversion de Kelvin en Celsius
    result -= 273.15;
    
    //Serial.print("Température: ");
    //Serial.println(result);

    pel_temperature = result;

    //CALCULATE DEW POINT AND LOWER INSTRUCTION IF NECESSARY

    //TODO IS HUMIDITY 0-100 OR 0-1?
    //SEE https://fr.wikipedia.org/wiki/Point_de_ros%C3%A9e#Calcul
    
    dewPoint = 237.7 * magnus_alpha(int_temperature, humidity / 100);
    dewPoint = dewPoint / (17.27 - magnus_alpha(int_temperature, humidity / 100));

    //We should never reach the dew point.

    if(int_temperature < dewPoint) {
      overrideStop = true;
    }

    if(VERBOSE) {
      printVerbose();
    }

  }
}

float magnus_alpha(float t, float rh) {
  return (17.27 * t) / (237.7 + t) + log(rh);
}

void showNewData() {
    Serial.println(String(humidity) + ";" + String(int_temperature) + ";" + String(ext_temperature) + ";" + String(pel_temperature));
    newData = false;
}

void modulatePeltier() {
  if(!DEBUG) {
    if(int_temperature > instruction && !overrideStop) {
      //On refroidit
      analogWrite(PELTIER_PIN, HIGH);
      if(VERBOSE) {
        Serial.println("Je refroidit le module");
      }
    }
    else {
      //On réchauffe
      analogWrite(PELTIER_PIN, LOW);
      if(VERBOSE) {
        Serial.println("Je ne refroidit pas le module");
      }
    } 
  }

  overrideStop = false;
}

void printVerbose() {
  Serial.print("Température pel:");
  Serial.println(pel_temperature);

  Serial.print("Température int:");
  Serial.println(int_temperature);

  Serial.print("Température hygro:");
  Serial.println(ext_temperature);

  Serial.print("Taux d'humidité");
  Serial.println(humidity);

  Serial.print("Point de rosée:");
  Serial.println(dewPoint);
}

