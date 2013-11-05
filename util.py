import os
import settings
import datetime as dt

def check_dir(filename):
    dirname = os.path.dirname(filename)

    if dirname is '':
        return

    try:
        if not os.path.exists(dirname):
            os.makedirs(dirname)
            print("mkdir ", dirname)
    except FileExistsError:
        # It's possible that two threads created the directory at the same time.
        pass

def set_perms(filename):
    mode = settings.filemode
    if os.path.isdir(filename):
        mode = settings.dirmode

    if oct(os.stat(filename).st_mode & 0o777) != oct(mode):
        os.chmod(filename, mode)

def has_setting(setting):
    try:
        eval("settings." + setting)
        return True
    except:
        return False

def check_setting(setting, default):
    if has_setting(setting):
        return eval("settings." + setting)
    else:
        return default

def title(pagename):
    if pagename == '':
        return settings.basetitle
    else:
        return settings.basetitle + ' - ' + pagename

def copyright():
    now = dt.datetime.now()
    return '&copy; ' + str(now.year) + settings.owner
