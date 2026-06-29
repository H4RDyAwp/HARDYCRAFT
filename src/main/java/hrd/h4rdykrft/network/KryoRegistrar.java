package hrd.h4rdykrft.network;

import com.esotericsoftware.kryo.Kryo;

public final class KryoRegistrar {

    private KryoRegistrar() {
    }

    public static void register(Kryo kryo) {
        kryo.register(java.util.ArrayList.class);
        kryo.register(NetworkMessages.PlayerInfo.class);
        kryo.register(NetworkMessages.JoinRequest.class);
        kryo.register(NetworkMessages.JoinResponse.class);
        kryo.register(NetworkMessages.PlayerJoined.class);
        kryo.register(NetworkMessages.PlayerLeft.class);
        kryo.register(NetworkMessages.PlayerUpdate.class);
        kryo.register(NetworkMessages.BlockUpdate.class);
    }
}
