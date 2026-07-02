package net.eqozqq.nostalgialauncherdesktop.marketplace;

import java.util.List;

public class MarketplaceItem {
    public String title;
    public String version;
    public String short_description;
    public String full_description;
    public String thumbnail;
    public String file;
    public String date;
    public String type;
    public String alt_link;
    public String original_link;
    public Author author;
    public List<String> screenshots;

    public static class Author {
        public String name;
        public String avatar;
    }
}
