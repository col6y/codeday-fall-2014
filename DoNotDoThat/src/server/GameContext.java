package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import server.data.KeyValueStore;

public class GameContext {
	public final KeyValueStore storage = new KeyValueStore();

	public void processGame(ServerContext serverContext) {
		ClientContext[] players = serverContext.listPlayers();
		int count = 0, ready = 0;
		for (int i = 0; i < players.length; i++) {
			storage.put("connected." + i, players[i] != null);
			if (players[i] != null) {
				count++;
				Boolean b = (Boolean) storage.get("isready." + i);
				if (b != null && b) {
					ready++;
				}
			}
		}
		Boolean b = (Boolean) storage.get("mode.isinlobby");
		if (b != null && b) {
			if (count >= 2 && count == ready) {
				storage.put("mode.isinlobby", false);
				storage.put("mode.countdown", 30);
				for (int i = 0; i < players.length; i++) {
					storage.put("attack." + i, null);
				}
			}
		} else {
			int countdown = (int) storage.get("mode.countdown");
			int needed = 0;
			for (int i = 0; i < players.length; i++) {
				if (storage.get("attack." + players) != null) {
					needed++;
				}
			}
			if (needed == count || countdown <= 0) { // TURN OVER
				processTurn(players);
			}
		}
	}

	public void processTurn(ClientContext[] players) {
		for (int i = 0; i < players.length; i++) {
			CombatantContext target = (CombatantContext) storage.get("target."
					+ i);
			storage.put("attack." + i, null);
		}
		for (int i = 0; i < players.length; i++) {
			storage.put("attack." + i, null);
		}
	}

	private CombatantContext getCombatant(ClientContext user, String name) {
		if (name.equals("self")) {
			return user;
		}
		ClientContext[] plys = user.serverContext.listPlayers();
		for (int i=0; i<plys.length; i++) {
			if (name.equals(plys[i].name)) {
				return plys[i];
			}
		}
		return null;
	}

	public void initGame() {
		storage.put("mode.isinlobby", true);
	}

	private static final HashMap<String, List<String>> commands = new HashMap<>();

	static {
		commands.put("wizard",
				Arrays.asList("burn", "grind", "drown", "blast", "zap"));
		commands.put("soldier",
				Arrays.asList("shoot", "bombard", "punch", "kick", "stun"));
		commands.put("ranger",
				Arrays.asList("draw", "shank", "slash", "throw", "kick"));
		commands.put("robot", Arrays.asList("pew", "pewpew", "pewpewpew",
				"pewpewpewpew", "pow"));
	}

	public void queueAttack(ClientContext client, String cmdname, String who) {
		String cls = (String) storage.get("class." + client.clientId);
		List<String> valid = commands.get(cls);
		if (!valid.contains(cmdname)) {
			client.receivedChatMessage("Your class cannot use that attack!");
			return;
		}
		storage.put("attack." + client.clientId, cmdname);
		CombatantContext out = getCombatant(client, who);
		if (out != null) {
			storage.put("target." + client.clientId, out);
		}
	}

	public void sendChatMessage(ClientContext client, String textline) {
		for (ClientContext target : client.serverContext.listPlayers()) {
			target.receivedChatMessage("[" + target.clientId + "] " + textline);
		}
	}
}
