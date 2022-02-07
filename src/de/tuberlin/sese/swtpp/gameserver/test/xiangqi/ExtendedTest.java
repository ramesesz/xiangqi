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
	
    public void printBoardArr(char[][] boardArr) {
    	for(int i=0;i<boardArr.length;i++) {
    		for(int j=0;j<boardArr[i].length;j++) {
    			System.out.print(boardArr[i][j]+" ");
    		}
    		System.out.println("");
    	}
    }
    
    public void printBoard(String board) {
    	printBoardArr(game.FENtoBoard(board));
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
    	assertEquals(game.doMove("a0-a2", redPlayer),"rheagaehr/9/1c5c1/s1s1s1s1s/9/9/S1S1S1S1S/RC5C1/9/1HEAGAEHR");
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
    	assertFalse(game.moveInBoard("A1-a2"));
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
    
    @Test
    public void testValidMoves() {
    	startGame(startFEN, false);
    	ArrayList<String> moveList = game.validMoves(blackPlayer, game.FENtoBoard(game.getBoard()));
    	
    	for (String move: moveList) {
    		System.out.println(move);
    	}
    }
    
//    @Test
//    public void testValidMovesByFiguren() {
//    	startGame(startFEN, false);
//    	game.doMove("d9-e8", blackPlayer);
//    	String[] figuren = {"e8"};
//    	ArrayList<String> moveList = game.validMoves(blackPlayer, game.FENtoBoard(game.getBoard()), figuren);
//    	ArrayList<String> expectedList = new ArrayList<String>();
//    	expectedList.add("e8-d9");
//    	expectedList.add("e8-d7");
//    	expectedList.add("e8-f7");
//    	
//    	assertTrue(moveList.equals(expectedList));
////    	for (String move: moveList) {
////    		System.out.println(move);
////    	}
//    }
//    
//    @Test
//    public void testCannonValidMoves() {
//    	startGame(startFEN, true);
//    	game.doMove("b2-b9", redPlayer);
//    	String[] figuren = {"b9"};
//    	ArrayList<String> moveList = game.validMoves(redPlayer, game.FENtoBoard(game.getBoard()), figuren);
//    	ArrayList<String> expectedList = new ArrayList<String>();
//    	expectedList.add("b9-d9");
//    	expectedList.add("b9-b8");
//
//    	assertTrue(moveList.equals(expectedList));
//    	System.out.println("blabla");
//    	for (String move: moveList) {
//    		System.out.println(move);
//    	}
//    }
    
//    @Test
//    public void validMovesChecker() {
//    	startGame(startFEN, false);
//    	String[] figuren = {"a6"};
//    	ArrayList<String> moveList = game.validMoves(blackPlayer, game.FENtoBoard(game.getBoard()), figuren);
////    	System.out.println("valid moves for a0: ");
////    	for (String moveString: moveList) {
////    		int[] move = game.getTranslatedMove(moveString);
////    		int[] pos = {move[2],move[3]};
////    		setFigur(pos, 'Z');
////    		System.out.println(move);
////    	}
////    	printBoard(startFEN);
////    	System.out.println("--------------------");
////    	printBoard();
//    }
    
	@Test
	public void testCheckMate() {
		startGame("7R1/5g3/C2RH4/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2",true);
		String[] figuren = {"d7"};
    	ArrayList<String> moveList = game.validMoves(redPlayer, game.FENtoBoard(game.getBoard()), figuren);
//    	for (String moveString: moveList) {
//    		int[] move = game.getTranslatedMove(moveString);
//    		int[] pos = {move[2],move[3]};
//    		setFigur(pos, 'Z');
//    		System.out.println(move);
//    	}
		assertMove("d7-d8", true, true);
//		printBoard();
//		assertGameState("7R1/3R1g3/C3H4/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2", false, false, true);
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
