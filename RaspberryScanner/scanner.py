import time
import board
import busio
import adafruit_pn532.i2c
import requests
import argparse
import os

# Parse command-line arguments
parser = argparse.ArgumentParser(description="NFC Scanner for room entry")
parser.add_argument(
    "roomId", type=int, help="The ID of the room where the scanner is located"
)
args = parser.parse_args()

# Save roomId to variable and print it
room_id = args.roomId
print(f"Scanner initialized for Room ID: {room_id}")

# Load API URL from environment
API_BASE_URL = "https://09cc208360a9.ngrok-free.app"
API_URL = f"{API_BASE_URL}/api/RoomEntryRequest/CreateRequest"

i2c = busio.I2C(board.SCL, board.SDA)
pn532 = adafruit_pn532.i2c.PN532_I2C(i2c, debug=False)

pn532.SAM_configuration()

print("NFC Reader ready. Waiting for tag...")

while True:
    uid = pn532.read_passive_target(timeout=5)
    if uid is not None:
        uid_hex = "".join([hex(i)[2:].zfill(2) for i in uid]).upper()
        print(f"Tag detected: {uid_hex}")

        try:
            # Prepare request payload
            payload = {"nfcId": uid_hex, "roomId": room_id}

            # Send POST request to create room entry request
            response = requests.post(API_URL, json=payload)
            print(f"API Response: {response.status_code}")

            if response.status_code == 200:
                print(f"Entry request created successfully for NFC ID: {uid_hex}")
                try:
                    print(f"Response Data: {response.json()}")
                except:
                    print("No JSON response body")
            else:
                print(f"Error: {response.text}")
        except Exception as e:
            print(f"Error calling API: {e}")

        time.sleep(1)
