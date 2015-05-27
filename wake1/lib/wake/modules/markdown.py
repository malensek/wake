from jinja2 import Environment, FileSystemLoader
import markdown
import os

import wake.settings as settings
import wake.util as util

markdown_extensions = util.check_setting("markdown_extensions",
        ["markdown.extensions.meta", "markdown.extensions.smarty",
        "markdown.extensions.fenced_code", "markdown.extensions.codehilite"])
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
    md = markdown.Markdown(extensions=markdown_extensions)
    mdtext = md.convert(contents)

    jinja = Environment(loader=FileSystemLoader(settings.templatedir))
    jinja.globals["settings"] = settings
    template = jinja.get_template(markdown_template)

    title = ""
    if "title" in md.Meta:
        title = md.Meta["title"][0]
    title = util.title(title)

    html = template.render(content=mdtext, metadata=md.Meta,
            title=title)

    print("markdown: " + filename + " -> " + outfile)
    fout = open(outfile, 'w')
    fout.write(html)
    fout.close()
