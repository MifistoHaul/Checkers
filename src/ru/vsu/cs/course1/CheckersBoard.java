package src.ru.vsu.cs.course1;

import java.awt.*;
import javax.swing.*;

public class CheckersBoard extends JPanel {
    private static final int BOARD_SIZE = 8;
    private CheckersPiece[][] board;
    private Point selectedPiece;
    private boolean isWhiteTurn;

    public CheckersBoard() {
        board = new CheckersPiece[BOARD_SIZE][BOARD_SIZE];
        selectedPiece = null;
        isWhiteTurn = true;
        initializeBoard();

        this.setPreferredSize(new Dimension(400, 400));
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
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

    private void handleClick(int x, int y) {
        int cellSize = Math.min(getWidth(), getHeight()) / BOARD_SIZE;
        int col = x / cellSize;
        int row = y / cellSize;

        if (col < 0 || col >= BOARD_SIZE || row < 0 || row >= BOARD_SIZE) {
            return;
        }

        if (selectedPiece == null) {
            // Выбор шашки
            if (board[row][col] != null &&
                    ((isWhiteTurn && board[row][col].getColor() == CheckersPiece.Color.WHITE) ||
                            (!isWhiteTurn && board[row][col].getColor() == CheckersPiece.Color.BLACK))) {
                selectedPiece = new Point(col, row);
                repaint();
            }
        } else {
            // Попытка хода
            if (isValidMove(selectedPiece.y, selectedPiece.x, row, col)) {
                movePiece(selectedPiece.y, selectedPiece.x, row, col);
                selectedPiece = null;
                isWhiteTurn = !isWhiteTurn;
                repaint();
            } else {
                // Если кликнули на другую свою шашку - выбираем её
                if (board[row][col] != null &&
                        ((isWhiteTurn && board[row][col].getColor() == CheckersPiece.Color.WHITE) ||
                                (!isWhiteTurn && board[row][col].getColor() == CheckersPiece.Color.BLACK))) {
                    selectedPiece = new Point(col, row);
                    repaint();
                } else {
                    selectedPiece = null;
                    repaint();
                }
            }
        }
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Проверяем, что целевая клетка пуста
        if (board[toRow][toCol] != null) {
            return false;
        }

        // Проверяем, что ход по диагонали
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        if (rowDiff != colDiff || rowDiff == 0) {
            return false;
        }

        // Для обычных шашек - только вперед на 1 клетку
        CheckersPiece piece = board[fromRow][fromCol];
        if (!piece.isKing()) {
            if (piece.getColor() == CheckersPiece.Color.WHITE) {
                if (toRow > fromRow) return false; // Белые ходят только вверх
            } else {
                if (toRow < fromRow) return false; // Черные ходят только вниз
            }

            if (rowDiff != 1) {
                return false;
            }
        } else {
            // Для дамок - любое количество клеток по диагонали
            // Но пока 1 клетка
            if (rowDiff != 1) {
                return false;
            }
        }

        return true;
    }

    private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = null;

        // Проверяем, стала ли шашка дамкой
        CheckersPiece piece = board[toRow][toCol];
        if (!piece.isKing()) {
            if (piece.getColor() == CheckersPiece.Color.WHITE && toRow == 0) {
                piece.makeKing();
            } else if (piece.getColor() == CheckersPiece.Color.BLACK && toRow == BOARD_SIZE - 1) {
                piece.makeKing();
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

                    // Отмечаем дамки
                    if (piece.isKing()) {
                        g.setColor(Color.RED);
                        g.drawString("K", col * cellSize + cellSize/2 - 3, row * cellSize + cellSize/2 + 3);
                    }
                }
            }
        }
    }
}
