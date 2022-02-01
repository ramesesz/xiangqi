package de.tuberlin.sese.swtpp.gameserver.model.xiangqi;

import de.tuberlin.sese.swtpp.gameserver.model.*;
//TODO: more imports from JVM allowed here
import java.util.Arrays;

import java.io.Serializable;

public class XiangqiGame extends Game implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 5424778147226994452L;

	/************************
	 * member
	 ***********************/

	// just for better comprehensibility of the code: assign red and black player
	private Player blackPlayer;
	private Player redPlayer;

	// internal representation of the game state
	// TODO: insert additional game data here
	private String board;

	/************************
	 * constructors
	 ***********************/

	public XiangqiGame() {
		super();

		// TODO: initialization of game state can go here
		this.board = "rheagaehr/9/1c5c1/s1s1s1s1s/9/9/S1S1S1S1S/1C5C1/9/RHEAGAEHR";
	}

	public String getType() {
		return "xiangqi";
	}

	/*******************************************
	 * Game class functions already implemented
	 ******************************************/

	@Override
	public boolean addPlayer(Player player) {
		if (!started) {
			players.add(player);

			// game starts with two players
			if (players.size() == 2) {
				started = true;
				this.redPlayer = players.get(0);
				this.blackPlayer= players.get(1);
				nextPlayer = redPlayer;
			}
			return true;
		}

		return false;
	}

	@Override
	public String getStatus() {
		if (error)
			return "Error";
		if (!started)
			return "Wait";
		if (!finished)
			return "Started";
		if (surrendered)
			return "Surrendered";
		if (draw)
			return "Draw";

		return "Finished";
	}

	@Override
	public String gameInfo() {
		String gameInfo = "";

		if (started) {
			if (blackGaveUp())
				gameInfo = "black gave up";
			else if (redGaveUp())
				gameInfo = "red gave up";
			else if (didRedDraw() && !didBlackDraw())
				gameInfo = "red called draw";
			else if (!didRedDraw() && didBlackDraw())
				gameInfo = "black called draw";
			else if (draw)
				gameInfo = "draw game";
			else if (finished)
				gameInfo = blackPlayer.isWinner() ? "black won" : "red won";
		}

		return gameInfo;
	}

	@Override
	public String nextPlayerString() {
		return isRedNext() ? "w" : "b";
	}

	@Override
	public int getMinPlayers() {
		return 2;
	}

	@Override
	public int getMaxPlayers() {
		return 2;
	}

	@Override
	public boolean callDraw(Player player) {

		// save to status: player wants to call draw
		if (this.started && !this.finished) {
			player.requestDraw();
		} else {
			return false;
		}

		// if both agreed on draw:
		// game is over
		if (players.stream().allMatch(Player::requestedDraw)) {
			this.draw = true;
			finish();
		}
		return true;
	}

	@Override
	public boolean giveUp(Player player) {
		if (started && !finished) {
			if (this.redPlayer == player) {
				redPlayer.surrender();
				blackPlayer.setWinner();
			}
			if (this.blackPlayer == player) {
				blackPlayer.surrender();
				redPlayer.setWinner();
			}
			surrendered = true;
			finish();

			return true;
		}

		return false;
	}

	/* ******************************************
	 * Helpful stuff
	 ***************************************** */

	/**
	 *
	 * @return True if it's red player's turn
	 */
	public boolean isRedNext() {
		return nextPlayer == redPlayer;
	}

	/**
	 * Ends game after regular move (save winner, finish up game state,
	 * histories...)
	 *
	 * @param winner player who won the game
	 * @return true if game was indeed finished
	 */
	public boolean regularGameEnd(Player winner) {
		// public for tests
		if (finish()) {
			winner.setWinner();
			winner.getUser().updateStatistics();
			return true;
		}
		return false;
	}

	public boolean didRedDraw() {
		return redPlayer.requestedDraw();
	}

	public boolean didBlackDraw() {
		return blackPlayer.requestedDraw();
	}

	public boolean redGaveUp() {
		return redPlayer.surrendered();
	}

	public boolean blackGaveUp() {
		return blackPlayer.surrendered();
	}

	/*******************************************
	 * !!!!!!!!! To be implemented !!!!!!!!!!!!
	 ******************************************/

	@Override
	public void setBoard(String state) {
		// Note: This method is for automatic testing. A regular game would not start at some artificial state.
		//       It can be assumed that the state supplied is a regular board that can be reached during a game.
		// TODO: implement
		this.board = state;
	}

	@Override
	public String getBoard() {
		// TODO: implement
		return this.board;
	}

	@Override
	public boolean tryMove(String moveString, Player player) {
		// TODO: implement

		return false;
	}
	
	public String[] getTranslatedMove(String moveString) {
		// c3c4 -> [26][25]
		String translatedMove[] = new String[2];
		String[] move = moveString.split("-");
		translatedMove[0] = Integer.toString(zeileMove(move[0])) + Integer.toString(spalteMove(move[0]));
		translatedMove[1] = Integer.toString(zeileMove(move[1])) + Integer.toString(spalteMove(move[1]));
		
		return translatedMove;
	}
	
	// �bersetzt die Zeile in die Indexposition des Arrays
	public int zeileMove(String move) {
		int zeile = 10;
		switch (move.toCharArray()[1]) {
		case '0':
			zeile = 9;
			break;
		case '1':
			zeile = 8;
			break;
		case '2':
			zeile = 7;
			break;
		case '3':
			zeile = 6;
			break;
		case '4':
			zeile = 5;
			break;
		case '5':
			zeile = 4;
			break;
		case '6':
			zeile = 3;
			break;
		case '7':
			zeile = 2;
			break;
		case '8':
			zeile = 1;
			break;
		case '9':
			zeile = 0;
			break;
		}

		return zeile;
	}	
		
	public int spalteMove(String move) {
		int spalte = 9;
		switch (move.toCharArray()[0]) {
		case 'a':
			spalte = 0;
			break;
		case 'b':
			spalte = 1;
			break;
		case 'c':
			spalte = 2;
			break;
		case 'd':
			spalte = 3;
			break;
		case 'e':
			spalte = 4;
			break;
		case 'f':
			spalte = 5;
			break;
		case 'g':
			spalte = 6;
			break;
		case 'h':
			spalte = 7;
			break;
		case 'i':
			spalte = 8;
			break;
		default:
			return spalte = -1;
		}
		return spalte;
	}	
	
	public char[][] FENtoBoard(String boardstr){
		String[] boardarr = boardstr.split("/");
		char[][] board = new char[9][10];
		for(int i=0;i<9;i++) {
			int n = 0;
			for(int j=0;j<boardarr[i].length();j++) {
				if (!Character.isAlphabetic(boardarr[i].charAt(j))) {
					for (int k = 0; k < Character.getNumericValue(boardarr[i].charAt(j)); k++) {
						board[i][n] = ' ';
						n++;
					}
				} else {
					board[i][n] = boardarr[i].charAt(j);
					n++;
				}
			}
		}
		return board;
	}
	
	public String boardToFEN(char[][] board) {
		String state = "";
		for (int i=0;i<9;i++) {
			int n = 0;
			for(int j=0;j<board[i].length;j++) {
				if (!Character.isAlphabetic(board[i][j])) {
					n++;
					if (j == board[i].length - 1)
						state = state + n;
				} else {
					if (n != 0) {
						state = state + n + board[i][j];
						n = 0;
					} else {
						state = state + board[i][j];
					}
				}
			}
			if(i!=8) state = state + "/";
		}
		return state;
	}

	
	public boolean checkGeneral(char[][] board, String[] translatedMove) {
		
		return true;
	}
}
