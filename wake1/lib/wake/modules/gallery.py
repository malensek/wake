from jinja2 import Environment, FileSystemLoader
import os
from PIL import Image
import shutil

import wake.settings as settings
import wake.util as util

def name():
    return "gallery"

def wants(filename):
    if filename.endswith('gallery.json'):
        return True

    filedir = os.path.dirname(os.path.realpath(filename))
    if os.path.isfile(filedir + "/gallery.json"):
        try:
            i = Image.open(filename)
            # We successfully loaded the file as an image:
            i.close()
            return True
        except OSError:
            print("gallery: '%s' is not an image or cannot be loaded."
                    % (filename))
    return False

def produces(filename):
    if util.ext_of(filename) == '.json':
        return [util.src2out(os.path.dirname(filename)) + "/index.html"]
    else:
        outfile = util.src2out(filename)
        return [outfile, outfile + ".thumb"]

def process(filename):
    if util.ext_of(filename) == '.json':
        process_index(filename)
    else:
        process_image(filename)

def process_index(filename):
    outfile = produces(filename)[0]
    print("gallery.index: " + outfile)

    images = []
    dirname = os.path.dirname(filename)
    for i in os.listdir(dirname):
        if wants(dirname + os.sep + i) and i != "gallery.json":
            images.append(i)

    jinja = Environment(loader=FileSystemLoader(settings.templatedir))
    jinja.globals["settings"] = settings
    template = jinja.get_template("gallery.html")

    html = template.render(images=images, title="My great gallery")

    fout = open(outfile, 'w')
    fout.write(html)
    fout.close()

def process_image(filename):
    out = produces(filename)
    image = out[0]
    thumb = out[1]
    util.check_dir(image)

    print("gallery.thumb: " + filename + " -> " + thumb)
    i = Image.open(filename)
    t = i.copy()
    t.thumbnail((300, 300), resample=Image.ANTIALIAS)
    t.save(thumb, i.format)
    t.close()

    print("gallery.image: " + filename + " -> " + image)
    i = Image.open(filename)
    i.thumbnail((1024, 1024))
    i.save(image)
    i.close()
