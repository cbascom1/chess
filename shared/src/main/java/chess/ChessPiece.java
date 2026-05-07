package chess;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (type) {
            case KING -> kingMoves(board, myPosition);
            case QUEEN  -> queenMoves(board, myPosition);
            case BISHOP -> bishopMoves(board, myPosition);
            case KNIGHT -> knightMoves(board, myPosition);
            case ROOK   -> rookMoves(board, myPosition);
            case PAWN   -> pawnMoves(board, myPosition);
        };
    }
    private static final int[][] BISHOP_DIRECTIONS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final int[][] ROOK_DIRECTIONS   = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] QUEEN_DIRECTIONS  = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
    private static final int[][] KING_DIRECTIONS   = QUEEN_DIRECTIONS;
    private static final int[][] KNIGHT_DIRECTIONS = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
    };


    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition pos) {
        return slidingMoves(board, pos, BISHOP_DIRECTIONS);
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition pos) {
        return slidingMoves(board, pos, ROOK_DIRECTIONS);
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition pos) {
        return slidingMoves(board, pos, QUEEN_DIRECTIONS);
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition pos) {
        return steppingMoves(board, pos, KING_DIRECTIONS);
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition pos) {
        return steppingMoves(board, pos, KNIGHT_DIRECTIONS);
    }

    private Collection<ChessMove> slidingMoves(ChessBoard board, ChessPosition pos, int[][] directions) {

    }

    private Collection<ChessMove> steppingMoves(ChessBoard board, ChessPosition pos, int[][] directions) {

    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition pos) {

    }
}
