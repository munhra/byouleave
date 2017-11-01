#include <ESP8266WiFi.h>
#include <ArduinoOTA.h>

#define FOTA_HOST_NAME "FutureHouseGarageDoorESP"

const char* ssid     = "FollowMe-Pi3";
const char* password = "FollowMeRadio";

const int httpPort = 3000;
const char* host = "192.168.42.1";

int port = 12345;
String roomName = "garage";

int defautDelay = 500;
int oneSecondDelay = 1000;
int twoSecondDelay = 2000;

WiFiServer server(12345);
WiFiClient client;// Esp Client
const int REED_PIN = 2; // Reed Pin - ESP
int state;

void setup() {
  Serial.begin(115200);
  pinMode(REED_PIN,INPUT_PULLUP);
     
  setupWifi();
  sendRegister();
  setUpFota();

  delay(defautDelay);

  server.begin();
  server.setNoDelay(true);
  acceptClient();
}

void loop() {
   ArduinoOTA.handle();
    doorStatus();

 if(WiFi.status() == WL_DISCONNECTED){
    setupWifi();
      sendRegister();
  }

  if(!client.connected()){
    client.stop(); 
    acceptClient();
  }

  delay(defautDelay);
}

// Accept new Client
void acceptClient(){
while(!server.hasClient()){
  ArduinoOTA.handle();
    doorStatus();
    Serial.println("Wainting Client");
    delay(oneSecondDelay);
}
  // accept the client
  Serial.println("New Client");
  client = server.available();
  doorStatus();
  client.print("CONNECTED000");
  client.flush();
  delay(twoSecondDelay);

// send to the Client the door Status
  if(state==-1){
    client.print("CLOSE0000000");
    client.flush();
  }
  else{  
    client.print("OPEN00000000");
    client.flush();
  }
  delay(twoSecondDelay);
}

void setupWifi() {
  Serial.print("Connecting to ");
  Serial.println(ssid);
  
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(defautDelay);
    Serial.print(".");
  }

  Serial.println("WiFi connected");  
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

String ipToString(IPAddress ip){
  String s="";
  for (int i=0; i<4; i++)
    s += i  ? "." + String(ip[i]) : String(ip[i]);
  return s;
}

void sendRegister()
{
  WiFiClient client;
  const int httpPort = 3000;
  if (!client.connect(host, httpPort)) {
    Serial.println("connection failed");
    ESP.restart();
    return;
  }
  String url = "/api/Door/ip?ipDoor="+ipToString(WiFi.localIP());
  client.print(String("POST ") + url + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" + 
               "Connection: close\r\n\r\n");
  client.flush();
// SEND DOOR STATUS TO SERVER
  state = digitalRead(REED_PIN);
  if(state==LOW){
    sendDetectionPost("0");
     state = -1;
  }
else{  
  sendDetectionPost("1");
  state = 1;
}
  Serial.println("closing connection");
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
    cMac += ":";
  }
  cMac.toUpperCase();
  return cMac;
}

void sendDetectionPost(String detected) {
  WiFiClient client;
  const int httpPort = 3000;
  client.connect(host, httpPort);
  String url = "/api/sensor?roomName="+roomName+"&mac="+getMacAddress()+"&ip="+ipToString(WiFi.localIP())+"&presence="+detected;
  client.print(String("POST ") + url + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" + 
               "Connection: close\r\n\r\n");
  client.flush();
  delay(defautDelay);
}

void setUpFota() {
  ArduinoOTA.setHostname(FOTA_HOST_NAME);
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
}

void doorStatus(){
  if((digitalRead(REED_PIN)==LOW) && (state == 1)) {
    if(client.connected()){
      client.print("CLOSE0000000");
      client.flush();
    }
     sendDetectionPost("0"); 
     state = -1;
  }
  
  if((digitalRead(REED_PIN)==HIGH) && (state == -1)){
    if(client.connected()){
      client.print("OPEN00000000");
      client.flush();
    }
    sendDetectionPost("1"); 
    state = 1;
  }
}
