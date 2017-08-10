#include <SoftwareSerial.h>
//Huzza Feather 
//RX -> Pin 0
//TX -> Pin 1
// BLE RX -> BOARD TX
// BLE TX -> BOARD RX
bool closed = true;

SoftwareSerial BTSerial(0, 1); // RX | TX

void setup()
{
  BTSerial.begin(9600);  // HM-10 default speed in AT command more
}

void loop()
{
  if (closed) {
    BTSerial.write("Open");  
  }else{
    BTSerial.write("Close");
  }
  closed = !closed;
  delay(10000);
}
