import os
import shutil

import wake.settings as settings
import wake.util as util

def is_module():
    return True

def name():
    return "copy"

def wants(filename, ext):
    # The copy module is a catch-all:
    return True

def produces(filename, ext):
    return [filename.replace(settings.sourcedir, settings.outputdir) + ext]

def process(args):
    filename, ext = os.path.splitext(args)
    infile = args
    outfile = produces(filename, ext)[0]
    util.check_dir(outfile)
    shutil.copy(infile, outfile)
    print("cp " + infile + " -> " + outfile)
