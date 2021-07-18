import socket
import sys

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(('127.0.0.1', 6969))

# Send just enough data to simulate a sourceafis match request
img_path = '/Users/james/ownCloud/University/Masters/Project/Code/Server/resources/fingerprints/LeftIndex.bmp'

with open(img_path, 'rb') as imgfile:
    img = imgfile.read()


def test():
    softwareName = str.encode("The Witcher 3: Wild Hunt", 'UTF-8')
    softwareNameLen = len(softwareName).to_bytes(4, 'big')

    img_len = len(img).to_bytes(4, 'big')

    data = [0x00]
    data.extend(bytearray(img_len))
    data.extend(bytearray(img))

    # 0x05 is a AuthenticationRequest
    s.send(b'\x05')
    s.send(softwareNameLen)
    s.send(softwareName)
    s.send(bytes(data))

    response = bytearray(s.recv(5))
    if response[0] == 0x00:
        key_len = int.from_bytes(bytes(response[1:5]), 'big')
        key = bytearray(s.recv(key_len))
        print(key)
    else:
        quit(1)


test()
#if len(sys.argv) < 2:
#    repetitions = 0
#else:
#    repetitions = int(sys.argv[1])
#for x in range(repetitions):
#    test()
