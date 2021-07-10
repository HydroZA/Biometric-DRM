import socket 
from PIL import Image
import io

class MatchResponse():
    def read(self, s: socket):
        match = s.recv(1)
        if match == b'\x00':
            print("MatchRequest Succesful")
            self.is_match = True
            length = int.from_bytes(s.recv(4), 'big')
            img_bytes = s.recv(length)
            self.img = Image.frombytes('RGB', (256, 288), img_bytes, 'raw')
        else:
            self.is_match = False
            print("MatchRequest Failed")
        return self