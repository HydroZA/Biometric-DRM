from match_request import ComparisonMethod
import gi
gi.require_version("Gtk", "3.0")
gi.require_version('GdkPixbuf', '2.0')
from gi.repository import GLib, GdkPixbuf, GObject
from PIL import Image
gi.require_version("Gtk", "3.0")
from gi.repository import Gtk

class Handler:
    def onDestroy(self, *args):
        Gtk.main_quit()

class UI:
    def __init__(self) -> None:
        self.builder = Gtk.Builder()
        self.builder.add_from_file("UI/ui.glade")
        self.builder.connect_signals(Handler())
        self.window = self.builder.get_object("window")
        self.fixed = self.builder.get_object("fixed1")
        self.btn_capture = self.builder.get_object("btnCapture")
        self.btn_save = self.builder.get_object("btnSave")
        self.btn_match = self.builder.get_object("btnMatch")
        self.fingerprint_preview = self.builder.get_object("fingerprint_preview")
        self.match_preview = self.builder.get_object("matched_preview")
        self.progress_bar = self.builder.get_object("progress_bar")
        self.label_prompt = self.builder.get_object("lblPrompt")
        self.label_matched = self.builder.get_object("lblMatchedImage")
        self.rbtn_comparison = self.builder.get_object("comparison_method")
        self.rbtn_minutiae = self.builder.get_object("minutiae_method")
    def get_match_method(self) -> ComparisonMethod:
        if self.rbtn_comparison.get_active():
            return ComparisonMethod.SSIM
        else:
            return ComparisonMethod.SOURCE_AFIS
    def show(self):
        self.window.show_all()
    def hide(self):
        self.window.hide()
    def set_prompt_label_text(self, new_text):
        self.label_prompt.set_text(new_text)
        #update the ui w.r.t the new changes
        while Gtk.events_pending():
            Gtk.main_iteration()
    def set_matched_img_label_text(self, new_text):
        self.label_matched.set_text(new_text)
        #update the ui w.r.t the new changes
        while Gtk.events_pending():
            Gtk.main_iteration()
    def set_window_title(self, new_text):
        self.window.set_title(new_text)
    def set_progress_bar(self, progress):
        # progress should be a float between 0 and 1
        if progress < 0 or progress > 1:
            raise ValueError("Call to update progress bar with invalid value")
        self.progress_bar.set_fraction(progress) 
        #update the ui w.r.t the new changes
        while Gtk.events_pending():
            Gtk.main_iteration()
    def set_preview_image(self, img: Image):
        img_pixbuf = image2pixbuf(img)
        self.fingerprint_preview.set_from_pixbuf(img_pixbuf)
    def get_preview_image(self) -> Image:
        pixbuf = self.fingerprint_preview.get_pixbuf()
        return pixbuf2image(pixbuf)
    def disable_buttons(self):
        self.btn_capture.set_sensitive(False)
        self.btn_save.set_sensitive(False)
        self.btn_match.set_sensitive(False)
    def enable_buttons(self):
        self.btn_save.set_sensitive(True)
        self.btn_capture.set_sensitive(True)
        self.btn_match.set_sensitive(True)
    def set_matched_image(self, img: Image):
        img_pix = image2pixbuf(img)
        self.match_preview.set_from_pixbuf(img_pix)

def image2pixbuf(im):
    """Convert Pillow image to GdkPixbuf"""
    #gdkpixbuf only supports RGB, lets convert it 
    rgb_im = im.convert("RGB")

    data = rgb_im.tobytes()
    w, h = 256, 288
    data = GLib.Bytes.new(data)
    pix = GdkPixbuf.Pixbuf.new_from_bytes(data, GdkPixbuf.Colorspace.RGB,
            False, 8, w, h, w * 3)
    return pix
def pixbuf2image(pix):
    """Convert gdkpixbuf to PIL image"""
    data = pix.get_pixels()
    w = pix.props.width
    h = pix.props.height
    stride = pix.props.rowstride
    mode = "RGB"
    if pix.props.has_alpha == True:
        mode = "RGBA"
    im = Image.frombytes(mode, (w, h), data, "raw", mode, stride)
    return im

# Return selected path + filename
def show_save_dialog(text, parent=None) -> str:
    dialog = Gtk.FileChooserDialog(
        text, 
        parent,
        Gtk.FileChooserAction.SAVE
    )
    dialog.add_buttons(
        Gtk.STOCK_CANCEL,
        Gtk.ResponseType.CANCEL,
        Gtk.STOCK_SAVE,
        Gtk.ResponseType.OK,
    )
    dialog.set_current_name("UntitledFingerprint.bmp")
    response = dialog.run()
    if response == Gtk.ResponseType.OK:
        filename = dialog.get_filename()
        dialog.destroy()
        return filename
    elif response == Gtk.ResponseType.CANCEL:
        dialog.destroy()
        return None