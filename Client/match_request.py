# Holds data needed to request server to perform a match on our behalf

from enum import Enum
import socket

class ComparisonMethod(Enum):
    SOURCE_AFIS = 0x00
    SSIM = 0x01
    MSE = 0x02

class MatchRequest():
    # Fingerprint needs to be a bmp format bytearray
    def __init__(self, method: ComparisonMethod, fingerprint: bytearray) -> None:
        self.method = method
        self.fingerprint = fingerprint
    def serialize(self) -> bytearray:
        #0x01 is used by the server to identify match requests
        request = bytearray([0x01])

        request.extend(self.method.value)
        request.extend(len(self.fingerprint).to_bytes(4, 'big'))
        request.extend(self.fingerprint)

        return request
    def send(self, s: socket):
        mr = self.serialize()
        s.send(bytes(mr))