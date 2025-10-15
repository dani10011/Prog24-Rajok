import time
import board
import busio
import adafruit_pn532.i2c
import requests
import argparse
import os

# Parse command-line arguments
parser = argparse.ArgumentParser(
    description="NFC Scanner for room entry (HCE compatible)"
)
parser.add_argument(
    "roomId", type=int, help="The ID of the room where the scanner is located"
)
args = parser.parse_args()

# Save roomId to variable and print it
room_id = args.roomId
print(f"Scanner initialized for Room ID: {room_id}")

# Load API URL from environment
API_BASE_URL = "https://72ee88981b0e.ngrok-free.app"
API_URL = f"{API_BASE_URL}/api/RoomEntryRequest/CreateRequest"

i2c = busio.I2C(board.SCL, board.SDA)
pn532 = adafruit_pn532.i2c.PN532_I2C(i2c, debug=False)

pn532.SAM_configuration()

# ClassMaster Pro AID (must match the Android app)
CLASSMASTER_AID = bytes([0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06])

print("NFC Reader ready. Waiting for tag or HCE device...")
print("Compatible with both physical NFC tags and smartphones with HCE")


def release_target():
    """Release the current target to reset PN532 state"""
    try:
        # InRelease command = 0x52
        pn532.call_function(0x52, params=[0x01], response_length=10)
    except:
        pass  # Ignore errors during release


def read_hce_uid(target):
    """
    Read UID from HCE device using APDU commands
    Returns the custom UID broadcasted by the ClassMaster Pro app
    """
    try:
        # Build SELECT APDU command
        # Format: CLA INS P1 P2 LC AID
        select_apdu = (
            bytes(
                [
                    0x00,  # CLA
                    0xA4,  # INS (SELECT)
                    0x04,  # P1 (Select by name)
                    0x00,  # P2
                    len(CLASSMASTER_AID),  # LC (length of AID)
                ]
            )
            + CLASSMASTER_AID
        )

        print(f"  Sending SELECT command: {select_apdu.hex().upper()}")

        # Send SELECT command using call_function
        # InDataExchange command = 0x40
        # Format: [0x40, Tg (target number, always 1), APDU...]
        try:
            response = pn532.call_function(
                0x40,  # InDataExchange
                params=[0x01] + list(select_apdu),  # Tg=1 + APDU
                response_length=255,
            )
        except RuntimeError as e:
            # Physical tags don't understand APDU - this is normal
            print(f"  Device doesn't support APDU (likely physical tag)")
            release_target()  # Reset state for next scan
            return None

        if response is None or len(response) < 3:
            print("  No response to SELECT command")
            release_target()
            return None

        # Response format: [Status, Data...]
        # Status byte: 0x00 = success
        if response[0] != 0x00:
            print(f"  SELECT exchange failed. Status: {response[0]:02X}")
            release_target()
            return None

        # Extract data (skip status byte)
        data = response[1:]

        # Check APDU status word (last 2 bytes should be 90 00 for success)
        if len(data) < 2:
            print("  SELECT response too short")
            release_target()
            return None

        status = data[-2:]
        if status != bytes([0x90, 0x00]):
            print(f"  SELECT failed. APDU Status: {status.hex().upper()}")
            release_target()
            return None

        print("  SELECT successful")

        # Send READ command to get UID
        # Simple read command
        read_apdu = bytes(
            [
                0x00,  # CLA
                0xB0,  # INS (READ BINARY)
                0x00,  # P1
                0x00,  # P2
                0x07,  # LE (expected length - 7 bytes for UID)
            ]
        )

        print(f"  Sending READ command: {read_apdu.hex().upper()}")

        try:
            response = pn532.call_function(
                0x40,  # InDataExchange
                params=[0x01] + list(read_apdu),  # Tg=1 + APDU
                response_length=255,
            )
        except RuntimeError as e:
            print(f"  READ command failed (device disconnected?)")
            release_target()
            return None

        if response is None or len(response) < 3:
            print("  No response to READ command")
            release_target()
            return None

        # Check status byte
        if response[0] != 0x00:
            print(f"  READ exchange failed. Status: {response[0]:02X}")
            release_target()
            return None

        # Extract data (skip status byte)
        data = response[1:]

        if len(data) < 2:
            print("  READ response too short")
            release_target()
            return None

        # Extract UID (everything except last 2 status bytes)
        uid_bytes = data[:-2]
        status = data[-2:]

        if status != bytes([0x90, 0x00]):
            print(f"  READ failed. APDU Status: {status.hex().upper()}")
            release_target()
            return None

        if len(uid_bytes) != 7:
            print(f"  Unexpected UID length: {len(uid_bytes)} bytes (expected 7)")
            print(f"  Received data: {data.hex().upper()}")
            release_target()
            return None

        # Convert to hex string
        uid_hex = uid_bytes.hex().upper()
        print(f"  ✓ HCE UID received: {uid_hex}")

        return uid_hex

    except Exception as e:
        print(f"  Error communicating with HCE device: {e}")
        release_target()  # Always release on error
        return None


last_uid = None
last_scan_time = 0
SCAN_COOLDOWN = 3  # seconds between scans of the same tag

while True:
    # Try to detect card/device
    # For HCE, we need to use in_list_one to get the target info
    uid = pn532.read_passive_target(timeout=0.5)

    if uid is not None:
        current_time = time.time()
        uid_hex = "".join([hex(i)[2:].zfill(2) for i in uid]).upper()

        # Skip if same tag scanned too recently
        if uid_hex == last_uid and (current_time - last_scan_time) < SCAN_COOLDOWN:
            continue

        print(f"\n📱 Device detected!")
        print(f"  Hardware UID: {uid_hex}")

        # Try to communicate via APDU (for HCE devices)
        print("  Attempting HCE communication...")
        hce_uid = read_hce_uid(uid)

        # Determine which UID to use
        final_uid = None
        device_type = "Unknown"

        if hce_uid is not None:
            # Successfully read from HCE - use custom UID
            final_uid = hce_uid
            device_type = "Smartphone (HCE)"
            print(f"  ✓ Using HCE custom UID: {final_uid}")
        else:
            # Failed HCE communication - use hardware UID (physical tag)
            final_uid = uid_hex
            device_type = "Physical NFC Tag"
            print(f"  ✓ Using hardware UID: {final_uid}")

        # Update last scan tracking
        last_uid = uid_hex
        last_scan_time = current_time

        print(f"\n🔑 Final UID: {final_uid} ({device_type})")

        try:
            # Prepare request payload
            payload = {"nfcId": final_uid, "roomId": room_id}

            # Send POST request to create room entry request
            print(f"📤 Sending to API: {payload}")
            response = requests.post(API_URL, json=payload, timeout=5)
            print(f"✅ API Response: {response.status_code}")

            if response.status_code == 200:
                print("✓ Entry request created successfully!")
            else:
                print(f"⚠ API returned status {response.status_code}")
                try:
                    print(f"   Response: {response.text}")
                except:
                    pass

        except requests.exceptions.Timeout:
            print("❌ API request timed out")
        except requests.exceptions.RequestException as e:
            print(f"❌ API request failed: {e}")
        except Exception as e:
            print(f"❌ Error sending to API: {e}")

        print("\n" + "=" * 50)
        print("Waiting for next scan...\n")

        # Small delay before next scan
        time.sleep(0.5)

    # Small delay to prevent CPU spinning
    time.sleep(0.1)
