package hrd.h4rdykrft.network;

import com.esotericsoftware.kryo.Kryo;

public final class KryoRegistrar {

    private KryoRegistrar() {
    }

    public static void register(Kryo kryo) {
        // 1. Register ArrayList to fix the JoinResponse crash
        kryo.register(java.util.ArrayList.class);

        // 2. Register PlayerInfo (because it lives inside the ArrayList)
        kryo.register(NetworkMessages.PlayerInfo.class);

        // 3. Register the rest of your network messages in order
        kryo.register(NetworkMessages.JoinRequest.class);
        kryo.register(NetworkMessages.JoinResponse.class);
        kryo.register(NetworkMessages.PlayerJoined.class);
        kryo.register(NetworkMessages.PlayerLeft.class);
        kryo.register(NetworkMessages.PlayerUpdate.class);
        kryo.register(NetworkMessages.BlockUpdate.class);
    }
}