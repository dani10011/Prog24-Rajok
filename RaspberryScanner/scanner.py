import time
import board
import busio
import adafruit_pn532.i2c
import requests


i2c = busio.I2C(board.SCL, board.SDA)
pn532 = adafruit_pn532.i2c.PN532_I2C(i2c, debug=False)

pn532.SAM_configuration()

print("NFC Reader ready. Waiting for tag...")

while True:
    uid = pn532.read_passive_target(timeout=0.5)
    if uid is not None:
        uid_hex = "".join([hex(i)[2:].zfill(2) for i in uid]).upper()
        print(f"Tag detected: {uid_hex}")
        time.sleep(1)


"""
        try:
            response = requests.post(API_URL, json={"uid": uid_hex})
            print(f"Sent UID {uid_hex} → Response: {response.status_code}")
        except Exception as e:
            print(f"Error sending UID: {e}")
"""
