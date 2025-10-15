package com.example.classmasterpro.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

/**
 * NFC Host Card Emulation Service
 * Makes the phone act as an NFC tag that broadcasts a custom UID
 */
class CardEmulationService : HostApduService() {

    companion object {
        private const val TAG = "CardEmulationService"

        // APDU command constants
        private const val SELECT_APDU_HEADER = "00A40400"
        private const val READ_BINARY_HEADER = "00B0"

        // Status codes
        private val SELECT_OK_SW = hexStringToByteArray("9000")
        private val UNKNOWN_CMD_SW = hexStringToByteArray("0000")

        // Application ID (AID) - must match aid_list.xml
        private const val SAMPLE_AID = "F0010203040506"

        // Custom UID - 7 bytes (typical for MIFARE Ultralight/NTAG)
        // Format: Manufacturer byte + 6-byte UID
        // Default test UID: 04:12:34:56:78:9A:BC
        private var customUid = byteArrayOf(
            0x04.toByte(), // NXP manufacturer code
            0x12.toByte(),
            0x34.toByte(),
            0x56.toByte(),
            0x78.toByte(),
            0x9A.toByte(),
            0xBC.toByte()
        )

        /**
         * Set a custom UID to broadcast (7 bytes)
         * @param uid ByteArray of 7 bytes, or hex string like "04:12:34:56:78:9A:BC"
         */
        fun setCustomUid(uid: String) {
            try {
                val bytes = uid.replace(":", "")
                    .replace(" ", "")
                    .replace("-", "")
                    .chunked(2)
                    .map { it.toInt(16).toByte() }
                    .toByteArray()

                if (bytes.size == 7) {
                    customUid = bytes
                    Log.d(TAG, "Updated UID to: ${getCustomUidString()}")
                } else {
                    Log.e(TAG, "UID must be 7 bytes, got ${bytes.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing UID: ${e.message}")
            }
        }

        /**
         * Set a custom UID from a numeric student ID
         * @param studentId Numeric ID (will be encoded into bytes)
         */
        fun setUidFromStudentId(studentId: Int) {
            // Encode student ID into last 4 bytes, keep manufacturer code
            customUid = byteArrayOf(
                0x04.toByte(), // NXP manufacturer
                0x43.toByte(), // 'C' for ClassMaster
                0x50.toByte(), // 'P' for Pro
                ((studentId shr 24) and 0xFF).toByte(),
                ((studentId shr 16) and 0xFF).toByte(),
                ((studentId shr 8) and 0xFF).toByte(),
                (studentId and 0xFF).toByte()
            )
            Log.d(TAG, "Set UID from student ID $studentId: ${getCustomUidString()}")
        }

        /**
         * Set fixed UID for testing
         */
        fun setUidFromPhoneId(phoneId: String) {
            // Fixed UID: 04:A3:B2:C1:D4:E5:F6
            customUid = byteArrayOf(
                0x04.toByte(),
                0xA3.toByte(),
                0xB2.toByte(),
                0xC1.toByte(),
                0xD4.toByte(),
                0xE5.toByte(),
                0xF6.toByte()
            )
            Log.d(TAG, "Set fixed UID: ${getCustomUidString()}")
        }

        /**
         * Get the current UID as a hex string
         */
        fun getCustomUidString(): String {
            return customUid.joinToString(":") { String.format("%02X", it) }
        }

        /**
         * Get the current UID as byte array
         */
        fun getCustomUid(): ByteArray = customUid.copyOf()

        private fun hexStringToByteArray(s: String): ByteArray {
            val len = s.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }

        private fun byteArrayToHexString(bytes: ByteArray): String {
            val hexArray = "0123456789ABCDEF".toCharArray()
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v = bytes[j].toInt() and 0xFF
                hexChars[j * 2] = hexArray[v ushr 4]
                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
            }
            return String(hexChars)
        }
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "HCE deactivated. Reason: $reason")
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            return UNKNOWN_CMD_SW
        }

        val hexCommandApdu = byteArrayToHexString(commandApdu)
        Log.d(TAG, "Received APDU: $hexCommandApdu")

        // Check if this is a SELECT command for our AID
        if (hexCommandApdu.startsWith(SELECT_APDU_HEADER)) {
            Log.d(TAG, "SELECT command received - sending OK")
            return SELECT_OK_SW
        }

        // For any other command, return our custom UID
        Log.d(TAG, "Data request - sending UID: ${getCustomUidString()}")
        return buildUidResponse()
    }

    /**
     * Build a response containing the custom UID
     */
    private fun buildUidResponse(): ByteArray {
        try {
            // Simple response: UID + success status
            val response = ByteArray(customUid.size + SELECT_OK_SW.size)

            // Copy UID
            customUid.copyInto(response, 0)

            // Append success status
            SELECT_OK_SW.copyInto(response, customUid.size)

            Log.d(TAG, "Built UID response: ${byteArrayToHexString(response)}")
            return response

        } catch (e: Exception) {
            Log.e(TAG, "Error building UID response", e)
            return UNKNOWN_CMD_SW
        }
    }
}
