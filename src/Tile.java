public enum Tile {
	BABA("baba.png"),
	FLAG("flag.png"),
	WALL("wall.png"),

	BABA_W("baba_w.png", BABA),
	FLAG_W("flag_w.png", FLAG),
	WALL_W("wall_w.png", WALL),

	IS("is.png", Type.LOGIC_WORD),

	YOU("you.png", Type.STATE_WORD),
	WIN("win.png", Type.STATE_WORD),
	MOVE("move.png", Type.STATE_WORD);

	final String texture;
	final boolean word, blockWord, stateWord;
	final Tile block;

	boolean you, win, move;

	Tile(String texture, Type type, Tile block) {
		this.texture = texture;
		this.block = block;

		switch (type) {
			case LOGIC_WORD:
				word = true;
				blockWord = stateWord = false;
				break;
			case BLOCK_WORD:
				word = blockWord = true;
				stateWord = false;
				break;
			case STATE_WORD:
				word = stateWord = true;
				blockWord = false;
				break;
			default:
				word = blockWord = stateWord = false;
		}
	}

	Tile(String texture, Tile block) {
		this(texture, Type.BLOCK_WORD, block);
	}

	Tile(String texture, Type type) {
		this(texture, type, null);
	}

	Tile(String texture) {
		this(texture, Type.BLOCK, null);
	}

	private enum Type {
		BLOCK, LOGIC_WORD, BLOCK_WORD, STATE_WORD
	}
}
