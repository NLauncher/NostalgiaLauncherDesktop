package net.eqozqq.nostalgialauncherdesktop;

public class Version {
    private String name;
    private String url;

    public Version() {
    }

    public Version(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return name;
    }
}