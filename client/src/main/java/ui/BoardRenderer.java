package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import static ui.EscapeSequences.*;

public class BoardRenderer {

    public static String render(ChessBoard board, ChessGame.TeamColor perspective) {
        return render(board, perspective, null, null);
    }

    public static String render(ChessBoard board, ChessGame.TeamColor perspective, ChessPosition selected, Collection<ChessPosition> legalTargets) {

        boolean whiteView = perspective == ChessGame.TeamColor.WHITE;
        StringBuilder sb = new StringBuilder();

        Set<ChessPosition> targets = (legalTargets == null) ? new HashSet<>() : new HashSet<>(legalTargets);

        int[] rowOrder = whiteView ? new int[]{8, 7, 6, 5, 4, 3, 2, 1}
                : new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        int[] colOrder = whiteView ? new int[]{1, 2, 3, 4, 5, 6, 7, 8}
                : new int[]{8, 7, 6, 5, 4, 3, 2, 1};

        sb.append(columnHeader(colOrder));
        for (int row : rowOrder) {
            sb.append(SET_TEXT_COLOR_LIGHT_GREY).append(" ").append(row).append(" ").append(RESET_TEXT_COLOR);
            for (int col : colOrder) {
                ChessPosition square = new ChessPosition(row, col);
                boolean lightSquare = (row + col) % 2 != 0;

                if (square.equals(selected)) {
                    sb.append(SET_BG_COLOR_YELLOW);
                } else if (targets.contains(square)) {
                    sb.append(lightSquare ? SET_BG_COLOR_GREEN : SET_BG_COLOR_DARK_GREEN);
                } else {
                    sb.append(lightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_BLACK);
                }

                sb.append(glyph(board.getPiece(square)));
                sb.append(RESET_BG_COLOR);
            }
            sb.append(SET_TEXT_COLOR_LIGHT_GREY).append(" ").append(row).append(" ").append(RESET_TEXT_COLOR);
            sb.append("\n");
        }
        sb.append(columnHeader(colOrder));
        return sb.toString();
    }

    private static String columnHeader(int[] colOrder) {
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        for (int col : colOrder) {
            char letter = (char) ('a' + col - 1);
            sb.append(SET_TEXT_COLOR_LIGHT_GREY).append(" ").append(letter).append(" ").append(RESET_TEXT_COLOR);
        }
        sb.append("\n");
        return sb.toString();
    }

    private static String glyph(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }
        boolean white = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        String color = white ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE;
        String symbol = switch (piece.getPieceType()) {
            case KING   -> white ? WHITE_KING   : BLACK_KING;
            case QUEEN  -> white ? WHITE_QUEEN  : BLACK_QUEEN;
            case ROOK   -> white ? WHITE_ROOK   : BLACK_ROOK;
            case BISHOP -> white ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> white ? WHITE_KNIGHT : BLACK_KNIGHT;
            case PAWN   -> white ? WHITE_PAWN   : BLACK_PAWN;
        };
        return color + symbol;
    }
}
