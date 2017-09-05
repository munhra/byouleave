#include <ESP8266WiFi.h>

/*================VARIABLES==================*/
const char* ssid     = "Jonaphael ESP";
const char* password = "esp8266esp";
int port = 12345;
WiFiServer server(12345);
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
/*-------------------------------------------*/
  
/*-------------------------------------------*/
  server.begin();
  server.setNoDelay(true);
  Serial.print("Ready! Use port:");
  Serial.println("'port'to connect");
  acceptClient();
/*------------------------------------------*/
}
/*=================VOID LOOP====================*/

void loop() {

  while((client.connected()) && (WiFi.status() == WL_CONNECTED)){
    // envia os dados reecebidos do REED
   // state = digitalRead(REED_PIN);  
    if(digitalRead(REED_PIN)==HIGH){
       Serial.println("Porta Fechada");
       client.print("Porta Fechada");
    }
    else{
      Serial.println("Porta Aberta");
      client.print("Porta Aberta");
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
  delay(2000);
}
