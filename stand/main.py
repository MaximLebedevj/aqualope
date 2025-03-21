import paho.mqtt.client as mqtt
import time
import random
import json


broker = "test.mosquitto.org"
port = 1883
topic = "topic/water_quality"


def generate_sensor_data():
    data = {
        "te": round(random.uniform(18.0, 30.0), 2),
        "ox": round(random.uniform(80, 100), 2),
        "p": round(random.uniform(6.5, 8.5), 2),
        "or": round(random.uniform(200, 800), 2),
        "s": round(random.uniform(0.1, 5.0), 2),
        "w": round(random.uniform(0, 100), 2),
        "tu": round(random.uniform(0, 100), 2),
        "a": round(random.uniform(0, 5.0), 2),
        "n": round(random.uniform(0, 1.0), 2)
    }
    return data

client = mqtt.Client()

client.connect(broker, port, 60)

client.loop_start()

while True:
    sensor_data = generate_sensor_data()
    payload = json.dumps(sensor_data)
    print("Publishing:", payload)
    client.publish(topic, payload)
    time.sleep(1)
