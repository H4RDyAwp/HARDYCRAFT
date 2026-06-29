package hrd.h4rdykrft.network;

import java.util.ArrayList;
import java.util.List;

public final class NetworkMessages {

    public static class PlayerInfo {
        public String uuid;
        public String username;
        public float x;
        public float y;
        public float z;
        public float yaw;
        public float pitch;

        public PlayerInfo() {
        }

        public PlayerInfo(String uuid, String username, float x, float y, float z, float yaw, float pitch) {
            this.uuid = uuid;
            this.username = username;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    public static class JoinRequest {
        public String username;

        public JoinRequest() {
        }

        public JoinRequest(String username) {
            this.username = username;
        }
    }

    public static class JoinResponse {
        public String uuid;
        public float spawnX;
        public float spawnY;
        public float spawnZ;
        public List<PlayerInfo> existingPlayers = new ArrayList<>();

        public JoinResponse() {
        }
    }

    public static class PlayerJoined {
        public PlayerInfo player;

        public PlayerJoined() {
        }

        public PlayerJoined(PlayerInfo player) {
            this.player = player;
        }
    }

    public static class PlayerLeft {
        public String uuid;

        public PlayerLeft() {
        }

        public PlayerLeft(String uuid) {
            this.uuid = uuid;
        }
    }

    public static class PlayerUpdate {
        public String uuid;
        public float x;
        public float y;
        public float z;
        public float yaw;
        public float pitch;

        public PlayerUpdate() {
        }

        public PlayerUpdate(String uuid, float x, float y, float z, float yaw, float pitch) {
            this.uuid = uuid;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    public static class BlockUpdate {
        public int x;
        public int y;
        public int z;
        public byte blockId;

        public BlockUpdate() {
        }

        public BlockUpdate(int x, int y, int z, byte blockId) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockId = blockId;
        }
    }

    private NetworkMessages() {
    }
}
