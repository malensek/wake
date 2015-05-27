import shutil
import wake.util as util

def name():
    return "copy"

def wants(filename):
    # The copy module is a catch-all:
    return True

def produces(filename):
    return [util.src2out(filename)]

def process(filename):
    outfile = produces(filename)[0]
    util.check_dir(outfile)
    shutil.copy(filename, outfile)
    print("cp " + filename + " -> " + outfile)
