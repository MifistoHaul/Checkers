package src.ru.vsu.cs.course1;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

public class CheckersBoard extends JPanel {
    private static final int BOARD_SIZE = 8;
    private CheckersPiece[][] board;
    private Point selectedPiece;
    private boolean isWhiteTurn;
    private List<Point> mandatoryJumps; // Обязательные взятия
    private boolean isJumpingSequence;
    private Point jumpingFrom;

    public CheckersBoard() {
        board = new CheckersPiece[BOARD_SIZE][BOARD_SIZE];
        selectedPiece = null;
        isWhiteTurn = true;
        mandatoryJumps = new ArrayList<>();
        isJumpingSequence = false;
        jumpingFrom = null;
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
        int cellSize = Math.min(getWidth(), getHeight()) / BOARD_SIZE; // квадрат.окно
        // после клика преобразуем корды пикс. в номер клетки
        int col = x / cellSize;
        int row = y / cellSize;

        if (col < 0 || col >= BOARD_SIZE || row < 0 || row >= BOARD_SIZE) { // кликнули за пределы окна - игнорим
            return;
        }

        if (!isJumpingSequence) { // обычный ход
            // Находим шашки которые обязаны бить
            mandatoryJumps = findMandatoryJumps();
            // если не выбрана шашка - пытаемся выбрать
            if (selectedPiece == null) {
                // Выбор шашки
                if (board[row][col] != null &&
                        ((isWhiteTurn && board[row][col].getColor() == CheckersPiece.Color.WHITE) ||
                                (!isWhiteTurn && board[row][col].getColor() == CheckersPiece.Color.BLACK))) {

                    // проверяем, что выбранная шашка может бить, если нет - нельзя выбрать
                    if (!mandatoryJumps.isEmpty()) {
                        boolean canThisPieceJump = false;
                        for (Point p : mandatoryJumps) {
                            if (p.y == row && p.x == col) {
                                canThisPieceJump = true;
                                break;
                            }
                        }
                        if (!canThisPieceJump) {
                            return; // Нельзя выбрать эту шашку, есть другие с обязательными взятиями
                        }
                    }

                    selectedPiece = new Point(col, row); //сохранили выбранную ш.
                    repaint();
                }
            } else { // шашка уже выбрана -> ходим, много ошибок в логике
                // Попытка хода
                if (isValidMove(selectedPiece.y, selectedPiece.x, row, col)) {
                    boolean wasJump = Math.abs(row - selectedPiece.y) >= 2; // сейчас неправильно работает,из-за движ.дамок
                    movePiece(selectedPiece.y, selectedPiece.x, row, col);

                    // Проверяем, был ли это взятие и может ли шашка продолжить бить
                    if (wasJump) {
                        List<Point> nextJumps = findJumpsFrom(row, col);
                        if (!nextJumps.isEmpty()) {
                            // Начинаем последовательность взятий
                            isJumpingSequence = true;
                            jumpingFrom = new Point(col, row);
                            selectedPiece = null;
                            repaint();
                        } else {
                            // Нет продолжения - ход завершен
                            isJumpingSequence = false;
                            jumpingFrom = null;
                            selectedPiece = null;
                            // Только теперь передаем ход противнику
                            isWhiteTurn = !isWhiteTurn;
                            repaint();
                        }
                    } else { // если прыжка не было
                        // Простой ход (не взятие)
                        selectedPiece = null;
                        isWhiteTurn = !isWhiteTurn;
                        repaint();
                    }
                } else {
                    // Если кликнули на другую свою шашку - выбираем её
                    if (board[row][col] != null &&
                            ((isWhiteTurn && board[row][col].getColor() == CheckersPiece.Color.WHITE) ||
                                    (!isWhiteTurn && board[row][col].getColor() == CheckersPiece.Color.BLACK))) {

                        // Проверяем обязательные взятия
                        if (!mandatoryJumps.isEmpty()) {
                            boolean canThisPieceJump = false;
                            for (Point p : mandatoryJumps) {
                                if (p.y == row && p.x == col) {
                                    canThisPieceJump = true;
                                    break;
                                }
                            }
                            if (!canThisPieceJump) {
                                return;
                            }
                        }

                        selectedPiece = new Point(col, row);
                        repaint();
                    } else {
                        selectedPiece = null;
                        repaint();
                    }
                }
            }
        } else {
            // Продолжение последовательности взятий
            // Обязательные взятия во время последовательности - только для текущей шашки
            mandatoryJumps = findMandatoryJumps();

            if (isValidMove(jumpingFrom.y, jumpingFrom.x, row, col)) { // ходим только начальной шашкой цепочки
                boolean wasJump = Math.abs(row - jumpingFrom.y) >= 2; // опять проблема в логике
                movePiece(jumpingFrom.y, jumpingFrom.x, row, col);

                // Проверяем, может ли эта же шашка продолжить бить
                if (wasJump) {
                    List<Point> nextJumps = findJumpsFrom(row, col);
                    if (!nextJumps.isEmpty()) {
                        // Продолжаем последовательность
                        jumpingFrom = new Point(col, row);
                        repaint();
                    } else {
                        // Цепочка взятий завершена - только теперь передаем ход
                        isJumpingSequence = false;
                        jumpingFrom = null;
                        selectedPiece = null;
                        isWhiteTurn = !isWhiteTurn;
                        repaint();
                    }
                } else {
                    // Это не было взятием, но мы в последовательности - ошибка
                    isJumpingSequence = false;
                    jumpingFrom = null;
                    selectedPiece = null;
                    isWhiteTurn = !isWhiteTurn;
                    repaint();
                }
            }
        }
    }

    private List<Point> findMandatoryJumps() { // находит все шашки что обязаны бить
        List<Point> jumps = new ArrayList<>();

        // Если идет последовательность взятий, проверяем только текущую шашку
        if (isJumpingSequence && jumpingFrom != null) {
            // Проверяем, может ли текущая шашка продолжать бить
            if (!findJumpsFrom(jumpingFrom.y, jumpingFrom.x).isEmpty()) {
                jumps.add(jumpingFrom);
            }
            return jumps;
        }

        // Если НЕ последовательность взятий, проверяем ВСЕ шашки текущего игрока
        CheckersPiece.Color currentColor = isWhiteTurn ?
                CheckersPiece.Color.WHITE : CheckersPiece.Color.BLACK;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] != null &&
                        board[row][col].getColor() == currentColor) {
                    if (!findJumpsFrom(row, col).isEmpty()) {
                        jumps.add(new Point(col, row));
                    }
                }
            }
        }

        return jumps;
    }

    private List<Point> findJumpsFrom(int row, int col) { // все взятия для конкретной шашки
        List<Point> jumps = new ArrayList<>();
        CheckersPiece piece = board[row][col];

        if (piece == null) return jumps;

        // Определяем направления для простых шашек
        int[][] directions;
        if (!piece.isKing()) {
            if (piece.getColor() == CheckersPiece.Color.WHITE) {
                directions = new int[][]{{-1, -1}, {-1, 1}}; // Белые ходят вверх
            } else {
                directions = new int[][]{{1, -1}, {1, 1}}; // Черные ходят вниз
            }
        } else {
            // Дамки могут ходить во всех направлениях
            directions = new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        }

        // Проверяем все возможные взятия
        for (int[] dir : directions) {
            // Проверяем ближайшие взятия (через одну клетку)
            int landRow = row + 2 * dir[0]; // куда шашка приземлится
            int landCol = col + 2 * dir[1];

            if (isValidJump(row, col, landRow, landCol, false)) {
                jumps.add(new Point(landCol, landRow));
            }

            // Для дамок проверяем дальние взятия
            if (piece.isKing()) {
                // Ищем первую вражескую шашку на пути рядом
                int currentRow = row + dir[0];
                int currentCol = col + dir[1];
                // дамка идет пока не дойдет до края или не пустой клетки
                while (isWithinBounds(currentRow, currentCol) && board[currentRow][currentCol] == null) {
                    currentRow += dir[0];
                    currentCol += dir[1];
                }

                // Если нашли вражескую шашку
                if (isWithinBounds(currentRow, currentCol) && // все еще внутри границ
                        board[currentRow][currentCol] != null && // в клетке есть шашка
                        board[currentRow][currentCol].getColor() != piece.getColor()) { // шашка другого цвета от дамки

                    // Проверяем клетку за вражеской шашкой
                    int afterEnemyRow = currentRow + dir[0];
                    int afterEnemyCol = currentCol + dir[1];

                    // Проверяем, что клетка за вражеской шашкой пуста и доступна
                    while (isWithinBounds(afterEnemyRow, afterEnemyCol) &&
                            board[afterEnemyRow][afterEnemyCol] == null) {

                        // Добавляем все возможные клетки для приземления
                        jumps.add(new Point(afterEnemyCol, afterEnemyRow));

                        // Проверяем следующую клетку
                        afterEnemyRow += dir[0];
                        afterEnemyCol += dir[1];
                    }
                }
            }
        }

        return jumps;
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Проверяем, что целевая клетка в пределах доски
        if (!isWithinBounds(toRow, toCol)) {
            return false;
        }

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

        CheckersPiece piece = board[fromRow][fromCol];
        if (piece == null) return false;

        // Во время последовательности взятий разрешаем ТОЛЬКО продолжение взятий
        if (isJumpingSequence) {
            // Можно ходить только той шашкой, которая бьет (jumpingFrom)
            if (jumpingFrom == null || jumpingFrom.y != fromRow || jumpingFrom.x != fromCol) {
                return false;
            }
            // Во время последовательности разрешаем ТОЛЬКО взятия (ход на 2+ клетки)
            if (rowDiff < 2) {
                return false;
            }
            // является ли ход валидным взятием
            return isValidJump(fromRow, fromCol, toRow, toCol, true);
        }

        // Если есть обязательные взятия - разрешаем только взятия
        if (!mandatoryJumps.isEmpty()) {
            // Проверяем, что выбранная шашка должна бить
            boolean mustJump = false;
            for (Point p : mandatoryJumps) {
                if (p.y == fromRow && p.x == fromCol) {
                    mustJump = true;
                    break;
                }
            }
            if (mustJump) { // Находится ли выбранная шашка в списке тех, что обязаны бить.
                return rowDiff >= 2 && isValidJump(fromRow, fromCol, toRow, toCol, false);
            }
            return false; // Если шашка должна бить, но ход не является взятием
        }

        // Обычный ход (не взятие)
        if (!piece.isKing()) {
            // Для обычных шашек - только вперед на 1 клетку
            if (piece.getColor() == CheckersPiece.Color.WHITE) {
                if (toRow > fromRow) return false; // Белые ходят только вверх
            } else {
                if (toRow < fromRow) return false; // Черные ходят только вниз
            }

            if (rowDiff != 1) { // ходим только на 1 клетку
                return false;
            }
        } else {
            // Для дамок - проверяем путь на пустоту
            if (rowDiff > 1 && !isPathClear(fromRow, fromCol, toRow, toCol)) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidJump(int fromRow, int fromCol, int toRow, int toCol, boolean isSequence) {
        // Проверяем границы
        if (!isWithinBounds(fromRow, fromCol) || !isWithinBounds(toRow, toCol)) {
            return false;
        }

        CheckersPiece piece = board[fromRow][fromCol];
        if (piece == null) return false;

        // Проверяем, что целевая клетка пуста
        if (board[toRow][toCol] != null) {
            return false;
        }

        // Проверяем, что ход по диагонали
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        if (rowDiff != colDiff || rowDiff < 2) {
            return false;
        }

        if (!piece.isKing()) {
            // Для обычных шашек - только на 2 клетки
            if (rowDiff != 2) {
                return false;
            }

            // Определяем середину прыжка
            int middleRow = (fromRow + toRow) / 2;
            int middleCol = (fromCol + toCol) / 2;

            // Проверяем, что в середине вражеская шашка
            if (!isWithinBounds(middleRow, middleCol) ||
                    board[middleRow][middleCol] == null ||
                    board[middleRow][middleCol].getColor() == piece.getColor()) {
                return false;
            }

            // Проверяем направление для обычных шашек
            if (piece.getColor() == CheckersPiece.Color.WHITE && toRow > fromRow) {
                return false; // Белые бьют только вверх
            } else if (piece.getColor() == CheckersPiece.Color.BLACK && toRow < fromRow) {
                return false; // Черные бьют только вниз
            }

            return true;
        } else {
            // Для дамок
            int rowDir = Integer.compare(toRow, fromRow); // Возвращает -1 если a < b, 0 если a == b, 1 если a > b
            int colDir = Integer.compare(toCol, fromCol);

            int currentRow = fromRow + rowDir;
            int currentCol = fromCol + colDir;
            boolean foundEnemy = false;

            while (currentRow != toRow && currentCol != toCol) { // Начинаем с клетки рядом с начальной и движемся к цели
                if (!isWithinBounds(currentRow, currentCol)) {
                    return false;
                }

                if (board[currentRow][currentCol] != null) {
                    if (foundEnemy) {
                        // Уже нашли врага, дальше не должно быть шашек
                        // Но для дамки это нормально, если дальше пусто до toRow/toCol
                        return false;
                    }

                    if (board[currentRow][currentCol].getColor() == piece.getColor()) {
                        return false; // Нельзя бить свои шашки
                    }
                    foundEnemy = true;
                }
                currentRow += rowDir;
                currentCol += colDir;
            }

            // Для дамки должен быть ровно один враг на пути
            return foundEnemy;
        }
    }

    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDir = Integer.compare(toRow, fromRow);
        int colDir = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + rowDir;
        int currentCol = fromCol + colDir;

        while (currentRow != toRow && currentCol != toCol) {
            if (!isWithinBounds(currentRow, currentCol) || board[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowDir;
            currentCol += colDir;
        }

        return true;
    }

    private boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = null;

        // Если это взятие - удаляем побитую шашку
        if (Math.abs(toRow - fromRow) == 2) { //  шашка переместилась на 2 клетки по диагонали - взятие
            int middleRow = (fromRow + toRow) / 2;
            int middleCol = (fromCol + toCol) / 2;
            board[middleRow][middleCol] = null;
        } else if (Math.abs(toRow - fromRow) > 2) { // сейчас считает каждый ход >2 - взятием - переделать
            // Для дамок
            int rowDir = Integer.compare(toRow, fromRow);
            int colDir = Integer.compare(toCol, fromCol);

            int currentRow = fromRow + rowDir;
            int currentCol = fromCol + colDir;

            while (currentRow != toRow && currentCol != toCol) { // проверяем все клетки от старта к цели
                if (board[currentRow][currentCol] != null) {
                    board[currentRow][currentCol] = null;
                    break;
                }
                currentRow += rowDir;
                currentCol += colDir;
            }
        }

        // Проверяем, стала ли шашка дамкой
        CheckersPiece piece = board[toRow][toCol];
        if (!piece.isKing()) {
            if (piece.getColor() == CheckersPiece.Color.WHITE && toRow == 0) { // белая сверху - значит дамка
                piece.makeKing();
            } else if (piece.getColor() == CheckersPiece.Color.BLACK && toRow == BOARD_SIZE - 1) { // черная снизу - дамка
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

        // Показываем чей ход
        g.setColor(Color.BLACK);
        g.drawString("Ход: " + (isWhiteTurn ? "Белые" : "Черные"), 10, getHeight() - 10);

        if (!mandatoryJumps.isEmpty() && !isJumpingSequence) {
            g.setColor(Color.RED);
            g.drawString("Обязательное взятие!", 100, getHeight() - 10);
        }

        if (isJumpingSequence) {
            g.setColor(Color.BLUE);
            g.drawString("Продолжайте бить!", 200, getHeight() - 10);
        }
    }
}