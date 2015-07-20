package wake.plugins.gallery;

import java.awt.Dimension;

class ImageDescriptor {

    private static final String figureTag
        = "<figure itemprop=\"associatedMedia\" itemscope "
        + "itemtype=\"http://schema.org/ImageObject\">";

    public String fileName;
    public String thumbnail;
    public Dimension dims;
    public String description = "";

    public String toHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append(figureTag);
        sb.append("<a href=\"" + fileName + "\" itemprop=\"contentUrl\" "
                + "data-size=\"" + dims.width + "x" + dims.height + "\">");
        sb.append("<img src=\"" + thumbnail + "\" itemprop=\"thumbnail\" "
                + "alt=\"" + description + "\">");
        sb.append("</a></figure>");
        return sb.toString();
    }
}
