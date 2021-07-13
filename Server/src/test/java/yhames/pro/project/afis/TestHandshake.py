import socket
import sys

def test():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect(('127.0.0.1', 6969))

    # 0x00 is a handshake request
    s.send(b'\x00')

    if bytearray(s.recv(1))[0] != 0x00:
        exit(1)

if len(sys.argv) < 2:
    repetitions = 1
else:
    repetitions = int(sys.argv[1])
for x in range(repetitions):
    test()