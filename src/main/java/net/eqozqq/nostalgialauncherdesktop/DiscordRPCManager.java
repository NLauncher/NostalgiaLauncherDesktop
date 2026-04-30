package net.eqozqq.nostalgialauncherdesktop;

import dev.firstdark.rpc.DiscordRpc;
import dev.firstdark.rpc.handlers.DiscordEventHandler;
import dev.firstdark.rpc.models.DiscordJoinRequest;
import dev.firstdark.rpc.models.DiscordRichPresence;
import dev.firstdark.rpc.models.User;
import dev.firstdark.rpc.enums.ErrorCode;

public class DiscordRPCManager {

    private static final String APPLICATION_ID = "1476548485481238741";
    private static DiscordRPCManager instance;
    private DiscordRpc rpc;
    private final long startTimestamp = System.currentTimeMillis() / 1000L;

    private DiscordRPCManager() {
    }

    public static synchronized DiscordRPCManager getInstance() {
        if (instance == null) {
            instance = new DiscordRPCManager();
        }
        return instance;
    }

    public void init() {
        new Thread(() -> {
            try {
                rpc = new DiscordRpc();
                rpc.init(APPLICATION_ID, new DiscordEventHandler() {
                    @Override
                    public void ready(User user) {
                        updatePresence("On Main Page");
                    }

                    @Override
                    public void disconnected(ErrorCode code, String message) {
                    }

                    @Override
                    public void errored(ErrorCode code, String message) {
                    }

                    @Override
                    public void joinGame(String secret) {
                    }

                    @Override
                    public void spectateGame(String secret) {
                    }

                    @Override
                    public void joinRequest(DiscordJoinRequest request) {
                    }
                }, false);
            } catch (Exception e) {
                rpc = null;
            }
        }, "Discord-RPC-Init").start();
    }

    public void updatePresence(String state) {
        if (rpc == null)
            return;
        new Thread(() -> {
            try {
                DiscordRichPresence presence = DiscordRichPresence.builder()
                        /* .details("MCPE Alpha launcher for Windows & Linux") */
                        .state(state)
                        .largeImageKey("launcher_icon")
                        .largeImageText("NostalgiaLauncher")
                        .startTimestamp(startTimestamp)
                        .build();
                rpc.updatePresence(presence);
                rpc.runCallbacks();
            } catch (Exception e) {
            }
        }, "Discord-RPC-Update").start();
    }

    public void shutdown() {
        if (rpc == null)
            return;
        try {
            rpc.shutdown();
        } catch (Exception e) {
        }
        rpc = null;
    }
}