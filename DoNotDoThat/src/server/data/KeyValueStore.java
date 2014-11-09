package server.data;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Objects;

import server.ClientContext;
import server.logger.Logger;
import server.netio.Packet;
import server.netio.PacketOutputStream;

public class KeyValueStore {
	private final HashMap<String, Object> data = new HashMap<>();
	private final LinkedHashSet<String> dirty = new LinkedHashSet<>();

	public synchronized Object get(String key) {
		return data.get(key);
	}

	public synchronized Object remove(String key) {
		if (!data.containsKey(key)) {
			return null;
		}
		dirty.add(key);
		return data.remove(key);
	}

	public synchronized void put(String key, Object value) {
		if (Objects.equals(data.get(key), value)) {
			return;
		}
		Logger.finer("Put " + key + " = " + value);
		dirty.add(key);
		data.put(key, value);
	}

	public synchronized void sendUpdates(PacketOutputStream pout)
			throws IOException {
		Logger.fine("About to send to client: " + pout);
		ByteBuffer enc = ByteBuffer.allocate(4096);
		Logger.fine("SIZE1: " + dirty.size());
		for (String dirtykey : dirty) {
			Logger.finer("Sending key " + dirtykey);
			Packet out = new Packet();
			enc.clear();
			byte[] key;
			try {
				key = dirtykey.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				Logger.warning("Cannot encode: UTF-8");
				continue;
			}
			if (key.length != (key.length & 0xFF)) {
				Logger.warning("Key length too long: " + key.length);
				continue;
			}
			if (data.containsKey(dirtykey)) {
				out.type = 0x0204;
				enc.put((byte) key.length);
				enc.put(key);
				Encoder.encode(data.get(dirtykey), enc);
			} else {
				out.type = 0x0102;
				enc.put(key);
				// no additional data
			}
			out.data = Arrays.copyOf(enc.array(), enc.position());
			pout.write(out);
		}
		dirty.clear();
		Logger.fine("SIZE2: " + dirty.size());
	}

	public synchronized void sendUpdatesAll(ClientContext[] clients) {
		for (ClientContext client : clients) {
			if (client != null) {
				try {
					sendUpdates(client.getPacketOutput());
				} catch (IOException ex) {
					Logger.warning("Terminating client due to IO exception: "
							+ client, ex);
					client.terminate();
				}
			}
		}
	}
}
