"""Platform-neutral reference encoder/decoder for SynD BLE frames."""
from dataclasses import dataclass
from enum import IntEnum
from uuid import UUID
import struct

VERSION = 1
HEADER = struct.Struct("!BB16sHHH")
MAX_PARTS = 1024
MAX_PAYLOAD = 65535

class Kind(IntEnum):
    HELLO = 1
    TEXT = 2
    VOICE_CHUNK = 3
    ACK = 4
    PTT_START = 5
    PTT_END = 6
    CALL_OFFER = 7
    CALL_ACCEPT = 8
    CALL_END = 9

@dataclass(frozen=True)
class Frame:
    kind: Kind
    message_id: UUID
    sequence: int
    total: int
    payload: bytes

def encode(frame: Frame) -> bytes:
    if not (1 <= frame.total <= MAX_PARTS and 0 <= frame.sequence < frame.total):
        raise ValueError("invalid fragment numbering")
    if len(frame.payload) > MAX_PAYLOAD:
        raise ValueError("payload too large")
    return HEADER.pack(VERSION, int(frame.kind), frame.message_id.bytes,
                       frame.sequence, frame.total, len(frame.payload)) + frame.payload

def decode(data: bytes) -> Frame:
    if len(data) < HEADER.size:
        raise ValueError("truncated header")
    version, kind, mid, seq, total, length = HEADER.unpack_from(data)
    if version != VERSION or len(data) != HEADER.size + length:
        raise ValueError("invalid version or length")
    if not (1 <= total <= MAX_PARTS and seq < total):
        raise ValueError("invalid fragment numbering")
    return Frame(Kind(kind), UUID(bytes=mid), seq, total, data[HEADER.size:])
