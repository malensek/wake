/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package io.sigpipe.wake.plugins.gallery;

import java.awt.Dimension;

class ImageDescriptor {

    private static final String figureTag
        = "<figure itemprop=\"associatedMedia\" itemscope "
        + "itemtype=\"http://schema.org/ImageObject\" class=\"gallery_fig\">";

    public String description = "";
    public Dimension dims;
    public String fileName;
    public String thumbnail;
    public String thumbnail2x = "";

    public String toHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append(figureTag);
        sb.append(System.lineSeparator());
        sb.append("<a href=\"" + fileName + "\" itemprop=\"contentUrl\" "
                + "data-size=\"" + dims.width + "x" + dims.height + "\">");
        sb.append("<img src=\"" + thumbnail + "\" itemprop=\"thumbnail\" ");
        if (this.thumbnail2x.equals("") == false) {
            sb.append("srcset=\"" + thumbnail + " 1x, "
                    + thumbnail2x + " 2x\"");
        }
        sb.append("alt=\"" + description + "\">");
        sb.append("</a>");
        sb.append(System.lineSeparator());
        if (description.equals("") == false) {
            sb.append("<figcaption itemprop=\"caption description\" ");
            sb.append("class=\"gallery_description\">");
            sb.append(description);
            sb.append("</figcaption>");
            sb.append(System.lineSeparator());
        }
        sb.append("</figure>");
        sb.append(System.lineSeparator());
        return sb.toString();
    }
}

