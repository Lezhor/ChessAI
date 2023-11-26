package program.guis;

import program.ChessRules;

public class NoGui extends Gui{
    public NoGui() {
        super(ChessRules.PLAYER_WHITE);
    }

    @Override
    public void printBoard(int[] board, int lastMove) {
    }

    @Override
    public void printWinner(int player) {

    }

    @Override
    public void printStalemate() {

    }

    @Override
    public int getInputMove(int[] board, int player) {
        return 0;
    }
}
