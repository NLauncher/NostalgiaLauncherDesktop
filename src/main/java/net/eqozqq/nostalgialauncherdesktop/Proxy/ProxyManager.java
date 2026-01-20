package net.eqozqq.nostalgialauncherdesktop.Proxy;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ProxyManager {
    private static ProxyManager instance;

    private String currentAddress;
    private int currentPort;

    private volatile boolean running;
    private DatagramSocket socket;
    private Thread proxyThread;

    private ProxyManager() {
    }

    public static synchronized ProxyManager getInstance() {
        if (instance == null) {
            instance = new ProxyManager();
        }
        return instance;
    }

    public synchronized void startProxy(String address, int port) {
        if (running) {
            stopProxy();
        }

        this.currentAddress = address;
        this.currentPort = port;
        this.running = true;

        this.proxyThread = new Thread(() -> runProxyLoop(address, port));
        this.proxyThread.start();
    }

    private void runProxyLoop(String targetAddress, int targetPort) {
        InetSocketAddress clientAddr = null;
        try {
            InetAddress srcInetAddr = InetAddress.getByName(targetAddress);
            InetSocketAddress srcAddr = new InetSocketAddress(srcInetAddr, targetPort);

            socket = new DatagramSocket(19132);
            socket.setSoTimeout(100);

            byte[] buffer = new byte[4096];

            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    InetSocketAddress recvAddr = new InetSocketAddress(
                            packet.getAddress(),
                            packet.getPort());

                    if (recvAddr.equals(srcAddr)) {
                        if (clientAddr != null) {
                            DatagramPacket sendPacket = new DatagramPacket(
                                    packet.getData(),
                                    packet.getLength(),
                                    clientAddr.getAddress(),
                                    clientAddr.getPort());
                            socket.send(sendPacket);
                        }
                    } else {
                        if (clientAddr == null || clientAddr.getAddress().equals(recvAddr.getAddress())) {
                            clientAddr = recvAddr;
                            DatagramPacket sendPacket = new DatagramPacket(
                                    packet.getData(),
                                    packet.getLength(),
                                    srcAddr.getAddress(),
                                    srcAddr.getPort());
                            socket.send(sendPacket);
                        }
                    }
                } catch (java.net.SocketTimeoutException e) {
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            running = false;
        }
    }

    public synchronized void stopProxy() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (proxyThread != null) {
            try {
                proxyThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            proxyThread = null;
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public int getCurrentPort() {
        return currentPort;
    }
}