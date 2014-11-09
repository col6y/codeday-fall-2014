package server;

import java.util.Random;

public class BossContext extends CombatantContext {

	private static final int DEFAULT_BOSS_HEALTH = 15;
	private static final String DEFAULT_BOSS_NAME = "dragon";
	private final Random random = new Random();
	private static final String[] attacks = new String[] { "burn", "slam", "swipe" };

	public BossContext(GameContext game) {
		super(game, DEFAULT_BOSS_HEALTH, "boss");
		game.storage.put("name.boss", DEFAULT_BOSS_NAME);
		game.storage.put("class.boss", "boss");
	}

	@Override
	public String getID() {
		return "boss";
	}

	@Override
	public String getName() {
		return DEFAULT_BOSS_NAME;
	}

	@Override
	public void resetCombatant() {
		resetStatusAndHealth(DEFAULT_BOSS_HEALTH);
	}

	@Override
	public String getTargetName(CombatantContext[] combatants) {
		int count = 0;
		for (CombatantContext comb : combatants) {
			if (comb != null && comb != this && !comb.isDead()) {
				count++;
			}
		}
		if (count == 0) {
			return null;
		}
		int index = random.nextInt(count);
		for (CombatantContext comb : combatants) {
			if (comb != null && comb != this && !comb.isDead() && index-- == 0) {
				return comb.getID();
			}
		}
		return null;
	}

	@Override
	public String getAttackType() {
		return isDead() ? null : attacks[random.nextInt(attacks.length)];
	}

	@Override
	public String getClassName() {
		return "boss";
	}

	public int getMoveUses(String move) {
		return 0;
	}
}
