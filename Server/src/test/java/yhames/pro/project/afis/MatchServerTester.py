# Used to simulate a python client connecting to the server

import socket
import sys

def test():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 
    s.connect(('127.0.0.1', 6969))

    # Send just enough data to simulate a sourceafis match request
    img_path = '/Users/james/ownCloud/University/Masters/Project/Code/Stable/Server/resources/fingerprints/LeftIndex.bmp'

    img = None
    with open (img_path, 'rb') as imgfile:
        img = imgfile.read()

    img_len = len(img).to_bytes(4, 'big' )

    data = [0x00]
    data.extend(bytearray(img_len))
    data.extend(bytearray(img))

    s.send(bytes(data))

    if bytearray(s.recv(1))[0] != 0x00:
        print("NOT 0 FAM")
        exit(1)

if len(sys.argv) < 2:
    repetitions = 1
else:
    repetitions = int(sys.argv[1])
for x in range(repetitions):
    test()