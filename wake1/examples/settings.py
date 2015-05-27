# Directories
sourcedir = "../source"
outputdir = "../output"
#templatedir = "../templates"

# Remote (production environment) settings
remote_dest = "mycoolsite.com:www"
upload_command = ["rsync", "-av", "--del", outputdir + "/", remote_dest + "/"]

# Site Settings
docroot = "http://google.com/"
owner = "Web Master"
basetitle = owner + "'s Cool Site"

# File Handling
#filemode = 0o644
#dirmode = 0o755

# Module Settings
#modules = ["markdown", "dir", "copy"]
#markdown_template = "markdown.html"
#markdown_extras = ["metadata", "fenced-code-blocks", "smarty-pants"]
