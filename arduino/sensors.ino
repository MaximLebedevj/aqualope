#include <SPI.h>
#include <Ethernet.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>

// Ethernet
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };

// MQTT
//#define MQTT_BROKER "test.mosquitto.org"
#define MQTT_BROKER "147.45.126.9"
#define MQTT_PORT 1883
#define MQTT_TOPIC_PUBLISH "topic/water_quality"
#define MQTT_TOPIC_SUBSCRIBE "topic/arduino_control"
#define CLIENT_ID_PREFIX "ArduinoClient-"
#define BUFFER_SIZE 96
#define RECONNECT_DELAY 5000

// MQTT publish
#define SEND_INTERVAL 7000
#define JSON_DOC_SIZE 96

// LED Pin
#define LED_PIN 8

struct SensorData {
    float temperature;
    float oxygen;
    float pressure;
    float orp;
    float salinity;
    float water_level;
    float turbidity;
    float ammonia;
    float nitrate;
};

void setupEthernet(void);
void setupMQTT(void);
bool connectToMQTT(void);
void readSensors(struct SensorData *data);
bool sendSensorData(const struct SensorData *data);
float roundFloat(float value, int decimals);
void generateRandomSensorData(struct SensorData *data);
void callback(char* topic, byte* payload, unsigned int length);

EthernetClient ethClient;
PubSubClient mqttClient(ethClient);
unsigned long previousMillis = 0;

void setup() {

    pinMode(LED_PIN, OUTPUT);

    Serial.begin(9600);
    while (!Serial) {;}

    setupEthernet();
    setupMQTT();
    connectToMQTT();
}

void loop() {
    Ethernet.maintain();

    if (!mqttClient.connected()) {
        connectToMQTT();
    }

    mqttClient.loop();

    unsigned long currentMillis = millis();
    if (currentMillis - previousMillis >= SEND_INTERVAL) {
        previousMillis = currentMillis;

        struct SensorData data;
        readSensors(&data);
        sendSensorData(&data);
    }
}

void setupEthernet(void) {
    Serial.println("Init Ethernet...");
    Ethernet.begin(mac);
    delay(1000);
    Serial.print("IP: ");
    Serial.println(Ethernet.localIP());
}

void setupMQTT(void) {
    mqttClient.setServer(MQTT_BROKER, MQTT_PORT);
    mqttClient.setCallback(callback);
}

bool connectToMQTT(void) {
    Serial.println("Connecting to MQTT broker...");

    String clientId = CLIENT_ID_PREFIX;
    clientId += String(random(0xffff), HEX);

    if (mqttClient.connect(clientId.c_str())) {
        Serial.println("MQTT connected");

        if (mqttClient.subscribe(MQTT_TOPIC_SUBSCRIBE)) {
            Serial.print("Subscribed to ");
            Serial.println(MQTT_TOPIC_SUBSCRIBE);
        } else {
            Serial.println("Failed to subscribe");
        }

        return true;
    } else {
        Serial.print("Failed, rc=");
        Serial.print(mqttClient.state());
        Serial.println(" Retrying in 5 seconds");
        delay(RECONNECT_DELAY);
        return false;
    }
}

bool ledState = false;

void callback(char* topic, byte* payload, unsigned int length) {
    Serial.print("Message arrived [");
    Serial.print(topic);
    Serial.print("] ");

    char message[length + 1];
    for (unsigned int i = 0; i < length; i++) {
        message[i] = (char)payload[i];
        Serial.print((char)payload[i]);
    }
    message[length] = '\0';
    Serial.println();

    StaticJsonDocument<JSON_DOC_SIZE> doc;
    DeserializationError error = deserializeJson(doc, message);

    if (error) {
        Serial.print("deserializeJson() failed: ");
        Serial.println(error.c_str());
        return;
    }

    if (doc.containsKey("command")) {
        const char* command = doc["command"];

        if (strcmp(command, "led_on") == 0) {
            if (!ledState) {
                digitalWrite(LED_PIN, HIGH);
                ledState = true;
                Serial.println("LED turned ON");
            }
        }
        else if (strcmp(command, "led_off") == 0) {
            if (ledState) {
                digitalWrite(LED_PIN, LOW);
                ledState = false;
                Serial.println("LED turned OFF");
            }
        }
    }
}

void readSensors(struct SensorData *data) {
    generateRandomSensorData(data);
}

void generateRandomSensorData(struct SensorData *data) {
    data->temperature = roundFloat(random(1800, 3000) / 100.0, 2);
    data->oxygen = roundFloat(random(8000, 10000) / 100.0, 2);
    data->pressure = roundFloat(random(650, 850) / 100.0, 2);
    data->orp = roundFloat(random(200, 800), 0);
    data->salinity = roundFloat(random(10, 500) / 100.0, 2);
    data->water_level = roundFloat(random(0, 10000) / 100.0, 2);
    data->turbidity = roundFloat(random(0, 10000) / 100.0, 2);
    data->ammonia = roundFloat(random(0, 500) / 100.0, 2);
    data->nitrate = roundFloat(random(0, 100) / 100.0, 2);
}

bool sendSensorData(const struct SensorData *data) {
    StaticJsonDocument<JSON_DOC_SIZE> doc;

    doc["te"] = data->temperature;
    doc["ox"] = data->oxygen;
    doc["p"] = data->pressure;
    doc["or"] = data->orp;
    doc["s"] = data->salinity;
    doc["w"] = data->water_level;
    doc["tu"] = data->turbidity;
    doc["a"] = data->ammonia;
    doc["n"] = data->nitrate;

    char buffer[BUFFER_SIZE];
    size_t n = serializeJson(doc, buffer);

    Serial.print("n = ");
    Serial.println(n);
    Serial.print("Publishing: ");
    Serial.println(buffer);

    if (mqttClient.publish(MQTT_TOPIC_PUBLISH, buffer, n)) {
        Serial.println("Publish success");
        return true;
    } else {
        Serial.println("Publish failed");
        return false;
    }
}

float roundFloat(float value, int decimals) {
    long multiplier = 1;
    for (int i = 0; i < decimals; i++) {
        multiplier *= 10;
    }
    long tempValue = (long)(value * multiplier);
    return (float)tempValue / multiplier;
}
