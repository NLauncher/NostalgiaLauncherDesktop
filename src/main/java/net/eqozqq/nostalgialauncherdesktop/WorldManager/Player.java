package net.eqozqq.nostalgialauncherdesktop.WorldManager;

public class Player {
    private Vector3f location;
    private PlayerAbilities abilities = new PlayerAbilities();

    public Vector3f getLocation() {
        return location;
    }

    public void setLocation(Vector3f location) {
        this.location = location;
    }

    public PlayerAbilities getAbilities() {
        return abilities;
    }
}