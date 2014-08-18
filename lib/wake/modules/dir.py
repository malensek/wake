import os
import shutil

import wake.settings as settings
import wake.util as util

def name():
    return "dir"

def wants(filename, ext):
    fullpath = filename + ext
    return os.path.isdir(fullpath)

def produces(filename, ext):
    return [filename.replace(settings.sourcedir, settings.outputdir) + ext]

def process(args):
    pass
