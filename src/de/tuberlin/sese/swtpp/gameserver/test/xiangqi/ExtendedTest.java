package de.tuberlin.sese.swtpp.gameserver.test.xiangqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import de.tuberlin.sese.swtpp.gameserver.control.GameController;
import de.tuberlin.sese.swtpp.gameserver.model.xiangqi.XiangqiGame;
import de.tuberlin.sese.swtpp.gameserver.model.Player;
import de.tuberlin.sese.swtpp.gameserver.model.User;

public class ExtendedTest {


	User user1 = new User("Alice", "alice");
	User user2 = new User("Bob", "bob");
	
	Player redPlayer = null;
	Player blackPlayer = null;
	XiangqiGame game = null;
	GameController controller;
	String startFEN = "rheagaehr/9/1c5c1/s1s1s1s1s/9/9/S1S1S1S1S/1C5C1/9/RHEAGAEHR";
	
	@Before
	public void setUp() throws Exception {
		controller = GameController.getInstance();
		controller.clear();
		
		int gameID = controller.startGame(user1, "", "xiangqi");
		
		game =  (XiangqiGame) controller.getGame(gameID);
		redPlayer = game.getPlayer(user1);

	}
	
	public void startGame() {
		controller.joinGame(user2, "xiangqi");		
		blackPlayer = game.getPlayer(user2);
	}
	
	public void startGame(String initialBoard, boolean redNext) {
		startGame();
		
		game.setBoard(initialBoard);
		game.setNextPlayer(redNext? redPlayer:blackPlayer);
	}
	
	public void assertMove(String move, boolean red, boolean expectedResult) {
		if (red)
			assertEquals(expectedResult, game.tryMove(move, redPlayer));
		else 
			assertEquals(expectedResult,game.tryMove(move, blackPlayer));
	}
	
	public void assertGameState(String expectedBoard, boolean redNext, boolean finished, boolean redWon) {
		assertEquals(expectedBoard,game.getBoard());
		assertEquals(finished, game.isFinished());

		if (!game.isFinished()) {
			assertEquals(redNext, game.getNextPlayer() == redPlayer);
		} else {
			assertEquals(redWon, redPlayer.isWinner());
			assertEquals(!redWon, blackPlayer.isWinner());
		}
	}
	
    public void printBoard(char[][] boardArr) {
    	for(int i=0;i<boardArr.length;i++) {
    		System.out.print(9-i + " ");
    		for(int j=0;j<boardArr[i].length;j++) {
    			System.out.print((boardArr[i][j] == ' ' ? "." : boardArr[i][j])+" ");
    		}
    		System.out.println("");
    	}
    	System.out.println("* a b c d e f g h i ");
    }
    
    public void printBoard(String board) {
    	printBoard(game.FENtoBoard(board));
    }
    
    public void printBoard() {
    	printBoard(game.getBoard());
    }
    
    public void setFigur(int[] pos, char figur, char[][] board) {
    	board[pos[0]][pos[1]] = figur;
    	game.setBoard(game.boardToFEN(board));
    }
    
    public void setFigur(int[] pos, char figur) {
    	char[][] board = game.FENtoBoard(game.getBoard());
    	setFigur(pos,figur,board);
    }
    
    // board where it should be a checkmate, and the player got checkmated
    public void checkMater(String board, Player player) {
    	startGame(board,true);
    	printBoard();
    	boolean isCheck = game.isCheck(player, game.FENtoBoard(board));
    	boolean isCheckMate = game.isCheckmate(player, game.FENtoBoard(board));
    	System.out.println("isCheck: " + isCheck);
    	System.out.println("isCheckmate: " + isCheckMate);
    	ArrayList<String> moves = game.validMoves(player, game.FENtoBoard(board));
        for(String move:moves) System.out.println(move);
        assertTrue(isCheck);
        assertTrue(isCheckMate);
        boolean isRedChecked = player == redPlayer;
        assertGameState(board, !isRedChecked, true, !isRedChecked);
    }

	/*******************************************
	 * !!!!!!!!! To be implemented !!!!!!!!!!!!
	 *******************************************/
    
    @Test
    public void testGetTranslatedMove() {
    	startGame(startFEN, true);
    	// check translation from moveString to boardArr's zeile and spalte number
        int[] move = game.getTranslatedMove("a0-a2");
        int[] expectedMove = {9,0,7,0};
        assertTrue(Arrays.equals(move,expectedMove));
    }
    
    @Test
    public void testBoardConverter() {
    	startGame(startFEN, true);
    	String board = startFEN;
    	char[][] boardArr = game.FENtoBoard(board);
    	String newBoard = game.boardToFEN(boardArr);
    	assertEquals(board, newBoard);
    }
    
    @Test
    public void testDoMove() {
    	// test basic movement(unchecked)
    	startGame(startFEN, true);
    	// ignore this text -> redNext is true because doMove only applies the move to the board and not add it to history or set the next player
//    	assertEquals(game.doMove("a0-a2", redPlayer),"rheagaehr/9/1c5c1/s1s1s1s1s/9/9/S1S1S1S1S/RC5C1/9/1HEAGAEHR");
//    	assertGameState("rheagaehr/9/1c5c1/s1s1s1s1s/9/9/S1S1S1S1S/RC5C1/9/1HEAGAEHR", true, false, false);
//    	printBoard(game.getBoard());
    }
    
    @Test
    public void testMoveInBoard() {
    	startGame(startFEN, true);
    	// should be true
    	assertTrue(game.moveInBoard("a0-a2"));
    	// z is invalid
    	assertFalse(game.moveInBoard("z9-a2"));
    	// invalid zeile(null)
    	assertFalse(game.moveInBoard("a-a2"));
    	// invalid zeile(10)
    	assertFalse(game.moveInBoard("a10-a2"));
    	// invalid spalte(A)
//    	assertFalse(game.moveInBoard("A1-a2"));
    }
    
    @Test
    public void testStartZielIsValid() {
    	startGame(startFEN, true);
    	char[][] board = game.FENtoBoard(game.getBoard());
    	// in this case, the current player is redPlayer
    	// a valid move
    	assertTrue(game.startZielIsValid(board, game.getTranslatedMove("a0-a2"), redPlayer));
    	// blackPlayer trying to move redPlayer's piece in
    	assertFalse(game.startZielIsValid(board, game.getTranslatedMove("a0-a2"), blackPlayer));
    	// redPlayer trying to kill his own piece
    	assertFalse(game.startZielIsValid(board, game.getTranslatedMove("a0-a3"), blackPlayer));
    	// redPlayer trying to move to same location
    	assertFalse(game.startZielIsValid(board, game.getTranslatedMove("a0-a0"), blackPlayer));
    }
    
//    @Test
//    public void testtesttest() {
//        startGame("7R1/5g3/C2RH4/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2",false);
//        System.out.println("--------------------");
//        System.out.println(game.isCheck(blackPlayer,game.FENtoBoard("5g1R1/3R5/C3H4/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2")));
//        assertMove("d7-d8", true, true);
//        System.out.println(game.isCheck(blackPlayer,game.FENtoBoard("5g1R1/3R5/C3H4/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2")));
//        System.out.println("--------------------");
//        ArrayList<String> moves = game.validMoves(blackPlayer);
//        for(String move:moves) {
//        	System.out.println(move);
//        	String newBoard = game.doMove(move, blackPlayer);
//        	printBoard(newBoard);
//        	System.out.println(game.isCheck(blackPlayer,game.FENtoBoard(newBoard)));
//        }
//        ArrayList<String> moves2 = game.validMoves(redPlayer);
//        for(String move:moves2) {
//        	System.out.println(move);
//        }
//    }
    
//    @Test
//    public void testtesttest() {
//        startGame("2R6/3R3g1/R8/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2",true);
//        System.out.println("--------------------");
////        System.out.println("isCheck: " + game.isCheck(blackPlayer,game.FENtoBoard("5g1R1/3R5/C3H4/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2")));
////        assertMove("d7-d8", true, true);
////        System.out.println(game.isCheck(blackPlayer));
////        System.out.println(game.isCheck(blackPlayer,game.FENtoBoard("5g1R1/3R5/C3H4/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2")));
//        System.out.println("--------------------");
//        ArrayList<String> moves2 = game.validMoves(blackPlayer);
//        for(String move:moves2) System.out.println(move);
//        System.out.println("--------------------");
//        System.out.println(game.checkMove("f8-f7", blackPlayer) + " " + game.doMove("f8-f7", blackPlayer));
//        printBoard("2R6/3R3g1/R8/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2");
//        System.out.println(game.isCheck(blackPlayer, game.FENtoBoard("2R6/3R3g1/R8/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2")));
//        System.out.println(game.isCheckmate(blackPlayer));
////        ArrayList<String> moves1 = game.validMoves(redPlayer);
////        for(String move:moves1) System.out.println(move);
//    }
    
//    @Test
//    public void testingg() {
////    	String boardStr = "8R/9/R3g4/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2";
////    	String boardStr = "5g1R1/3R5/C3H4/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2";
//    	String boardStr = "2R6/3R3g1/R8/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2";
////    	String boardStr = "R2g5/R8/R8/9/9/9/9/9/9/4G4";
//    	startGame(boardStr,true);
//    	printBoard();
//    	System.out.println(game.isCheck(blackPlayer, game.FENtoBoard(boardStr)));
//    	System.out.println(game.isCheckmate(blackPlayer, game.FENtoBoard(boardStr)));
//    	ArrayList<String> moves2 = game.validMoves(blackPlayer, game.FENtoBoard(boardStr));
//        for(String move:moves2) System.out.println(move);
//    	
//    }
    
//    @Test
//    public void testMultipleCheckMate() {
////    	String board1 = "8R/9/R3g4/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2";
//    	String board2 = "5g1R1/3R5/C3H4/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2";
//    	String board3 = "2R6/3R3g1/R8/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2";
//    	String board4 = "R2g5/R8/R8/9/9/9/9/9/9/4G4";
////    	checkMater(board1, blackPlayer);
//    	checkMater(board2, blackPlayer);
//    	checkMater(board3, blackPlayer);
//    	checkMater(board4, blackPlayer);
//    }
    
    @Test
    public void testGame1() {
    	startGame(startFEN, true);
    	assertMove("d0-e1", true, true);
    	assertMove("h7-h3", false, true);
    	assertMove("h2-h9", true, true);
    	assertMove("b9-c7", false, true);
    	assertMove("h9-f9", true, true);
    	assertMove("g9-e7", false, true);
    	assertMove("f9-d9", true, true);
    	assertMove("h3-e3", false, true);
    	assertMove("e0-d0", true, true);
    	assertMove("i9-i8", false, true);
    	assertMove("d9-a9", true, true);
    	assertMove("e9-e8", false, true);
    	assertMove("h0-g2", true, true);
    	assertMove("e3-f3", false, true);
    	assertMove("a9-a7", true, true);
    	assertMove("b7-b3", false, true);
    	assertMove("a7-e7", true, true);
    	assertMove("i8-i9", false, true);
    	assertMove("b0-c2", true, true);
    	assertMove("f3-f1", false, true);
    	assertMove("g2-e3", true, true);
    	assertMove("f1-h1", false, true);
    	assertMove("g3-g4", true, true);
    	assertMove("i9-d9", false, true);
    	assertMove("d0-e0", true, true);
    	assertMove("d9-d3", false, true);
    	assertMove("i0-i1", true, true);
    	assertMove("h1-h5", false, true);
    	assertMove("i1-h1", true, true);
    	assertMove("h5-a5", false, true);
    	assertMove("h1-h8", true, true);
    	assertMove("e8-e9", false, true);
    	assertMove("g4-g5", true, true);
    	assertMove("b3-b9", false, true);
    	assertMove("b2-b6", true, true);
    	assertMove("c9-a7", false, true);
    	assertMove("b6-e6", true, true);
    	assertMove("e9-f9", false, true);
    	assertMove("e7-a7", true, true);
    	assertMove("c7-e6", false, true);
    	assertMove("h8-h9", true, true);
    	assertMove("f9-f8", false, true);
    	assertMove("c0-a2", true, true);
    	assertMove("b9-b8", false, true);
    	assertMove("a0-b0", true, true);
    	assertMove("b8-c8", false, true);
    	assertMove("b0-b8", true, true);
    	assertMove("d3-d8", false, true);
    	assertMove("b8-c8", true, true);
    	assertMove("i6-i5", false, true);
    	assertMove("c8-d8", true, true);
    	assertMove("f8-f7", false, true);
    	assertMove("d8-d7", true, true);
    	assertMove("f7-f8", false, true);
    	assertMove("e3-d5", true, true);
    	assertMove("e6-g5", false, true);
    	assertMove("d5-e7", true, true);
    	assertMove("a5-a2", false, true);
    	assertMove("d7-d8", true, true);
    	checkMater(game.getBoard(), blackPlayer);
    }
    
    @Test
    public void testGame2() {
    	startGame(startFEN, true);
    	assertMove("h0-g2", true, true);
    	assertMove("b7-b0", false, true);
    	assertMove("b2-e2", true, true);
    	assertMove("b0-d0", false, true);
    	assertMove("c0-a2", true, true);
    	assertMove("d0-f0", false, true);
    	assertMove("a0-d0", true, true);
    	assertMove("f0-d0", false, true);
    	assertMove("h2-h4", true, true);
    	assertMove("d0-g0", false, true);
    	assertMove("h4-h9", true, true);
    	assertMove("a9-a8", false, true);
    	assertMove("a3-a4", true, true);
    	assertMove("h7-e7", false, true);
    	assertMove("h9-h6", true, true);
    	assertMove("i9-h9", false, true);
    	assertMove("i0-h0", true, true);
    	assertMove("h9-h6", false, true);
    	assertMove("h0-g0", true, true);
    	assertMove("a8-b8", false, true);
    	assertMove("e2-e6", true, true);
    	assertMove("d9-e8", false, true);
    	assertMove("e6-e4", true, true);
    	assertMove("b8-b1", false, true);
    	assertMove("g2-i1", true, true);
    	assertMove("e9-d9", false, true);
    	assertMove("i3-i4", true, true);
    	assertMove("e7-e3", false, true);
    	assertMove("a4-a5", true, true);
    	assertMove("c9-e7", false, true);
    	assertMove("a5-a6", true, true);
    	assertMove("g9-i7", false, true);
    	assertMove("g0-g2", true, true);
    	assertMove("b9-c7", false, true);
    	assertMove("e4-c4", true, true);
    	assertMove("h6-h0", false, true);
    	assertMove("g2-g0", true, true);
    	assertMove("h0-g0", false, true);
    	assertMove("i1-g0", true, true);
    	assertMove("e3-e4", false, true);
    	assertMove("a6-a7", true, true);
    	assertMove("e4-h4", false, true);
    	assertMove("g0-f2", true, true);
    	assertMove("h4-h0", false, true);
    	assertMove("c4-f4", true, true);
    	assertMove("g6-g5", false, true);
    	assertMove("c3-c4", true, true);
    	assertMove("c6-c5", false, true);
    	assertMove("a7-b7", true, true);
    	assertMove("c5-c4", false, true);
    	assertMove("i4-i5", true, true);
    	assertMove("i6-i5", false, true);
    	assertMove("f4-f5", true, true);
    	assertMove("g5-g4", false, true);
    	assertMove("f2-g4", true, true);
    	assertMove("c4-c3", false, true);
    	assertMove("a2-c4", true, true);
    	assertMove("c3-c2", false, true);
    	assertMove("g4-i5", true, true);
    	assertMove("c2-c1", false, true);
    	assertMove("f5-c5", true, true);
    	assertMove("c1-c0", false, true);
    	assertMove("i5-g6", true, true);
    	assertMove("c0-d0", false, true);
    	assertMove("e0-f0", true, true);
    	assertMove("h0-h1", false, true);
    	assertMove("g3-g4", true, true);
    	assertMove("h1-c1", false, true);
    	assertMove("c4-e2", true, true);
    	assertMove("c7-d5", false, true);
    	assertMove("g6-f8", true, true);
    	assertMove("d9-e9", false, true);
    	assertMove("c5-c4", true, true);
    	assertMove("d0-e0", false, true);
    	assertMove("f0-f1", true, true);
    	assertMove("e0-f0", false, true);
    	assertMove("c4-d4", true, true);
    	assertMove("f0-e0", false, true);
    	assertMove("d4-e4", true, true);
    	assertMove("e0-d0", false, true);
    	assertMove("f1-f0", true, true);
    	assertMove("d5-c3", false, true);
    	assertMove("e2-c4", true, true);
    	assertMove("c1-c0", false, true);
    	assertMove("e4-e0", true, true);
    	assertMove("c0-e0", false, true);
    	assertMove("f8-e6", true, true);
    	assertMove("e0-e1", false, true);
    	assertMove("f0-f1", true, true);
    	assertMove("e1-e3", false, true);
    	assertMove("f1-f0", true, true);
    	assertMove("e3-e2", false, true);
    	assertMove("b7-a7", true, true);
    	assertMove("e2-c2", false, true);
    	assertMove("e6-d4", true, true);
    	assertMove("c2-c0", false, true);
    	checkMater(game.getBoard(), redPlayer);
    }
    
    @Test
    public void testGame3() {
    	// no check mate here
    	startGame(startFEN, true);
    	assertMove("c3-c4", true, true);
    	assertMove("h7-d7", false, true);
    	assertMove("g0-e2", true, true);
    	assertMove("h9-g7", false, true);
    	assertMove("b2-d2", true, true);
    	assertMove("b9-c7", false, true);
    	assertMove("b0-c2", true, true);
    	assertMove("a9-b9", false, true);
    	assertMove("a0-b0", true, true);
    	assertMove("b7-b5", false, true);
    	assertMove("i3-i4", true, true);
    	assertMove("i9-h9", false, true);
    	assertMove("h0-i2", true, true);
    	assertMove("g6-g5", false, true);
    	assertMove("f0-e1", true, true);
    	assertMove("c9-e7", false, true);
    	assertMove("b0-b4", true, true);
    	assertMove("h9-h3", false, true);
    	assertMove("h2-f2", true, true);
    	assertMove("f9-e8", false, true);
    	assertMove("c2-d4", true, true);
    	assertMove("b5-d5", false, true);
    	assertMove("b4-b9", true, true);
    	assertMove("c7-b9", false, true);
    	assertMove("f2-f3", true, true);
    	assertMove("h3-h1", false, true);
    	assertMove("d2-d5", true, true);
    	assertMove("d7-d4", false, true);
    	assertMove("d5-b5", true, true);
    	assertMove("g7-f5", false, true);
    	assertMove("e3-e4", true, true);
    	assertMove("f5-d6", false, true);
    	assertMove("i0-f0", true, true);
    	assertMove("c6-c5", false, true);
    	assertMove("c4-c5", true, true);
    	assertMove("d6-b5", false, true);
    	assertMove("c5-b5", true, true);
    	assertMove("b9-c7", false, true);
    	assertMove("b5-b6", true, true);
    	assertMove("c7-d5", false, true);
    	assertMove("f3-e3", true, true);
    	assertMove("h1-h6", false, true);
    	assertMove("f0-f5", true, true);
    	assertMove("d5-b4", false, true);
    	assertMove("f5-f2", true, true);
    	assertMove("d4-i4", false, true);
    	assertMove("e2-c4", true, true);
    	assertMove("g5-g4", false, true);
    	assertMove("g3-g4", true, true);
    	assertMove("i4-e4", false, true);
    	assertMove("f2-f4", true, true);
    	assertMove("e4-e5", false, true);
    	assertMove("f4-e4", true, true);
    	assertMove("h6-h2", false, true);
    	assertMove("e3-e5", true, true);
    	assertMove("e6-e5", false, true);
    	assertMove("e4-e2", true, true);
    	assertMove("h2-e2", false, true);
    	assertMove("c0-e2", true, true);
    	assertMove("b4-c2", false, true);
    	assertMove("b6-a6", true, true);
    	assertMove("i6-i5", false, true);
    	assertMove("i2-g3", true, true);
    	assertMove("c2-d4", false, true);
    	assertMove("g4-g5", true, true);
    	assertMove("e7-g5", false, true);
    	assertMove("a6-b6", true, true);
    	assertMove("d4-f3", false, true);
    	assertMove("a3-a4", true, true);
    	assertMove("e5-e4", false, true);
    	assertMove("e0-f0", true, true);
    	assertMove("e4-d4", false, true);
    	assertMove("b6-b7", true, true);
    	assertMove("f3-e5", false, true);
    	assertMove("c4-a2", true, true);
    	assertMove("e5-c6", false, true);
    	assertMove("g3-f5", true, true);
    	assertMove("e8-f7", false, true);
    	assertMove("b7-c7", true, true);
    	assertMove("d4-d3", false, true);
    	assertMove("f5-d6", true, true);
    	assertMove("d9-e8", false, true);
    	assertMove("f0-e0", true, true);
    	assertMove("i5-i4", false, true);
    	assertMove("a2-c0", true, true);
    	assertMove("i4-h4", false, true);
    	assertMove("d6-c4", true, true);
    	assertMove("d3-c3", false, true);
    	assertMove("e2-g0", true, true);
    	assertMove("c3-c2", false, true);
    	assertMove("c7-c8", true, true);
    	assertMove("h4-h3", false, true);
    	assertMove("a4-a5", true, true);
    	assertMove("c2-c1", false, true);
    	assertMove("c0-e2", true, true);
    	assertMove("h3-g3", false, true);
    	assertMove("c4-d6", true, true);
    	assertMove("g3-g2", false, true);
    	assertMove("a5-b5", true, true);
    	assertMove("c6-d4", false, true);
    	assertMove("d6-e4", true, true);
    	assertMove("g5-e7", false, true);
    	assertMove("e2-c4", true, true);
    	assertMove("e9-d9", false, true);
    	assertMove("g0-e2", true, true);
    	assertMove("c1-d1", false, true);
    	assertMove("e2-g4", true, true);
    	assertMove("g2-g1", false, true);
    	assertMove("b5-b6", true, true);
    	assertMove("g9-i7", false, true);
    	assertMove("g4-i2", true, true);
    	assertMove("i7-g5", false, true);
    	assertMove("i2-g4", true, true);
    	assertMove("g5-i7", false, true);
    	assertMove("g4-i2", true, true);
    	assertMove("e7-g5", false, true);
    	assertMove("i2-g4", true, true);
    	assertMove("e8-d7", false, true);
    	assertMove("e4-f2", true, true);
    	assertMove("d1-c1", false, true);
    	assertMove("f2-e4", true, true);
    	assertMove("d9-e9", false, true);
    	assertMove("e4-d6", true, true);
    	assertMove("d4-f3", false, true);
    	assertMove("c4-e2", true, true);
    	assertMove("c1-d1", false, true);
    	assertMove("e1-f2", true, true);
    	assertMove("e9-f9", false, true);
    	assertMove("d6-e4", true, true);
    	assertMove("f3-d4", false, true);
    	assertMove("d0-e1", true, true);
    	assertMove("f9-f8", false, true);
    	assertMove("e0-f0", true, true);
    	assertMove("d7-e8", false, true);
    	assertMove("e1-d2", true, true);
    	assertMove("g5-e7", false, true);
    	assertMove("c8-d8", true, true);
    	assertMove("i7-g5", false, true);
    	assertMove("d8-c8", true, true);
    	assertMove("e8-d7", false, true);
    	assertMove("c8-d8", true, true);
    	assertMove("d7-e8", false, true);
    	assertMove("d8-c8", true, true);
    	assertMove("e8-d7", false, true);
    	assertMove("c8-d8", true, true);
    	assertMove("d7-e8", false, true);
    	printBoard();
    }
    
    @Test
    public void testGame4() {
    	startGame(startFEN, true);
    	assertMove("e3-e4", true, true);
    	assertMove("b7-c7", false, true);
    	assertMove("h0-g2", true, true);
    	assertMove("c7-c3", false, true);
    	assertMove("b2-e2", true, true);
    	assertMove("c3-i3", false, true);
    	assertMove("i0-i3", true, true);
    	assertMove("h9-g7", false, true);
    	assertMove("h2-h6", true, true);
    	assertMove("e6-e5", false, true);
    	assertMove("e4-e5", true, true);
    	assertMove("g7-e6", false, true);
    	assertMove("e2-e6", true, true);
    	assertMove("b9-c7", false, true);
    	assertMove("e6-f6", true, true);
    	assertMove("a9-b9", false, true);
    	assertMove("b0-c2", true, true);
    	assertMove("b9-b3", false, true);
    	assertMove("f6-a6", true, true);
    	assertMove("b3-a3", false, true);
    	assertMove("a0-a3", true, true);
    	assertMove("c7-a6", false, true);
    	assertMove("a3-a6", true, true);
    	assertMove("i9-i8", false, true);
    	assertMove("h6-c6", true, true);
    	assertMove("g6-g5", false, true);
    	assertMove("a6-a9", true, true);
    	assertMove("h7-h0", false, true);
    	assertMove("c0-e2", true, true);
    	assertMove("i6-i5", false, true);
    	assertMove("e5-e6", true, true);
    	assertMove("i5-i4", false, true);
    	assertMove("i3-i0", true, true);
    	assertMove("h0-h2", false, true);
    	assertMove("i0-h0", true, true);
    	assertMove("h2-i2", false, true);
    	assertMove("g0-i2", true, true);
    	assertMove("i8-f8", false, true);
    	assertMove("f0-e1", true, true);
    	assertMove("f8-f1", false, true);
    	assertMove("a9-c9", true, true);
    	assertMove("f1-i1", false, true);
    	assertMove("e6-e7", true, true);
    	assertMove("i1-i2", false, true);
    	assertMove("c6-e6", true, true);
    	assertMove("f9-e8", false, true);
    	assertMove("e7-e8", true, true);
    	assertMove("e9-e8", false, true);
    	assertMove("h0-h7", true, true);
    	assertMove("i2-g2", false, true);
    	assertMove("c9-c8", true, true);
    	assertMove("e8-e9", false, true);
    	assertMove("h7-h9", true, true);
    	assertMove("g2-e2", false, true);
    	assertMove("h9-g9", true, true);
    	checkMater(game.getBoard(), blackPlayer);
    }
    
    @Test
    public void todesBlickTest() {
    	startGame(startFEN, true);
    	assertMove("e3-e4", true, true);
    	assertMove("e6-e5", false, true);
    	assertMove("e4-e5", true, true);
    	assertMove("c6-c5", false, true);
    	assertMove("e5-e6", true, true);
    	assertMove("g6-g5", false, true);
    	printBoard();
    	ArrayList<String> moves = game.validMoves(redPlayer);
    	for(String move:moves) System.out.println(move);
    }
    
//    @Test
//    public void testSchach() {
//    	startGame(startFEN,true);
//    	printBoard();
//    	System.out.println(game.isCheck(redPlayer,"rCeag2R1/4a3r/1c5c1/s1s3s1s/4S4/9/S1S3S1S/9/9/RHEAGAEHR"));
//    }
    
    @Test
    public void test() {
    	startGame(startFEN,true);
    	printBoard();
    }

    // functions : 
//    tryMove -> implement in TryMoveIntegrationTest
//    doMove -> DONE
//    checkFigur
//    checkMove
//    startZielIsValid -> DONE
//    moveInBoard -> DONE
//    getTranslatedMove -> DONE
//    spalteMove
//    FENtoBoard -> DONE
//    boardToFEN -> DONE
//    checkGeneral
//    checkAdvisor
//    checkElephant
    
    // to check which moves are valid go to http://www.xichess.com/game/create, and use 2 browser to play against yourself
    // a better visualization of xiangqi rules: https://www.ymimports.com/pages/how-to-play-xiangqi-chinese-chess

//████████╗░█████╗░██████╗░░█████╗░  ██╗  ░█████╗░░█████╗░███╗░░██╗████████╗██╗███╗░░██╗██╗░░░██╗███████╗
//╚══██╔══╝██╔══██╗██╔══██╗██╔══██╗  ╚═╝  ██╔══██╗██╔══██╗████╗░██║╚══██╔══╝██║████╗░██║██║░░░██║██╔════╝
//░░░██║░░░██║░░██║██║░░██║██║░░██║  ░░░  ██║░░╚═╝██║░░██║██╔██╗██║░░░██║░░░██║██╔██╗██║██║░░░██║█████╗░░
//░░░██║░░░██║░░██║██║░░██║██║░░██║  ░░░  ██║░░██╗██║░░██║██║╚████║░░░██║░░░██║██║╚████║██║░░░██║██╔══╝░░
//░░░██║░░░╚█████╔╝██████╔╝╚█████╔╝  ██╗  ╚█████╔╝╚█████╔╝██║░╚███║░░░██║░░░██║██║░╚███║╚██████╔╝███████╗
//░░░╚═╝░░░░╚════╝░╚═════╝░░╚════╝░  ╚═╝  ░╚════╝░░╚════╝░╚═╝░░╚══╝░░░╚═╝░░░╚═╝╚═╝░░╚══╝░╚═════╝░╚══════╝

}
