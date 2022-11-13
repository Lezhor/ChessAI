package program;

import program.guis.Gui;
import program.players.*;

public class Game {

    /**
     * The chess-board is being saved in here.
     * Its length always is 64. The fields are numbered from left to right and top to down.
     */
    private int[] board;

    /**
     * First Player is stored in here.
     */
    private final Player whitePlayer;

    /**
     * Second Player is stored in here.
     */
    private final Player blackPlayer;

    /**
     * The GUI used in this game. (e.g. it can be a Terminal-UI)
     */
    public Gui gui;

    /**
     * This PGNWriter saves the game into a pgn-file.
     */
    public PGNWriter pgnWriter;

    /**
     * Saves which of the two players is on the bottom of the board.
     * Mostly it's the white player, except black is the Human-Player and white is not.
     */


    /**
     * Initializes the game.
     *
     * @param whitePlayer needs to be given an Object of Type 'Player'. It can be Human or AI.
     * @param blackPlayer needs to be given an Object of Type 'Player'. It can be Human or AI.
     * @param gui         The GUI where the Board is printed to and where the Human-Input is being got from.
     */
    public Game(Player whitePlayer, Player blackPlayer, Gui gui) {
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.gui = gui;

        initBoard();
        //initPos3();
        //initPos4();
        pgnWriter = new PGNWriter(blackPlayer.PGN_NAME, whitePlayer.PGN_NAME);
        gui.printBoard(board);
        gameLoop();
    }

    /**
     * The Main Loop, which is being called from the Constructor.
     * It gets moves from the two players and Updates the board. Checks if the gme is finished.
     */
    private void gameLoop() {
        int player = ChessRules.PLAYER_WHITE;
        boolean gameOver = false;
        while (!gameOver) {
            ChessRules.makeMove(board, player == ChessRules.PLAYER_WHITE ? whitePlayer.decideOnMove(board) : blackPlayer.decideOnMove(board));
            gui.printBoard(board);
            player = player ^ ChessRules.MASK_PLAYER;
            if (ChessRules.noLegalMovesLeft(board, player) || ChessRules.countPieces(board) <= 2) {
                gameOver = true;
            }
        }
        if (ChessRules.playerInCheck(board, player)) {
            gui.printWinner(player ^ ChessRules.MASK_PLAYER);
        } else {
            gui.printStalemate();
        }
    }


    // ---------------- GAME-INIT -----------------------

    /**
     * Initializes the board-Array and sets the Starting Positions of every piece by calling initUpperRowPieces & initBottomRowPieces.
     */
    private void initBoard() {
        board = new int[64];
        //setBottomPlayer(ChessRules.PLAYER_BLACK);
        initBottomRowPieces();
        initUpperRowPieces();
    }

    private void initPos4() {
        board = new int[64];
        board[4] = ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_BLACK | ChessRules.PIECE_KING;
        board[1] = ChessRules.MASK_SET_FIELD | ChessRules.MASK_HAS_MOVED | ChessRules.PLAYER_BLACK | ChessRules.PIECE_KNIGHT;
        board[6] = ChessRules.MASK_SET_FIELD | ChessRules.MASK_HAS_MOVED | ChessRules.PLAYER_BLACK | ChessRules.PIECE_KNIGHT;
        board[60] = ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_KING;
        for (int i = 0; i < 8; i++) {
            board[55 - i] = ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_PAWN;
        }
    }

    private void initPos3() {
        board = new int[64];
        board[9] = ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_PAWN;
        board[17] = ChessRules.MASK_SET_FIELD | ChessRules.MASK_HAS_MOVED | ChessRules.PLAYER_BLACK | ChessRules.PIECE_PAWN;
        board[30] = ChessRules.MASK_SET_FIELD | ChessRules.MASK_HAS_MOVED | ChessRules.PLAYER_WHITE | ChessRules.PIECE_QUEEN;
        board[47] = ChessRules.MASK_SET_FIELD | ChessRules.MASK_HAS_MOVED | ChessRules.PLAYER_BLACK | ChessRules.PIECE_KING;
        board[52] = ChessRules.MASK_SET_FIELD | ChessRules.MASK_HAS_MOVED | ChessRules.PLAYER_BLACK | ChessRules.PIECE_QUEEN;
        board[53] = ChessRules.MASK_SET_FIELD | ChessRules.MASK_HAS_MOVED | ChessRules.PLAYER_WHITE | ChessRules.PIECE_KING;
    }

    /**
     * Places Pieces of 'player' on the Upper half of the board
     */
    private void initUpperRowPieces() {
        int pawn = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_BLACK | ChessRules.PIECE_PAWN);
        int rook = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_BLACK | ChessRules.PIECE_ROOK);
        int knight = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_BLACK | ChessRules.PIECE_KNIGHT);
        int bishop = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_BLACK | ChessRules.PIECE_BISHOP);
        int queen = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_BLACK | ChessRules.PIECE_QUEEN);
        int king = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_BLACK | ChessRules.PIECE_KING);
        board[0] = rook;
        board[1] = knight;
        board[2] = bishop;
        board[3] = queen;
        board[4] = king;
        board[5] = bishop;
        board[6] = knight;
        board[7] = rook;
        for (int i = 8; i < 16; i++) {
            board[i] = pawn;
        }
    }

    /**
     * Places Pieces of 'player' on the Upper half of the board
     */
    private void initBottomRowPieces() {
        int pawn = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_PAWN);
        int rook = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_ROOK);
        int knight = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_KNIGHT);
        int bishop = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_BISHOP);
        int queen = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_QUEEN);
        int king = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_KING);
        board[56] = rook;
        board[57] = knight;
        board[58] = bishop;
        board[59] = queen;
        board[60] = king;
        board[61] = bishop;
        board[62] = knight;
        board[63] = rook;
        for (int i = 48; i < 56; i++) {
            board[i] = pawn;
        }
    }
}
