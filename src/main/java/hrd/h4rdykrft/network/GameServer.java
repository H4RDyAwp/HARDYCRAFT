package hrd.h4rdykrft.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import hrd.h4rdykrft.world.Chunk;
import hrd.h4rdykrft.world.World;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {
    private final Server server;
    private final World world;
    private final Map<Integer, ServerPlayer> players = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    private static final class ServerPlayer {
        final String uuid;
        final String username;
        final Vector3f position = new Vector3f();
        float yaw = -90f;
        float pitch;

        ServerPlayer(String uuid, String username, Vector3f spawn) {
            this.uuid = uuid;
            this.username = username;
            this.position.set(spawn);
        }

        NetworkMessages.PlayerInfo toInfo() {
            return new NetworkMessages.PlayerInfo(
                    uuid, username,
                    position.x, position.y, position.z,
                    yaw, pitch
            );
        }
    }

    public GameServer() {
        Chunk.headlessMode = true;
        world = new World();
        server = new Server();
        KryoRegistrar.register(server.getKryo());

        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof NetworkMessages.JoinRequest request) {
                    handleJoin(connection, request);
                } else if (object instanceof NetworkMessages.PlayerUpdate update) {
                    handlePlayerUpdate(connection, update);
                } else if (object instanceof NetworkMessages.BlockUpdate blockUpdate) {
                    handleBlockUpdate(connection, blockUpdate);
                }
            }

            @Override
            public void disconnected(Connection connection) {
                handleDisconnect(connection);
            }
        });
    }

    public void start(int tcpPort, int udpPort) throws IOException {
        server.start();
        server.bind(tcpPort, udpPort);
        System.out.println("HARDYCRAFT server listening on TCP " + tcpPort + ", UDP " + udpPort);
    }

    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        while (running) {
            for (ServerPlayer player : players.values()) {
                world.update(player.position.x, player.position.z);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        if (world != null) {
            world.shutdown();
        }
        if (server != null) {
            server.stop();
        }
        System.out.println("Server stopped.");
    }

    private void handleJoin(Connection connection, NetworkMessages.JoinRequest request) {
        if (players.containsKey(connection.getID())) {
            return;
        }

        String username = sanitizeUsername(request.username);
        String uuid = UUID.randomUUID().toString();
        Vector3f spawn = new Vector3f(8f + players.size() * 2f, 30f, 22f);
        ServerPlayer player = new ServerPlayer(uuid, username, spawn);
        players.put(connection.getID(), player);

        NetworkMessages.JoinResponse response = new NetworkMessages.JoinResponse();
        response.uuid = uuid;
        response.spawnX = spawn.x;
        response.spawnY = spawn.y;
        response.spawnZ = spawn.z;
        for (ServerPlayer existing : players.values()) {
            if (!existing.uuid.equals(uuid)) {
                response.existingPlayers.add(existing.toInfo());
            }
        }
        connection.sendTCP(response);

        NetworkMessages.PlayerJoined joined = new NetworkMessages.PlayerJoined(player.toInfo());
        server.sendToAllExceptTCP(connection.getID(), joined);

        System.out.println("Player joined: " + username + " (" + uuid + ")");
    }

    private void handlePlayerUpdate(Connection connection, NetworkMessages.PlayerUpdate update) {
        ServerPlayer player = players.get(connection.getID());
        if (player == null || !player.uuid.equals(update.uuid)) {
            return;
        }

        player.position.set(update.x, update.y, update.z);
        player.yaw = update.yaw;
        player.pitch = update.pitch;

        server.sendToAllExceptUDP(connection.getID(), update);
    }

    private void handleBlockUpdate(Connection connection, NetworkMessages.BlockUpdate update) {
        if (players.get(connection.getID()) == null) {
            return;
        }

        world.setBlock(update.x, update.y, update.z, update.blockId);
        server.sendToAllExceptTCP(connection.getID(), update);
    }

    private void handleDisconnect(Connection connection) {
        ServerPlayer player = players.remove(connection.getID());
        if (player != null) {
            server.sendToAllTCP(new NetworkMessages.PlayerLeft(player.uuid));
            System.out.println("Player left: " + player.username);
        }
    }

    private static String sanitizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return "Player";
        }
        String trimmed = username.trim();
        return trimmed.length() > 16 ? trimmed.substring(0, 16) : trimmed;
    }

    public static void main(String[] args) throws IOException {
        int tcpPort = NetworkConfig.TCP_PORT;
        int udpPort = NetworkConfig.UDP_PORT;

        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i]) && i + 1 < args.length) {
                tcpPort = Integer.parseInt(args[i + 1]);
                udpPort = tcpPort + 1;
            }
        }

        GameServer gameServer = new GameServer();
        gameServer.start(tcpPort, udpPort);
        gameServer.run();
    }
}
