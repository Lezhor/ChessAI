package java;

import java.guis.Gui;
import java.players.*;

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
    public int bottomPlayer;


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
            if (ChessRules.noLegalMovesLeft(board, player)) {
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
     * (according to bottomPlayer)
     */
    private void initBoard() {
        board = new int[64];
        if (!(HumanPlayer.class.isAssignableFrom(whitePlayer.getClass()))) {
            setBottomPlayer((HumanPlayer.class.isAssignableFrom(blackPlayer.getClass())) ? ChessRules.PLAYER_BLACK : ChessRules.PLAYER_WHITE);
        } else {
            setBottomPlayer(ChessRules.PLAYER_WHITE);
        }
        //setBottomPlayer(ChessRules.PLAYER_BLACK);
        initBottomRowPieces(bottomPlayer);
        initUpperRowPieces(bottomPlayer ^ ChessRules.MASK_PLAYER);
    }

    private void initPos3() {
        board = new int[64];
        if (!(HumanPlayer.class.isAssignableFrom(whitePlayer.getClass()))) {
            setBottomPlayer((HumanPlayer.class.isAssignableFrom(blackPlayer.getClass())) ? ChessRules.PLAYER_BLACK : ChessRules.PLAYER_WHITE);
        } else {
            setBottomPlayer(ChessRules.PLAYER_WHITE);
        }
        setBottomPlayer(ChessRules.PLAYER_BLACK);
        board[9] = ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_PAWN;
        board[17] = ChessRules.MASK_SET_FIELD | ChessRules.MASK_HAS_MOVED | ChessRules.PLAYER_BLACK | ChessRules.PIECE_PAWN;
        board[30] = ChessRules.MASK_SET_FIELD | ChessRules.MASK_HAS_MOVED | ChessRules.PLAYER_WHITE | ChessRules.PIECE_QUEEN;
        board[47] = ChessRules.MASK_SET_FIELD | ChessRules.MASK_HAS_MOVED | ChessRules.PLAYER_BLACK | ChessRules.PIECE_KING;
        board[52] = ChessRules.MASK_SET_FIELD | ChessRules.MASK_HAS_MOVED | ChessRules.PLAYER_BLACK | ChessRules.PIECE_QUEEN;
        board[53] = ChessRules.MASK_SET_FIELD | ChessRules.MASK_HAS_MOVED | ChessRules.PLAYER_WHITE | ChessRules.PIECE_KING;
    }

    /**
     * Places Pieces of 'player' on the Upper half of the board
     *
     * @param player which starts on the top half
     */
    private void initUpperRowPieces(int player) {
        int pawn = (ChessRules.MASK_SET_FIELD | (player | ChessRules.PIECE_PAWN));
        int rook = (ChessRules.MASK_SET_FIELD | (player | ChessRules.PIECE_ROOK));
        int knight = (ChessRules.MASK_SET_FIELD | (player | ChessRules.PIECE_KNIGHT));
        int bishop = (ChessRules.MASK_SET_FIELD | (player | ChessRules.PIECE_BISHOP));
        int queen = (ChessRules.MASK_SET_FIELD | (player | ChessRules.PIECE_QUEEN));
        int king = (ChessRules.MASK_SET_FIELD | (player | ChessRules.PIECE_KING));
        board[0] = rook;
        board[1] = knight;
        board[2] = bishop;
        board[3] = bottomPlayer != ChessRules.PLAYER_WHITE ? king : queen;
        board[4] = bottomPlayer != ChessRules.PLAYER_WHITE ? queen : king;
        board[5] = bishop;
        board[6] = knight;
        board[7] = rook;
        for (int i = 8; i < 16; i++) {
            board[i] = pawn;
        }
    }

    /**
     * Places Pieces of 'player' on the Upper half of the board
     *
     * @param player which starts on the bottom half
     */
    private void initBottomRowPieces(int player) {
        int pawn = (ChessRules.MASK_SET_FIELD | player | ChessRules.PIECE_PAWN);
        int rook = (ChessRules.MASK_SET_FIELD | (player | ChessRules.PIECE_ROOK));
        int knight = (ChessRules.MASK_SET_FIELD | (player | ChessRules.PIECE_KNIGHT));
        int bishop = (ChessRules.MASK_SET_FIELD | (player | ChessRules.PIECE_BISHOP));
        int queen = (ChessRules.MASK_SET_FIELD | (player | ChessRules.PIECE_QUEEN));
        int king = (ChessRules.MASK_SET_FIELD | (player | ChessRules.PIECE_KING));
        board[56] = rook;
        board[57] = knight;
        board[58] = bishop;
        board[59] = bottomPlayer == ChessRules.PLAYER_WHITE ? queen : king;
        board[60] = bottomPlayer == ChessRules.PLAYER_WHITE ? king : queen;
        board[61] = bishop;
        board[62] = knight;
        board[63] = rook;
        for (int i = 48; i < 56; i++) {
            board[i] = pawn;
        }
    }

    /**
     * Sets the Global Var 'bottomPlayer' - Not only for this class, but also in the ChessRules-Class
     *
     * @param bottomPlayer Sets 'bottomPlayer' to this parameter
     */
    public void setBottomPlayer(int bottomPlayer) {
        this.bottomPlayer = bottomPlayer;
        ChessRules.setBottomPlayer(bottomPlayer);
    }
}
