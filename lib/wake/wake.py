from collections import defaultdict
import importlib
import multiprocessing
import os
import shutil
import sys

import wake.settings as settings
import wake.util as util

def load_modules():
    """Load wake modules.  Priority is determined by the PYTHONPATH.

    Returns:
        dict of loaded modules
    """
    if not util.has_setting("modules"):
        return []

    modules = []
    for modname in settings.modules:
        module = load_module(modname)
        print("Loaded module: " + modname)
        modules.append(module)

    return modules

def load_module(modname):
    """Import a module under the wake.modules package.

    Args:
        modname: name of the module to load
    Returns:
        loaded module instance
    """
    module = importlib.import_module("wake.modules." + modname)
    try:
        if module.is_module():
            name = module.name()
            return module
    except AttributeError:
        print("Invalid module: " + modname, file=sys.stderr)

def map_modules(modules, sourcedir):
    """Map source files to the modules that will process them.

    Args:
        sourcedir (str): directory containing source files
        modules (list): loaded modules
    Returns:
        tuple with module map (module name -> source file) and
        reverse map (source file -> module name) dicts.
    """
    source = util.scan_dir(sourcedir)
    modmap = defaultdict(list)
    reversemap = {}
    for sfile in source:
        for module in modules:
            name, ext = os.path.splitext(sfile)
            if module.wants(name, ext):
                modname = module.name()
                modmap[modname].append(sfile)
                reversemap[sfile] = module
                break
    return modmap, reversemap

def map_outputs(modules, modmap):
    """Map source files to their output file(s).

    Args:
        modules (list): loaded modules
        modmap (dict): module -> source file mappings
    Returns:
        dict mapping source files to output files
    """
    outmap = {}
    for module in modules:
        for sfile in modmap[module.name()]:
            name, ext = os.path.splitext(sfile)
            outmap[sfile] = module.produces(name, ext)
    return outmap

def check_modifications(outmap):
    """Find new or modified files.

    Args:
        outmap: source -> output mappings
    Returns:
        list of modified or new files
    """
    modified = []
    for sfile in outmap:
        smod = os.path.getctime(sfile)
        for outfile in outmap[sfile]:
            if not os.path.isfile(outfile):
                modified.append(sfile)
                continue
            omod = os.path.getctime(outfile)
            if smod > omod:
                modified.append(sfile)
    return modified

def tidy(outmap):
    """Clean up 'orphaned' files in the output directory (files that are no
    longer owned/produced by an active module).

    Args:
        outmap (dict): source -> output mappings.  Files in the output directory
                that are not in this dict are removed.
    """
    expected = []
    for sfile in outmap:
        for f in outmap[sfile]:
            expected.append(f)
    outdir = util.check_setting("outputdir", "./output")
    actual = util.scan_dir(outdir)
    orphans = set(actual).difference(set(expected))
    for orphan in orphans:
        try:
            util.remove(orphan)
        # Files may get removed from a 'rm -r' on a directory
        except FileNotFoundError: pass

def check_perms(outmap):
    """Verify output files' permissions match settings.{filemode,dirmode}."""
    for sfile in outmap:
        for f in outmap[sfile]:
            util.set_perms(f)

def build(num_threads=1):
    """Build a wake site.

        num_threads: Number of worker threads to use to build the site
    """

    modules = load_modules()
    if not modules:
        print("No modules were loaded!  Exiting.", file=sys.stderr)
        exit(1)

    print("Thread pool size: " + str(num_threads))

    sourcedir = util.check_setting("sourcedir", "./source")
    modmap, reversemap = map_modules(modules, sourcedir)
    outmap = map_outputs(modules, modmap)
    modified = check_modifications(outmap)

    # Start processing
    pool = multiprocessing.Pool(processes=num_threads)
    workers = []
    for sfile in modified:
        worker = pool.map_async(reversemap[sfile].process, (sfile,))
        workers.append(worker)

    for worker in workers:
        worker.wait()
        # Report errors:
        worker.get()

    print("Removing orphaned files...")
    tidy(outmap)

    print("Verifying permissions...")
    check_perms(outmap)

    print("Build complete.")

def upload():
    command = util.check_setting("upload_command", "")
    if command == "":
        print("Please configure upload_command.")
        return

    from subprocess import call
    call(command)

