package de.tuberlin.sese.swtpp.gameserver.test.xiangqi;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.tuberlin.sese.swtpp.gameserver.control.GameController;
import de.tuberlin.sese.swtpp.gameserver.model.Game;
import de.tuberlin.sese.swtpp.gameserver.model.Player;
import de.tuberlin.sese.swtpp.gameserver.model.User;

public class TryMoveIntegrationTest {


	User user1 = new User("Alice", "alice");
	User user2 = new User("Bob", "bob");
	
	Player redPlayer = null;
	Player blackPlayer = null;
	Game game = null;
	GameController controller;
	
	@Before
	public void setUp() throws Exception {
		controller = GameController.getInstance();
		controller.clear();
		
		int gameID = controller.startGame(user1, "", "xiangqi");
		
		game =  controller.getGame(gameID);
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
	

	/*******************************************
	 * !!!!!!!!! To be implemented !!!!!!!!!!!!
	 *******************************************/

	String startFEN = "rheagaehr/9/1c5c1/s1s1s1s1s/9/9/S1S1S1S1S/1C5C1/9/RHEAGAEHR";

	// * 9 r h e a g a e h r 
	// * 8 . . . . . . . . . 
	// * 7 . c . . . . . c . 
	// * 6 s . s . s . s . s 
	// * 5 . . . . . . . . . 
	// * 4 . . . . . . . . . 
	// * 3 S . S . S . S . S 
	// * 2 . C . . . . . C . 
	// * 1 . . . . . . . . . 
	// * 0 R H E A G A E H R 
	// * + a b c d e f g h i 
	
	@Test
	public void exampleTest() {
	    startGame("rheagaehr/9/1c5c1/s1s1s1s1s/9/9/S1S1S1S1S/1C5C1/9/RHEAGAEHR",true);
	    assertMove("e3-e4",true,true);
	    assertGameState("rheagaehr/9/1c5c1/s1s1s1s1s/9/4S4/S1S3S1S/1C5C1/9/RHEAGAEHR",false,false,false);
	}

	//TODO: implement test cases of same kind as example here

	@Test
	public void soldierWrongB() {
		startGame(startFEN,false);
		// tried to move red pawn
		assertMove("e3-e4",false,false);
		// tried to move horizontal in own field
		assertMove("c6-d6",false,false);
		// tried to move backward
		assertMove("c6-c7",false,false);
		// tried to move diagonally
		assertMove("c6-d5",false,false);
		assertMove("c6-b5",false,false);
		assertMove("c6-b7",false,false);
		// confirm
		assertGameState(startFEN, false, false, false);
	}

	@Test
	public void rookWrongR() {
		startGame(startFEN,true);
		// tried to capture own horizontally
		assertMove("i0-h0",true,false);
		// tried to capture own vertically
		assertMove("i0-i3",true,false);
		// tried to move diagonally
		assertMove("i0-h1",true,false);
		// confirm
		assertGameState(startFEN, true, false, false);
	}

	@Test
	public void horseWrongB() {
		startGame(startFEN, false);
		// tried to move but blocked
		assertMove("b9-d8",false,false);
		// invalid moves
		assertMove("b9-d7",false,false);
		assertMove("b9-a8",false,false);
	}

	@Test
	public void elephantWrongR() {
		startGame(startFEN, true);
		// invalid moves
		assertMove("c0-d1",true,false);
		assertMove("c0-c1",true,false);
		assertMove("c0-b1",true,false);
		assertMove("c0-c2",true,false);
		// try to move and try to cross river 1
		assertMove("c0-e2",true,true);
		// opponent turn
		assertMove("e6-e5",false,true);
		// try to move and try to cross river 2
		assertMove("e2-g4",true,true);
		// opponent turn
		assertMove("a6-a5",false,true);
		// try to move and try to cross river 3
		assertMove("g4-e6",true,false);
		assertMove("h2-h3",true,true);
		assertMove("i6-i5",false,true);
		assertMove("g4-i2",true,false);
	}

	@Test
	public void advisorWrongB() {
		startGame(startFEN, false);
		// tried to move to outside palast
		assertMove("d9-c8",false,false);
		// tried to move vertically
		assertMove("d9-d8",false,false);
	}

	@Test
	public void generalWrongR() {
		startGame(startFEN, true);
		// tried to move diagonally
		assertMove("e0-d1",true,false);
		assertMove("e0-f1",true,false);
		// blocked
		assertMove("e0-d0",true,false);
		assertMove("e0-f0",true,false);
	}

	@Test
	public void cannonWrongB() {
		startGame(startFEN, false);
		// tried to eat own
		assertMove("b7-h7",false,false);
		// tried to move but blocked
		assertMove("b7-i7",false,false);
		// tried to pwn but no one to jump
		assertMove("b7-b2",false,false);
		// tried to move but blocked
		assertMove("b7-b1",false,false);
	}
	
	@Test
	public void testRWon() {
		startGame("7R1/5g3/C2RH4/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2", true);
		assertMove("d7-d8", true, true);
		assertGameState("7R1/3R1g3/C3H4/s1s3s2/6h1s/9/S1S5S/c1H6/4A4/4GAE2", true, true, true);
	}
	
	@Test
	public void testBWon() {
		startGame("4ga3/4a4/S3e3e/9/9/2EH2S2/2h6/2c6/1r7/3s1G3", false);
		assertMove("c2-c0", false, true);
		assertGameState("4ga3/4a4/S3e3e/9/9/2EH2S2/2h6/9/1r7/2cs1G3", false, true, false);
	}
}
