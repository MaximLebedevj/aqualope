import paho.mqtt.client as mqtt
import time
import random
import json


broker = "broker.emqx.io"
port = 1883
topic = "topic/water_quality"


def generate_sensor_data():
    data = {
        "temperature": round(random.uniform(18.0, 30.0), 2),
        "oxygen_saturation": round(random.uniform(80, 100), 2),
        "pH": round(random.uniform(6.5, 8.5), 2),
        "orp": round(random.uniform(200, 800), 2),
        "salinity": round(random.uniform(0.1, 5.0), 2),
        "water_level": round(random.uniform(0, 100), 2),
        "turbidity": round(random.uniform(0, 100), 2),
        "ammonia": round(random.uniform(0, 5.0), 2),
        "nitrites": round(random.uniform(0, 1.0), 2),
        "timestamp": int(time.time())
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
    time.sleep(5)
