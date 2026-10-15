# SynD BLE Protocol v0.1 (draft)
Transport: Bluetooth LE GATT, central/peripheral role negotiation; iOS and Android must advertise the same service UUID.
Service UUID: 7c31a4e0-8d62-4f1d-a9b3-104c8a63d001
RX characteristic (write with response): 7c31a4e0-8d62-4f1d-a9b3-104c8a63d002
TX characteristic (notify): 7c31a4e0-8d62-4f1d-a9b3-104c8a63d003

## Wire format
Version 1 frame: version:u8 | kind:u8 | message_id:16 bytes | sequence:u16 big-endian | total:u16 big-endian | payload_length:u16 big-endian | payload:bytes.
Kinds: 1 HELLO, 2 TEXT, 3 VOICE_CHUNK, 4 ACK, 5 PTT_START, 6 PTT_END, 7 CALL_OFFER, 8 CALL_ACCEPT, 9 CALL_END.
Fragment payload to negotiated ATT MTU minus 3-byte ATT overhead and 24-byte frame header. Reassemble by peer/message ID with bounds, timeouts and deduplication.
This is a transport specification, NOT a security protocol. Before exchanging user messages, implement authenticated ephemeral key exchange, peer identity verification, AEAD encryption, replay protection and key rotation.
Never transmit unencrypted voice or text in production.
Calls over BLE are experimental, not guaranteed. Store-and-forward relaying requires explicit TTL/hop count, loop prevention, and consent.
