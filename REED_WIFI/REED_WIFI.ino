
#include <ESP8266WiFi.h>

/*================VARIABLES==================*/
/*const char* ssid     = "FollowMe-Pi3";
const char* password = "FollowMeRadio";*/
const char* ssid     = "Jonaphael ESP";
const char* password = "esp8266esp";
const int httpPort = 3000;
const char* host = "192.168.137.1";
int port = 12345;
String roomName = "garage";
WiFiServer server(12345);
//HttpClient clienthttp;// = HttpClient(wifi, host, httpPort);
WiFiClient client;
const int REED_PIN = 2; // Pin connected to reed switch
int state;
/*===========================================*/

/*================VOID SETUP=================*/
void setup() {
  Serial.begin(9600);
  Serial.begin(115200);
  pinMode(REED_PIN,INPUT_PULLUP);
  delay(100);
  
/*-------------------------------------------*/
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  wifiConection();
  sendRegister();
/*-------------------------------------------*/
  
/*-------------------------------------------*/
  server.begin();
  server.setNoDelay(true);
  Serial.print("Ready! Use port:");
  Serial.println("'port'to connect");
  acceptClient();
/*------------------------------------------*/
state = digitalRead(REED_PIN);
if(state==LOW){
   client.print("Porta Fechada");
   state = -1;
}
else{
  client.print("Porta Aberta");
  state = 1;
}
}
/*=================VOID LOOP====================*/

void loop() {
delay(500);
  while((client.connected()) && (WiFi.status() == WL_CONNECTED)){
    // envia os dados reecebidos do REED
   // state = digitalRead(REED_PIN);
   
    if((digitalRead(REED_PIN)==LOW) && (state == 1)) {
       Serial.println("Porta Fechada");
       client.print("Porta Fechada");
       state = -1;
    }
    
    if((digitalRead(REED_PIN)==HIGH) && (state == -1)){
      Serial.println("Porta Aberta");
      client.print("Porta Aberta");
      state = 1;
    }
    delay(500);
  }

      

  if(!client.connected()){
    client.stop(); 
    acceptClient();
  }
  
  if(WiFi.status() != WL_CONNECTED){
    wifiConection();
  }
}

/*=================METHODS====================*/
//Connect To Wifi
void wifiConection(){
  while(WiFi.status() != WL_CONNECTED){
  delay(500);
  Serial.print('.');
  }
  Serial.println("");
  Serial.println("WiFi connected");  
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());  
}

// Accept Waiting for Client
void acceptClient(){
while(!server.hasClient()){
    Serial.println("Wainting for Client");
    delay(2000);
}
  // accept the client
  Serial.println("New Client");
  client = server.available();
  client.print("Conectado0000");
  delay(2000);

//send the door state
state = digitalRead(REED_PIN);
  if(state==LOW){
   client.print("Porta Fechada");
   state = -1;
}
else{
  client.print("Porta Aberta");
  state = 1;
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

String ipToString(IPAddress ip){
  String s="";
  for (int i=0; i<4; i++)
    s += i  ? "." + String(ip[i]) : String(ip[i]);
  return s;
}

/*void sendRegister()
{
  Serial.print("connecting to ");
  Serial.println(host);
 
  WiFiClient client;
  const int httpPort = 3000;
  
  if (!client.connect(host, httpPort)) {
    Serial.println("connection failed");
    ESP.restart();
    return;
  }

   String url = "/api/sensor/ip?ipDoor="+ipToString(WiFi.localIP());

  Serial.print("Requesting URL: ");
  Serial.println(url);
  client.print(String("POST ") + url + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" + 
               "Connection: close\r\n\r\n");
  Serial.println();
  Serial.println(client.read());
  Serial.println("closing connection");
}*/

void sendRegister()
{
  Serial.print("connecting to ");
  Serial.println(host);
  WiFiClient client;
  const int httpPort = 3000;
  if (!client.connect(host, httpPort)) {
    Serial.println("connection failed");
    ESP.restart();
    return;
  }
  String url = "/api/sensor/ip?ipDoor="+ipToString(WiFi.localIP());
  Serial.print("Requesting URL: ");
  Serial.println(url);
  client.print(String("POST ") + url + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" + 
               "Connection: close\r\n\r\n");
  Serial.println();
  Serial.println(client.read());
  Serial.println("closing connection");
}
