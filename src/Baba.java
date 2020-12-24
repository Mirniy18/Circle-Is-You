import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Baba {
	public static void main(String[] args) {
		JFrame frame = new JFrame("Baba");

		Map map = new Map();

		JPanel panel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				map.draw((Graphics2D) g);
			}
		};

		panel.setBackground(Color.BLACK);
		panel.setPreferredSize(new Dimension(Map.TILE_SIZE * map.getWidth(), Map.TILE_SIZE * map.getHeight()));

		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_W: map.move(Direction.UP); break;
					case KeyEvent.VK_A: map.move(Direction.LEFT); break;
					case KeyEvent.VK_S: map.move(Direction.DOWN); break;
					case KeyEvent.VK_D: map.move(Direction.RIGHT); break;
					case KeyEvent.VK_Z: map.undo(); break;
					case KeyEvent.VK_R: if (e.isShiftDown()) map.reset(); break;
					case KeyEvent.VK_ESCAPE: System.exit(0); break;
					default: return;
				}

				frame.repaint();
			}
		});
	}
}
