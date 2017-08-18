
#include <ESP8266WiFi.h>
#include <ArduinoOTA.h>
#include <ESP8266mDNS.h>
#include <WiFiUdp.h>
#include <ArduinoOTA.h>
#include <SoftwareSerial.h>

//wifi

const char* ssid     = "FollowMe-Pi3";
const char* password = "FollowMeRadio";
const char* host = "192.168.42.1";

//Huzza Feather 
//RX -> Pin 0
//TX -> Pin 1
// BLE RX -> BOARD TX
// BLE TX -> BOARD RX

boolean sendClosedBLEInfo = true;
boolean sendOpenedBLEInfo = true;
boolean stopWIFISearch = false;

const int REED_PIN = 2; // Pin connected to reed switch
const int LED_PIN = 0; // LED pin - active-high

SoftwareSerial BTSerial(0, 1); // RX | TX

String roomName = "garage";

void setup()
{
  BTSerial.begin(9600);  // HM-10 default speed in AT command more
  Serial.begin(9600);
  pinMode(REED_PIN, INPUT_PULLUP);
  pinMode(LED_PIN, OUTPUT);
  //digitalRead(REED_PIN)
  setupWifi();
  sendRegister();
  setupFOTA();
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
  stopWIFISearch = false;
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
      sendDetectionPost("0");
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
      sendDetectionPost("1"); 
    }
   
  }
  //delay(1000);
}

void setupWifi() {
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  
  WiFi.begin(ssid, password);
  
  while ((WiFi.status() != WL_CONNECTED) && (!stopWIFISearch)) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");  
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void setupFOTA() {
  // Port defaults to 8266
  // ArduinoOTA.setPort(8266);

  // Hostname defaults to esp8266-[ChipID]
     ArduinoOTA.setHostname("munhraESP8266");

  // No authentication by default
  // ArduinoOTA.setPassword((const char *)"123");

  ArduinoOTA.onStart([]() {
    Serial.println("Start");
  });
  ArduinoOTA.onEnd([]() {
    Serial.println("\nEnd");
  });
  ArduinoOTA.onProgress([](unsigned int progress, unsigned int total) {
    Serial.printf("Progress: %u%%\r", (progress / (total / 100)));
  });
  ArduinoOTA.onError([](ota_error_t error) {
    Serial.printf("Error[%u]: ", error);
    if (error == OTA_AUTH_ERROR) Serial.println("Auth Failed");
    else if (error == OTA_BEGIN_ERROR) Serial.println("Begin Failed");
    else if (error == OTA_CONNECT_ERROR) Serial.println("Connect Failed");
    else if (error == OTA_RECEIVE_ERROR) Serial.println("Receive Failed");
    else if (error == OTA_END_ERROR) Serial.println("End Failed");
  });
  ArduinoOTA.begin();
  Serial.println("Ready this one as uploaded by FOTA Sensor !!!");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
}

void sendRegister()
{
  Serial.print("connecting to ");
  Serial.println(host);
  
  // Use WiFiClient class to create TCP connections
  WiFiClient client;
  const int httpPort = 3000;
  
  
  if (!client.connect(host, httpPort)) {
    Serial.println("connection failed");
    setupWifi();
  }

  String url = "/api/sensor/register?roomName="+roomName+"&mac="+getMacAddress()+"&ip="+ipToString(WiFi.localIP());

  Serial.print("Requesting URL: ");
  Serial.println(url);
  client.print(String("POST ") + url + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" + 
               "Connection: close\r\n\r\n");
  Serial.println();
  Serial.println("closing connection");
}

void sendDetectionPost(String detected) {

  Serial.print("connecting to ");
  Serial.println(host);
  
  // Use WiFiClient class to create TCP connections
  WiFiClient client;
  const int httpPort = 3000;

  
  if (!client.connect(host, httpPort)) {
    Serial.println("connection failed");
    setupWifi();
  }

  String url = "/api/sensor?roomName="+roomName+"&mac="+getMacAddress()+"&ip="+ipToString(WiFi.localIP())+"&presence="+detected;

  Serial.print("Requesting URL: ");
  Serial.println(url);
  client.print(String("POST ") + url + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" + 
               "Connection: close\r\n\r\n");
  /*
  delay(500);

   while(client.available()){
    String line = client.readStringUntil('\r');
    Serial.print(line);
  }*/
  
  Serial.println();
  Serial.println("closing connection");

}

String ipToString(IPAddress ip){
  String s="";
  for (int i=0; i<4; i++)
    s += i  ? "." + String(ip[i]) : String(ip[i]);
  return s;
}

String getMacAddress() {
  byte mac[6];
  WiFi.macAddress(mac);
  String cMac = "";
  for (int i = 0; i < 6; ++i) {
    if (mac[i]<0x10) {cMac += "0";
   }
  cMac += String(mac[i],HEX);
  if(i<5)
    cMac += ":"; // put : or - if you want byte delimiters
  }
  cMac.toUpperCase();
  return cMac;
}

