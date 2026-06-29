package hrd.h4rdykrft.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import hrd.h4rdykrft.player.Player;
import hrd.h4rdykrft.world.World;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameClient {
    public interface MultiplayerListener {
        void onConnected(String uuid, Vector3f spawn);

        void onPlayerJoined(NetworkMessages.PlayerInfo info);

        void onPlayerLeft(String uuid);

        void onBlockUpdate(int x, int y, int z, byte blockId);
    }

    private final Client client;
    private final World world;
    private final Map<String, Player> remotePlayers = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Runnable> mainThreadTasks = new ConcurrentLinkedQueue<>();

    private MultiplayerListener listener;
    private String localUuid;
    private boolean connected;
    private float updateTimer;

    public GameClient(World world) {
        this.world = world;
        client = new Client();
        KryoRegistrar.register(client.getKryo());

        client.addListener(new com.esotericsoftware.kryonet.Listener() {
            @Override
            public void connected(Connection connection) {
                connection.sendTCP(new NetworkMessages.JoinRequest(getUsername()));
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof NetworkMessages.JoinResponse response) {
                    handleJoinResponse(response);
                } else if (object instanceof NetworkMessages.PlayerJoined joined) {
                    enqueue(() -> handlePlayerJoined(joined.player));
                } else if (object instanceof NetworkMessages.PlayerLeft left) {
                    enqueue(() -> handlePlayerLeft(left.uuid));
                } else if (object instanceof NetworkMessages.PlayerUpdate update) {
                    enqueue(() -> handlePlayerUpdate(update));
                } else if (object instanceof NetworkMessages.BlockUpdate blockUpdate) {
                    enqueue(() -> handleBlockUpdate(blockUpdate));
                }
            }

            @Override
            public void disconnected(Connection connection) {
                connected = false;
                System.out.println("Disconnected from server.");
            }
        });
    }

    public void setListener(MultiplayerListener listener) {
        this.listener = listener;
    }

    public void connect(String host, int tcpPort, int udpPort, String username) throws IOException {
        this.username = sanitizeUsername(username);
        client.start();
        client.connect(NetworkConfig.CONNECT_TIMEOUT_MS, host, tcpPort, udpPort);
    }

    public void disconnect() {
        if (client != null) {
            client.stop();
        }
        remotePlayers.clear();
        connected = false;
    }

    public void processEvents() {
        Runnable task;
        while ((task = mainThreadTasks.poll()) != null) {
            task.run();
        }
    }

    public void update(float deltaTime, Player localPlayer) {
        processEvents();

        if (!connected || localUuid == null) {
            return;
        }

        updateTimer += deltaTime;
        if (updateTimer >= NetworkConfig.PLAYER_UPDATE_INTERVAL) {
            updateTimer = 0f;
            Vector3f pos = localPlayer.getPosition();
            client.sendUDP(new NetworkMessages.PlayerUpdate(
                    localUuid,
                    pos.x, pos.y, pos.z,
                    localPlayer.getYaw(),
                    localPlayer.getPitch()
            ));
        }

        for (Player remote : remotePlayers.values()) {
            remote.updateRemote(deltaTime);
        }
    }

    public void sendBlockChange(int x, int y, int z, byte blockId) {
        if (connected) {
            client.sendTCP(new NetworkMessages.BlockUpdate(x, y, z, blockId));
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public String getLocalUuid() {
        return localUuid;
    }

    public Map<String, Player> getRemotePlayers() {
        return remotePlayers;
    }

    private String username = "Player";

    private String getUsername() {
        return username;
    }

    private void handleJoinResponse(NetworkMessages.JoinResponse response) {
        enqueue(() -> {
            localUuid = response.uuid;
            connected = true;
            Vector3f spawn = new Vector3f(response.spawnX, response.spawnY, response.spawnZ);

            for (NetworkMessages.PlayerInfo info : response.existingPlayers) {
                addRemotePlayer(info);
            }

            if (listener != null) {
                listener.onConnected(localUuid, spawn);
            }
            System.out.println("Connected to server as " + username);
        });
    }

    private void handlePlayerJoined(NetworkMessages.PlayerInfo info) {
        if (localUuid != null && localUuid.equals(info.uuid)) {
            return;
        }
        addRemotePlayer(info);
        if (listener != null) {
            listener.onPlayerJoined(info);
        }
    }

    private void handlePlayerLeft(String uuid) {
        remotePlayers.remove(uuid);
        if (listener != null) {
            listener.onPlayerLeft(uuid);
        }
    }

    private void handlePlayerUpdate(NetworkMessages.PlayerUpdate update) {
        if (localUuid != null && localUuid.equals(update.uuid)) {
            return;
        }

        Player remote = remotePlayers.get(update.uuid);
        if (remote == null) {
            remote = new Player(update.uuid, "Remote", new Vector3f(), false);
            remotePlayers.put(update.uuid, remote);
        }
        remote.applyNetworkState(update.x, update.y, update.z, update.yaw, update.pitch);
    }

    private void handleBlockUpdate(NetworkMessages.BlockUpdate update) {
        world.setBlock(update.x, update.y, update.z, update.blockId);
        if (listener != null) {
            listener.onBlockUpdate(update.x, update.y, update.z, update.blockId);
        }
    }

    private void addRemotePlayer(NetworkMessages.PlayerInfo info) {
        Player remote = new Player(
                info.uuid,
                info.username,
                new Vector3f(info.x, info.y, info.z),
                false
        );
        remote.setYaw(info.yaw);
        remote.setPitch(info.pitch);
        remotePlayers.put(info.uuid, remote);
    }

    private void enqueue(Runnable task) {
        mainThreadTasks.add(task);
    }

    private static String sanitizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return "Player";
        }
        String trimmed = username.trim();
        return trimmed.length() > 16 ? trimmed.substring(0, 16) : trimmed;
    }
}
