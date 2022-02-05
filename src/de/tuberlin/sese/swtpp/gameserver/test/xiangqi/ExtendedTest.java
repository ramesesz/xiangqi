package de.tuberlin.sese.swtpp.gameserver.test.xiangqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
	

	/*******************************************
	 * !!!!!!!!! To be implemented !!!!!!!!!!!!
	 *******************************************/
    
    @Test
    public void testGetTranslatedMove() {
    	// check translation from moveString to boardArr's zeile and spalte number
        int[] move = game.getTranslatedMove("a0-a2");
        int[] expectedMove = {9,0,7,0};
        assertTrue(Arrays.equals(move,expectedMove));
    }
    
    @Test
    public void testBoardConverter() {
    	String board = startFEN;
    	char[][] boardArr = game.FENtoBoard(board);
    	String newBoard = game.boardToFEN(boardArr);
    	assertEquals(board, newBoard);
    }
    
    @Test
    public void testDoMove() {
    	// test basic movement(unchecked)
    	startGame(startFEN, true);
    	game.doMove("a0-a2");
    	// redNext is true because doMove only applies the move to the board and not add it to history or set the next player
    	assertGameState("rheagaehr/9/1c5c1/s1s1s1s1s/9/9/S1S1S1S1S/RC5C1/9/1HEAGAEHR", true, false, false);
//    	printBoard(game.getBoard());
    }
    
    @Test
    public void testMoveInBoard() {
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
