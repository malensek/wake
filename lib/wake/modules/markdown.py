from jinja2 import Environment, FileSystemLoader
import markdown2 as md
import os

import wake.settings as settings
import wake.util as util

markdown_extras = util.check_setting("markdown_extras",
        ["metadata", "fenced-code-blocks", "smarty-pants"])
markdown_template = util.check_setting("markdown_template", "markdown.html")

def name():
    return "markdown"

def wants(filename):
    return util.ext_of(filename) == '.md'

def produces(filename):
    basename, _ = os.path.splitext(filename)
    return [util.src2out(basename) + ".html"]

def process(filename):
    outfile = produces(filename)[0]
    util.check_dir(outfile)

    fin = open(filename)

    contents = fin.read()
    mdtext = md.markdown(contents, extras=markdown_extras)

    jinja = Environment(loader=FileSystemLoader(settings.templatedir))
    jinja.globals["settings"] = settings
    template = jinja.get_template(markdown_template)

    title = ""
    if "title" in mdtext.metadata:
        title = mdtext.metadata["title"]
    title = util.title(title)

    html = template.render(content=mdtext, metadata=mdtext.metadata,
            title=title)

    print("markdown: " + args + " -> " + outfile)
    fout = open(outfile, 'w')
    fout.write(html)
    fout.close()
