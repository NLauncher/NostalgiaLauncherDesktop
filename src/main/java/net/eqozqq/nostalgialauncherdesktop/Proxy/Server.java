package net.eqozqq.nostalgialauncherdesktop.Proxy;

public class Server {
    private String address;
    private int port;

    public Server(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return address + ":" + port;
    }
}