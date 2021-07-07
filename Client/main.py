### TODO:
### - Match button should be hidden until a fingerprint is captured
### - Fix prompt messages when capturing 
### - Plan DRM System

import ScannerDriver.driver as driver
import UI.ui as ui
import platform 
import serial
import gi
from gi.repository import GLib, GdkPixbuf, Gtk
from PIL import Image
import os
import numpy
import cv2
gi.require_version("Gtk", "3.0")

if platform.system() == 'Linux':
    port = '/dev/ttyUSB0'
elif platform.system() == 'Windows':
    port = 'COM2'
elif platform.system() == 'Darwin':
    port = '/dev/tty.usbserial-1420'

ser = serial.Serial(
    port=port,
    baudrate=57600,
    parity=serial.PARITY_NONE,
    stopbits=serial.STOPBITS_ONE,
    bytesize=serial.EIGHTBITS
)

def on_btn_capture_clicked(btn, ser, gui: ui.UI):
    # Disable the buttons while working
    gui.disable_buttons()

    # capture the fingerprint 
    fingerprint = driver.get_fingerprint_image(ser, gui, enhance=False)

    # set the preview image to be the captured fingerprint 
    gui.set_preview_image(fingerprint)

    # enable the buttons
    gui.enable_buttons()
    gui.set_prompt_label_text("Ready")

def on_btn_save_clicked(btn, gui):
    img = gui.get_preview_image()

    # prompt the user for a file name
    filename = ui.show_save_dialog("Save As")

    if filename != None:
        img.save(filename)
    gui.set_prompt_label_text("Ready")

def on_btn_match_clicked(btn, gui: ui.UI):
    pass

def get_images_in_dir(path: str):
    if not path.endswith('/'):
        path = path + '/'

    files = filter(lambda f: f.endswith('.bmp'), os.listdir(path))
    # Prepend the relative path to all file names
    return [f'{path}{i}' for i in files]

def pil_to_cv2(pil_img: Image):
    pil_img.save('temp.bmp')
    fingerprint_cv = cv2.imread('temp.bmp', 0)
    os.remove('temp.bmp')
    return fingerprint_cv
gui = ui.UI()

gui.set_window_title("Biometric DRM")

# tell the UI that we want our methods to be the callback for the buttons
gui.btn_capture.connect("clicked", on_btn_capture_clicked, ser, gui)
gui.btn_save.connect("clicked", on_btn_save_clicked, gui)
gui.btn_match.connect("clicked", on_btn_match_clicked, gui)

# set the buttons to be greyed out until successful handshake
gui.disable_buttons()

# Connect to serial port
gui.set_prompt_label_text("Connecting...")
while not ser.is_open:
    pass
gui.set_prompt_label_text("Connected!")

# Handshake with scanner
print("Performing Handshake...")
handshake = driver.Handshake().send(ser)
if handshake.is_successful():
    print("Handshake Successful!")

    # Enable the buttons
    gui.enable_buttons()

    # Read system parameters for debugging
    print("Reading System Parameters...")
    sys_params = driver.ReadSysPara().send(ser)
    print("System Parameters:\n" + sys_params.to_string())

    gui.set_prompt_label_text("Ready")

    gui.show()
    Gtk.main()

else:
    gui.set_prompt_label_text("Handshake Failed")