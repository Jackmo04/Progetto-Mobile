package com.example.cacciaaltesoro.utils.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import java.nio.ByteBuffer
import java.util.UUID

class NfcUtils {
    companion object {
        private const val MIME_TYPE = "application/com.example.cacciaaltesoro.uuid"
    }

    private fun uuidToBytes(uuid: UUID): ByteArray {
        val buffer = ByteBuffer.wrap(ByteArray(16))
        buffer.putLong(uuid.mostSignificantBits)
        buffer.putLong(uuid.leastSignificantBits)
        return buffer.array()
    }

    private fun bytesToUuid(bytes: ByteArray): UUID {
        val buffer = ByteBuffer.wrap(bytes)
        return UUID(buffer.long, buffer.long)
    }

    fun writeUuidToNdef(nfcTag: Tag, uuid: UUID): Boolean {
        val ndef = Ndef.get(nfcTag) ?: return false

        return try {
            ndef.connect()
            if (!ndef.isWritable) return false

            val payload = uuidToBytes(uuid)
            val record = NdefRecord.createMime(
                MIME_TYPE,
                payload
            )
            val message = NdefMessage(record)

            ndef.writeNdefMessage(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try { ndef.close() } catch (e: Exception) { /* Ignore */ }
        }
    }

    fun readUuidFromNdef(nfcTag: Tag): UUID? {
        val ndef = Ndef.get(nfcTag) ?: return null

        return try {
            ndef.connect()
            val message = ndef.ndefMessage ?: return null
            val record = message.records.firstOrNull() ?: return null

            val recordType = String(record.type)

            if (recordType == MIME_TYPE && record.payload.size == 16) {
                bytesToUuid(record.payload)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try { ndef.close() } catch (e: Exception) { /* Ignore */ }
        }
    }
}