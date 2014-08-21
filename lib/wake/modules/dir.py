import os
import wake.util as util

def name():
    return "dir"

def wants(filename):
    return os.path.isdir(filename)

def produces(filename):
    return [util.src2out(filename)]

def process(args):
    pass
