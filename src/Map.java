import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;

public class Map {
	public static final int TILE_SIZE = 64;

	private static final String ASSETS_PATH = "assets";
	private static final int MAX_HISTORY = 512;

	private final int width, height;

	private Tile[][] map;

	private final ArrayDeque<Tile[][]> history;

	private final BufferedImage[] textures;

	private final boolean[][] moved;
	private final boolean[][] activeWords;

	public Map() {
		history = new ArrayDeque<>();

		width = 12;
		height = 12;

		moved = new boolean[height][width];
		activeWords = new boolean[height][width];

		textures = new BufferedImage[Tile.values().length];

		reset();
		loadTextures();
	}

	public void move(Direction dir) {
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				moved[y][x] = false;
			}
		}

		Tile[][] mapCopy = new Tile[height][];

		for (int y = 0; y < height; ++y) {
			mapCopy[y] = Arrays.copyOf(map[y], width);
		}

		history.add(mapCopy);

		boolean moved = false;

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				if (map[y][x] != null && map[y][x].you) {
					if (move(x, y, dir)) {
						moved = true;
					}
				}
			}
		}

		if (moved) {
			updateRules();

			if (history.size() >= MAX_HISTORY) {
				history.removeFirst();
			}
		} else {
			history.removeLast();
		}
	}

	public void undo() {
		if (!history.isEmpty()) {
			map = history.removeLast();
			updateRules();
		}
	}

	public void reset() {
		map = new Tile[height][width];

		history.clear();

		map[3][2] = Tile.BABA;

		map[5][2] = Tile.WALL_W;
		map[5][3] = Tile.BABA_W;
		map[6][3] = Tile.IS;
		map[7][3] = Tile.YOU;
		map[7][2] = Tile.WALL_W;
		map[5][4] = Tile.MOVE;

		map[10][1] = Tile.FLAG_W;
		map[10][2] = Tile.IS;
		map[10][3] = Tile.WIN;

		for (int y = 0; y < height; ++y) {
			map[y][8] = Tile.WALL;
		}

		map[1][11] = Tile.FLAG;

		updateRules();
	}

	public void draw(Graphics2D g) {
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				int x1 = x * TILE_SIZE, y1 = y * TILE_SIZE;

				if (activeWords[y][x]) {
					g.setColor(Color.GRAY);

					g.fillRect(x1, y1, TILE_SIZE, TILE_SIZE);
				}

				if (map[y][x] != null) {
					g.drawImage(textures[map[y][x].ordinal()], x1, y1, TILE_SIZE, TILE_SIZE, null);
				}

				g.setColor(Color.DARK_GRAY);

				g.drawRect(x1, y1, TILE_SIZE - 1, TILE_SIZE - 1);
			}
		}
	}

	private boolean move(int x, int y, Direction dir) {
		if (y == 0 && dir == Direction.UP) return false;
		if (y == height - 1 && dir == Direction.DOWN) return false;
		if (x == 0 && dir == Direction.LEFT) return false;
		if (x == width - 1 && dir == Direction.RIGHT) return false;

		if (moved[y][x]) return false;

		int x1 = x + dir.x, y1 = y + dir.y;

		Tile target = map[y1][x1];

		if (target != null) {
			if (target.win) {
				win();

				return false;
			}

			if (target.move || target.you) {
				if (!move(x1, y1, dir)) {
					return false;
				}
			} else {
				return false;
			}
		}

		map[y1][x1] = map[y][x];
		moved[y1][x1] = true;

		map[y][x] = null;

		return true;
	}

	private void updateRules() {
		for (Tile tile : Tile.values()) {
			tile.you = tile.win = false;

			tile.move = tile.word;
		}

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				activeWords[y][x] = false;
			}
		}

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				if (map[y][x] == Tile.IS) {
					if (y != 0 && y != height - 1) {
						int t = addRule(map[y - 1][x], Tile.IS, map[y + 1][x]);

						if (t == 2) {
							return;
						}

						if (t == 1) {
							activeWords[y - 1][x] = true;
							activeWords[y][x] = true;
							activeWords[y + 1][x] = true;
						}
					}
					if (x != 0 && x != width - 1) {
						int t = addRule(map[y][x - 1], Tile.IS, map[y][x + 1]);

						if (t == 2) {
							return;
						}

						if (t == 1) {
							activeWords[y][x - 1] = true;
							activeWords[y][x] = true;
							activeWords[y][x + 1] = true;
						}
					}
				}
			}
		}
	}

	private byte addRule(Tile... tiles) {
		assert tiles.length == 3;

		for (Tile tile : tiles) {
			if (tile == null) {
				return 0;
			}
		}

		if (tiles[1] == Tile.IS) {
			if (tiles[0].blockWord) {
				Tile block = tiles[0].block;

				if (tiles[2].blockWord) {
					for (int y = 0; y < height; ++y) {
						for (int x = 0; x < width; ++x) {
							if (map[y][x] == block) {
								map[y][x] = tiles[2].block;
							}
						}
					}

					return 1;
				} else if (tiles[2].stateWord) {
					switch (tiles[2]) {
						case YOU: block.you = true; break;
						case WIN: block.win = true; break;
						case MOVE: block.move = true; break;
					}

					if (block.you && block.win) {
						win();

						return 2;
					}

					return 1;
				}
			}
		}

		return 0;
	}

	private void win() {
		System.out.println("Win!");
		reset();
	}

	private void loadTextures() {
		Font wordsFont = new Font("Consolas", Font.PLAIN, 24);

		BufferedImage nullTexture = null;

		for (int i = 0; i < textures.length; ++i) {
			Tile tile = Tile.values()[i];

			String path = String.valueOf(Paths.get(ASSETS_PATH, tile.texture));

			try {
				textures[i] = ImageIO.read(new File(path));
			} catch (IOException e) {
				if (tile.word) {
					String name = tile.name().toLowerCase();

					name = (name.equals("is") ? name.charAt(0) : Character.toUpperCase(name.charAt(0)))
							+ name.substring(1, name.length() - (name.endsWith("_w") ? 2 : 0));

					textures[i] = Util.createImageFromText(name, TILE_SIZE, TILE_SIZE, new Color(0xdcdcdc), new Color(0, true), wordsFont);

					continue;
				}

				System.err.println(e.getMessage() + " Path: " + path);

				if (nullTexture == null) {
					nullTexture = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);

					Graphics2D g = nullTexture.createGraphics();

					g.setColor(Color.MAGENTA);
					g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
				}

				textures[i] = nullTexture;
			}
		}
	}

	public int getWidth() { return width; }
	public int getHeight() { return height; }
}
