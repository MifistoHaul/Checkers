package src.ru.vsu.cs.course1;


public class CheckersPiece {
    public enum Color { BLACK, WHITE }

    private Color color;
    private boolean isKing;

    public CheckersPiece(Color color) {
        this.color = color;
        this.isKing = false;
    }

    public Color getColor() {
        return color;
    }

    public boolean isKing() {
        return isKing;
    }

    public void makeKing() {
        this.isKing = true;
    }

    @Override
    public String toString() {
        return color == Color.BLACK ? "B" : "W";
    }

}
