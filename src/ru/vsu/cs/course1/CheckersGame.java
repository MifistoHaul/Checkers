package src.ru.vsu.cs.course1;

import javax.swing.*;

public class CheckersGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Шашки");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            CheckersBoard board = new CheckersBoard();
            frame.add(board);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
