#include <SoftwareSerial.h>
//Huzza Feather 
//RX -> Pin 0
//TX -> Pin 1
// BLE RX -> BOARD TX
// BLE TX -> BOARD RX

boolean sendClosedBLEInfo = true;
boolean sendOpenedBLEInfo = true;

const int REED_PIN = 2; // Pin connected to reed switch
const int LED_PIN = 0; // LED pin - active-high

SoftwareSerial BTSerial(0, 1); // RX | TX

void setup()
{
  BTSerial.begin(9600);  // HM-10 default speed in AT command more
  Serial.begin(9600);
  pinMode(REED_PIN, INPUT_PULLUP);
  pinMode(LED_PIN, OUTPUT);
  //digitalRead(REED_PIN)
}

void loop()
{
  /*
  if (closed) {
    BTSerial.write("Open");  
  }else{
    BTSerial.write("Close");
  }
  closed = !closed;
  delay(10000);
  */

  int proximity = digitalRead(REED_PIN); // Read the state of the switch
  if (proximity == LOW) // If the pin reads low, the switch is closed.
  {
    //Serial.println("Switch CLOSED");
    digitalWrite(LED_PIN, HIGH); // Turn the LED on
    if (sendClosedBLEInfo) {
      Serial.println("Switch CLOSED");
      BTSerial.write("Close");
      sendClosedBLEInfo = false;
      sendOpenedBLEInfo = true;
    }
  }
  else
  {
    //Serial.println("Switch OPEN");
    digitalWrite(LED_PIN, LOW); // Turn the LED off
    if (sendOpenedBLEInfo) {
      Serial.println("Switch OPEN");
      BTSerial.write("Open");
      sendClosedBLEInfo = true;
      sendOpenedBLEInfo = false;    
    }
   
  }
  //delay(1000);
}
