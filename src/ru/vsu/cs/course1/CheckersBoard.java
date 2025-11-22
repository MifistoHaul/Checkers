package src.ru.vsu.cs.course1;

import java.awt.*;
import javax.swing.*;

public class CheckersBoard extends JPanel {
    private static final int BOARD_SIZE = 8;
    private CheckersPiece[][] board;
    private Point selectedPiece;

    public CheckersBoard() {
        board = new CheckersPiece[BOARD_SIZE][BOARD_SIZE];
        selectedPiece = null;
        initializeBoard();

        this.setPreferredSize(new Dimension(400, 400));
    }

    private void initializeBoard() {
        // Расставляем черные шашки
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    board[row][col] = new CheckersPiece(CheckersPiece.Color.BLACK);
                }
            }
        }

        // Расставляем белые шашки
        for (int row = 5; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    board[row][col] = new CheckersPiece(CheckersPiece.Color.WHITE);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cellSize = Math.min(getWidth(), getHeight()) / BOARD_SIZE;

        // Рисуем доску
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if ((row + col) % 2 == 0) {
                    g.setColor(Color.LIGHT_GRAY);
                } else {
                    g.setColor(Color.DARK_GRAY);
                }
                g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);

                // Рисуем выделение выбранной шашки
                if (selectedPiece != null && selectedPiece.y == row && selectedPiece.x == col) {
                    g.setColor(Color.YELLOW);
                    g.drawRect(col * cellSize, row * cellSize, cellSize, cellSize);
                    g.drawRect(col * cellSize + 1, row * cellSize + 1, cellSize - 2, cellSize - 2);
                }

                // Рисуем шашки
                if (board[row][col] != null) {
                    CheckersPiece piece = board[row][col];
                    if (piece.getColor() == CheckersPiece.Color.BLACK) {
                        g.setColor(Color.BLACK);
                    } else {
                        g.setColor(Color.WHITE);
                    }
                    g.fillOval(col * cellSize + 5, row * cellSize + 5, cellSize - 10, cellSize - 10);

                    // Обводка для шашек
                    g.setColor(piece.getColor() == CheckersPiece.Color.BLACK ? Color.WHITE : Color.BLACK);
                    g.drawOval(col * cellSize + 5, row * cellSize + 5, cellSize - 10, cellSize - 10);

                }
            }
        }
    }
}
