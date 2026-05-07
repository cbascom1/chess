package chess;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

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
        // make a list for possible moves
        List<ChessMove> moves = new ArrayList<>();
        // check each direction
        for (int[] direction : directions) {
            int row = pos.getRow() + direction[0];
            int col = pos.getColumn() + direction[1];
            // continue in those directions until stopped
            while (inBounds(row, col)) {
                ChessPosition next = new ChessPosition(row, col);
                ChessPiece occupant = board.getPiece(next);
                // check the space
                if (occupant == null) {
                    moves.add(new ChessMove(pos, next, null));
                } else {
                    if (occupant.getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(pos, next, null));
                    }
                    break;
                }
                row += direction[0];
                col += direction[1];
            }
        }
        return moves;
    }

    private Collection<ChessMove> steppingMoves(ChessBoard board, ChessPosition pos, int[][] directions) {
        // make a list for possible moves
        List<ChessMove> moves = new ArrayList<>();
        // check each direction
        for (int[] direction : directions) {
            int row = pos.getRow() + direction[0];
            int col = pos.getColumn() + direction[1];
            if (!inBounds(row, col)) continue;
            ChessPosition next = new ChessPosition(row, col);
            ChessPiece occupant = board.getPiece(next);
            if (occupant == null || occupant.getTeamColor() != pieceColor) {
                moves.add(new ChessMove(pos, next, null));
            }
        }
        return moves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition pos) {
        List<ChessMove> moves = new ArrayList<>();
        boolean isWhite = pieceColor == ChessGame.TeamColor.WHITE;
        int direction = isWhite ? 1 : -1;
        int startRow = isWhite ? 2 : 7;

        int row = pos.getRow();
        int col = pos.getColumn();
        int forwardRow = row + direction;
        boolean isPromotion = forwardRow == (isWhite ? 8 : 1);

        ChessPosition oneStep = new ChessPosition(forwardRow, col);
        if (board.getPiece(oneStep) == null) {
            addPawnMove(moves, pos, oneStep, isPromotion);

            if (row == startRow) {
                ChessPosition twoStep = new ChessPosition(row + 2 * direction, col);
                if (board.getPiece(twoStep) == null) {
                    moves.add(new ChessMove(pos, twoStep, null));
                }
            }
        }

        for (int dCol : new int[]{-1, 1}) {
            int captureCol = col + dCol;
            if (captureCol < 1 || captureCol > 8) continue;
            ChessPosition target = new ChessPosition(forwardRow, captureCol);
            ChessPiece occupant = board.getPiece(target);
            if (occupant != null && occupant.getTeamColor() != pieceColor) {
                addPawnMove(moves, pos, target, isPromotion);
            }
        }

        return moves;
    }

    private static boolean inBounds(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private void addPawnMove(List<ChessMove> moves, ChessPosition from, ChessPosition to, boolean isPromotion) {
        if (isPromotion) {
            for (PieceType promotion : new PieceType[]{PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT}) {
                moves.add(new ChessMove(from, to, promotion));
            }
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }
}

