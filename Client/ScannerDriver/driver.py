from abc import ABC, abstractmethod
from typing import Final
from numpy.core.fromnumeric import reshape
import serial
from itertools import chain
import pprint
import fingerprint_enhancer
import cv2
import numpy
import os

class Packet (ABC):
    HEADER: Final = [0xEF, 0x01]
    ADDRESS: Final = [0xFF, 0xFF, 0xFF, 0xFF]
    pkgID = None
    pkgLen = None
    checksum = None

class CommandPacket(Packet):
    instruction_code = None
    pkgID = [0x01]

    @abstractmethod
    def build_packet(self):
        return list(chain(
            self.HEADER,
            self.ADDRESS,
            self.pkgID,
            self.pkgLen,
            self.instruction_code,
            self.checksum
        ))

    @abstractmethod
    def confirm(self, ser):
        pass

    def send(self, ser):
        pkt = self.build_packet()
        pkt_bytes = serial.to_bytes(pkt)

        ser.write(pkt_bytes)
        print ("Sent instruction")
        #print("Waiting for confirmation...")
        return self.confirm(ser)
    
class ConfirmationPacket (Packet):
    confirmation_code = None
    pkgID = [0x07]

    @abstractmethod
    def listen_for_confirmation(self, ser):
        # read first 9 bytes up to the end of pkg_len
        head = bytearray(ser.read(9))

        length = head[-2:]
        #pkg length is 2 bytes long
            #self.pkgLen = len[0] + len[1]
        self.pkgLen = sum(length)

        # read the rest of the packet now that we know its length
        response = ser.read(int(self.pkgLen))

        self.confirmation_code = response[0]
        self.checksum = response[-2:]

        return self

class DataPacket(Packet):
    pkg_contents = []

    def read(self, ser):
        head = bytearray(ser.read(9))

        self.pkgID = head[6]

        length = head[-2:]
        #pkg length is 2 bytes long
        self.pkgLen = sum(length)

        response = ser.read(int(self.pkgLen))

        self.pkg_contents = response[:-2]
        self.checksum = response[-2:]

        return self


class Handshake (CommandPacket):
    def __init__(self):
        #self.pkgID = [0x01]
        self.pkgLen = [0x00, 0x04]
        self.instruction_code = [0x17]
        self.control_code = [0x00]
        self.checksum = [0x00, 0x1C]
        
    def build_packet(self):
        return list(chain(
            self.HEADER,
            self.ADDRESS,
            self.pkgID,
            self.pkgLen,
            self.instruction_code,
            self.control_code,
            self.checksum
        ))
    def confirm(self, ser):
        return HandshakeConf().listen_for_confirmation(ser)

class HandshakeConf(ConfirmationPacket):
    def __init__(self):
        super().__init__()

    def listen_for_confirmation(self, ser):
        response = ser.read(12)
        #strip the header and address as we don't need it
        response = bytearray(response[6:])

        self.pkgID = [response.pop(0)]
        self.pkgLen = [response.pop(0), response.pop(0)]
        self.confirmation_code = [response.pop(0)]
        self.checksum = [response.pop(0), response.pop(0)]

       # print(pkt.confirmation_code[0])

        #ensure all bytes have been read
        if len(response) != 0:
            raise EnvironmentError("Handshake Failed: Invalid response")
        
        return self

    def is_successful(self):
        if self.confirmation_code[0] == 0x00:
            return True
            #print("Handshake Successful")
        elif self.confirmation_code[0] == 0x01:
            return False
            # print("Handshake Failed: Error when receiving package")
        elif self.confirmation_code[0] == 0x1d:
            return False
            #print("Handshake Failed: Failed to operate the communication port")
        else:
            return False

class ReadSysPara(CommandPacket):
    def __init__(self) -> None:
        #self.pkgID = [0x01]
        self.pkgLen = [0x00, 0x03]
        self.instruction_code = [0x0f]
        self.checksum = [0x00, 0x13]
    
    def build_packet(self):
        return super().build_packet()

    def confirm(self, ser):
        return ReadSysParaConf().listen_for_confirmation(ser)


class ReadSysParaConf(ConfirmationPacket):
    def __init__(self) -> None:
        self.param_list = bytes()

    def to_string(self):
        return pprint.pformat(self.unpack_params())

    def unpack_params(self):
        if len(self.param_list) != 16:
            raise EnvironmentError("Call to unpack param list with invalid list length")
        
        def status_register_to_bin(reg):
            # first byte
            status_register_1 = bin(int(reg[0], 16))[2:].zfill(8)
            # second byte
            status_register_2 = bin(int(reg[1], 16))[2:].zfill(8)
            return status_register_1 + status_register_2

        return {
            # status register bits 4-15 are reserved and do nothing
            "status_register": status_register_to_bin([hex(self.param_list[0]), hex(self.param_list[1])]),
            "system_id_code": [self.param_list[2], self.param_list[3]],
            "fingerprint_lib_size": int(self.param_list[4] + self.param_list[5]),
            "security_level": int(self.param_list[6] + self.param_list[7]),
            "device_address": [hex(self.param_list[8]), hex(self.param_list[9]), hex(self.param_list[10]), hex(self.param_list[11])],
            "data_packet_size_code": int(self.param_list[12] + self.param_list[13]),
            "baud_rate": int(self.param_list[14] + self.param_list[15]) * 9600 #baud = N*9600
        }
    def listen_for_confirmation(self, ser):
        # read first 9 bytes up to the end of pkg_len
        head = bytearray(ser.read(9))

        len = head[-2:]
        #pkg length is 2 bytes long
        self.pkgLen = len[0] + len[1]

        # read the rest of the packet now that we know its length
        response = ser.read(int(self.pkgLen))

        self.confirmation_code = [response[0]]
        self.param_list = response[1:-2]
        self.checksum = [response[-2], response[-1]]

        return self
    
class GenImg(CommandPacket):
    def __init__(self) -> None:
        #self.pkgID = [0x01]
        self.pkgLen = [0x00, 0x03]
        self.instruction_code = [0x01]
        self.checksum = [0x00, 0x05]
    def build_packet(self):
        return super().build_packet()

    def confirm(self, ser):
        return GenImgConf().listen_for_confirmation(ser)

class GenImgConf(ConfirmationPacket):
    def __init__(self):
        super().__init__()
    def listen_for_confirmation(self, ser):
        return super().listen_for_confirmation(ser)
    def get_result(self):
        if self.confirmation_code == 0x00:
            return "Fingerprint Sucessfully Taken"
        elif self.confirmation_code == 0x01:
            return "Error when receiving package"
        elif self.confirmation_code == 0x02:
            return "Finger not detected"
        elif self.confirmation_code == 0x03:
            return "Failed to collect fingerprint"

class UpImage(CommandPacket):
    def __init__(self):
        self.pkgLen = [0x00, 0x03]
        self.instruction_code = [0x0A]
        self.checksum = [0x00, 0x0E]
    def build_packet(self):
        return super().build_packet()
    def confirm(self, ser):
        return UpImageConf().listen_for_confirmation(ser)

class UpImageConf(ConfirmationPacket):
    def __init__(self) -> None:
        super().__init__()
    def listen_for_confirmation(self, ser):
        return super().listen_for_confirmation(ser)
    def get_result(self):
        if self.confirmation_code == 0x00:
            return "Ready to transfer image"
        elif self.confirmation_code == 0x01:
            return "Error when receiving package"
        elif self.confirmation_code == 0x0f:
            return "Failed to transfer the data packet"

class Img2Tz(CommandPacket):
    def __init__(self, buffer_id) -> None:
        self.pkgLen = [0x00, 0x04]
        self.instruction_code = [0x02]
        
        if buffer_id == 0x01:
            self.checksum = [0x00, 0x08] # 01 + 00 + 04 + 02 + 01
        elif buffer_id == 0x02:
            self.checksum = [0x00, 0x09]
        else:
            raise ValueError("Invalid buffer")
        
        self.buffer = [buffer_id]
    def confirm(self, ser):
        return Img2TzConf().listen_for_confirmation(ser)
    def build_packet(self):
        return list(chain(
            self.HEADER,
            self.ADDRESS,
            self.pkgID,
            self.pkgLen,
            self.instruction_code,
            self.buffer,
            self.checksum
        ))

class Img2TzConf(ConfirmationPacket):
    def __init__(self) -> None:
        super().__init__()
    def listen_for_confirmation(self, ser):
        return super().listen_for_confirmation(ser)
    def get_result(self):
        if self.confirmation_code == 0x00:
            return "Successfully generated character file"
        elif self.confirmation_code == 0x01:
            return "Error when receiving package"
        elif self.confirmation_code == 0x06:
            return "Failed to generate character file due to overly distorted image"
        elif self.confirmation_code == 0x07:
            return "Failed to generate character file due to lacking characteristic points or image too small"
        elif self.confirmation_code == 0x15:
            return "Failed to generate the image due to lacking primary image"

class UpChar(CommandPacket):
    def __init__(self, buffer_id) -> None:
        self.pkgLen = [0x00, 0x04]
        self.instruction_code = [0x08]

        if buffer_id == 0x01:
            self.checksum = [0x00, 0xE]
        elif buffer_id == 0x02:
            self.checksum = [0x00, 0x0F]
        else:
            raise ValueError("Invalid buffer id")
        
        self.buffer = [buffer_id]

    def confirm(self, ser):
        return UpCharConf().listen_for_confirmation(ser)
    def build_packet(self):
        return list(chain(
            self.HEADER,
            self.ADDRESS,
            self.pkgID,
            self.pkgLen,
            self.instruction_code,
            self.buffer,
            self.checksum
        ))
class UpCharConf(ConfirmationPacket):
    def __init__(self) -> None:
        super().__init__()
    def listen_for_confirmation(self, ser):
        return super().listen_for_confirmation(ser)
    def get_result(self):
        if self.confirmation_code == 0x00:
            return "Ready to transfer the data packet"
        elif self.confirmation_code == 0x01:
            return "Error when receiving the package"
        elif self.confirmation_code == 0x0D:
            return "Error when uploading template"

def download_character_file(ser):
    data_packets = []

    curr_pkt = DataPacket()
    # pkgID 0x08 means its the end of the data stream
    while (curr_pkt.pkgID != 0x08):
        curr_pkt = DataPacket().read(ser)
        data_packets.append(curr_pkt)

    # combine the contents of each data packet
    raw_char_data = bytearray(b'')
    for pkt in data_packets:
        raw_char_data.extend(pkt.pkg_contents)
    
    return raw_char_data

def get_img_buf_status(ser):
    # read all the system paramaters
    sys_params = ReadSysPara().send(ser).unpack_params()
    status_register = sys_params["status_register"]
    return status_register
    
def download_fingerprint(ser, gui=None):
    # fingerprint data is sent in a series of data packets each 139 bytes long
    # the after all packets are sent 40032 bytes should be have transferred
    # meaning there are 288 packets to read
    data_packets = []
    pkt_count = 0
    
    # progress is the number of packets sent mapped to between 0-100
    progress = 0
    while pkt_count < 288:
        progress = __interpolate(pkt_count, 0, 288, 0, 1)

        if gui:
            gui.set_progress_bar(progress)
        else:
            print(str(progress) + '%', end='\r')

        pkt = DataPacket().read(ser)
        data_packets.append(pkt)
        pkt_count += 1

    # combine the contents of each data packet
    raw_fingerprint_data = bytearray(b'')
    for pkt in data_packets:
        raw_fingerprint_data.extend(pkt.pkg_contents)
    
    return raw_fingerprint_data

import time
from PIL import Image

def get_fingerprint_image(ser, gui=None, enhance=False):
    gui.set_prompt_label_text("Place finger on the sensor")
    genimg = GenImg().send(ser)

    # Block while there is not finger on the sensor
    while genimg.confirmation_code != 0x00:
        #sleep so we dont spam the sensor
        time.sleep(0.5)
        genimg = GenImg().send(ser)
  
    upimg = UpImage().send(ser)
    print(upimg.get_result())

    gui.set_prompt_label_text("Downloading...")
    raw_fingerprint_data = download_fingerprint(ser, gui)
    
    # using UART, each pixel is 4 bits long, with 2 adjacent pixels on the same row forming a single byte
    # we need to pad each pixel to be 8 bits long otherwise we will not have sufficient data to create 
    # the 256x288 image
    fingerprint_data = bytearray(__convert_4bit_to_8bit(raw_fingerprint_data))
    fingerprint_pil = Image.frombytes('L', (256, 288), bytes(fingerprint_data), 'raw')

    ###### CHANGE THIS ######
    # I could not for the life of me convert the image from memory to a cv2 image, 
    # after trying for 2 days I have simply decided to just write it to disk
    # This severely impacts IO performance but its currently the only working solution
    #########################
    if enhance:
        gui.set_prompt_label_text("Enhancing...")

        # Convert Image to CV2 format
        fingerprint_pil.save('temp.bmp')
        fingerprint_cv = cv2.imread('temp.bmp', 0)
        os.remove('temp.bmp')

        # Enhance Fingerprint
        enhanced = fingerprint_enhancer.enhance_Fingerprint(fingerprint_cv)
        enhanced = cv2.cvtColor(enhanced, cv2.COLOR_BGR2RGB)

        return Image.fromarray(enhanced)
    else:
        return fingerprint_pil

# Scale numbers to a new range 
def __interpolate(value, left_min, left_max, right_min, right_max):
    # Figure out how 'wide' each range is
    leftSpan = left_max - left_min
    rightSpan = right_max - right_min

    # Convert the left range into a 0-1 range (float)
    valueScaled = float(value - left_min) / float(leftSpan)

    # Convert the 0-1 range into a value in the right range.
    return right_min + (valueScaled * rightSpan)

def __convert_4bit_to_8bit(_4bit_img):
    _8bit_img = []
    for my_byte in _4bit_img:
        two_pixel_byte = str(f'{my_byte:0>8b}')
        first_pixel = '0000' + two_pixel_byte[0:4] 
        second_pixel = '0000' + two_pixel_byte[4:] 

        # Interpolate the pixels to be 8 bit integers rather than 4 bit        
        first_pixel = __interpolate(int(first_pixel, 2), 0, 16, 0, 255)
        second_pixel = __interpolate(int(second_pixel, 2), 0, 16, 0, 255)

        _8bit_img.append(int(first_pixel))
        _8bit_img.append(int(second_pixel))
    return _8bit_img