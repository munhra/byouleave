
#include <ESP8266WiFi.h>
#include <ArduinoOTA.h>
#include <ESP8266mDNS.h>
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

const int REED_PIN = 2; // Pin connected to reed switch

SoftwareSerial BTSerial(0, 1); // RX | TX

String roomName = "garage";

void setup()
{
  BTSerial.begin(9600);  // HM-10 default speed in AT command more
  Serial.begin(9600);
  pinMode(REED_PIN, INPUT_PULLUP);
  setupWifi();
  //sendRegister();
  setupFOTA();
}

void loop()
{
  int proximity = digitalRead(REED_PIN); // Read the state of the switch
  if (proximity == LOW) // If the pin reads low, the switch is closed.
  {
    if (sendClosedBLEInfo) {
      Serial.println("Switch CLOSED");
      BTSerial.write("Close");
      sendClosedBLEInfo = false;
      sendOpenedBLEInfo = true;
      delay(100);
      sendDetectionPost("0");
    }
  }
  else
  {
    if (sendOpenedBLEInfo) {
      Serial.println("Switch OPEN");
      BTSerial.write("Open");
      sendClosedBLEInfo = true;
      sendOpenedBLEInfo = false;   
      delay(100);
      sendDetectionPost("1"); 
    } 
  }
}

void setupWifi() {
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");  
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void setupFOTA() {
  ArduinoOTA.setHostname("FutureHouseBFyouLeaveDoor");
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
  WiFiClient client;
  const int httpPort = 3000;
  client.connect(host, httpPort);
  String url = "/api/sensor/register?roomName="+roomName+"&mac="+getMacAddress()+"&ip="+ipToString(WiFi.localIP());
  client.print(String("POST ") + url + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" + 
               "Connection: close\r\n\r\n");
}

void sendDetectionPost(String detected) {
  WiFiClient client;
  const int httpPort = 3000;
  client.connect(host, httpPort);
  String url = "/api/sensor?roomName="+roomName+"&mac="+getMacAddress()+"&ip="+ipToString(WiFi.localIP())+"&presence="+detected;
  client.print(String("POST ") + url + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" + 
               "Connection: close\r\n\r\n");
  delay(500);
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

