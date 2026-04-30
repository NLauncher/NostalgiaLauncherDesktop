package net.eqozqq.nostalgialauncherdesktop;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;
        return Objects.equals(name, version.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}