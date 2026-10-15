import unittest
from uuid import uuid4
from synd_protocol import Frame, Kind, encode, decode

class FrameTests(unittest.TestCase):
    def test_round_trip(self):
        frame = Frame(Kind.TEXT, uuid4(), 0, 1, b"hello")
        self.assertEqual(decode(encode(frame)), frame)
    def test_truncation(self):
        with self.assertRaises(ValueError):
            decode(b"bad")
    def test_invalid_fragment(self):
        with self.assertRaises(ValueError):
            encode(Frame(Kind.TEXT, uuid4(), 1, 1, b"x"))

if __name__ == "__main__":
    unittest.main()
