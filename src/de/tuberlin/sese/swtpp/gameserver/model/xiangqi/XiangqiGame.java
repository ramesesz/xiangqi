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
	
	public boolean checkFigur(int[] translatedMove, char[][] board, char figur){
		switch (figur) {
			case 'G':
			case 'g':
				if (!checkGeneral(board, translatedMove))
					return false;
				break;
			case 'A':
			case 'a':
				if (!checkAdvisor(board, translatedMove))
					return false;
				break;
			case 'E':
			case 'e':
				if (!checkElephant(board, translatedMove))
					return false;
				break;
			case 'H':
			case 'h':
				if (!checkHorse(board, translatedMove))
					return false;
				break;
			case 'R':
			case 'r':
				if (!checkRook(board, translatedMove))
					return false;
				break;
			case 'C':
			case 'c':
				if (!checkCannon(board, translatedMove, figur, schlagen))
					return false;
				break;
			case 'S':
			case 's':
				if (!checkSoldier(board, translatedMove, figur, schlagen))
					return false;
				break;
			default:
				return false;
			}
		return true;
	}

	public boolean startZielIsValid(char[][] board, int[] translatedMove, Player player){
		int spalteMove1 = translatedMove[0];
		int zeileMove1 = translatedMove[1];
		int spalteMove2 = translatedMove[2];
		int zeileMove2 = translatedMove[3];
		//same start and destination?
		if(spalteMove1 == spalteMove2 && zeileMove1 == zeileMove2) 
			return false;
		//piece to be moved belongs to enemy?
		if(!Character.isAlphabetic(board[spalteMove1][zeileMove1]) 
		|| player == this.redPlayer && Character.isLowerCase(board[spalteMove1][zeileMove1]) 
		|| player == this.blackPlayer && Character.isUpperCase(board[spalteMove1][zeileMove1])) 
			return false;
		//piece at dest belongs to player?
		if(player == this.redPlayer && Character.isUpperCase(board[spalteMove2][zeileMove2])
		|| player == this.blackPlayer && Character.isLowerCase(board[spalteMove2][zeileMove2]))
			return false;

		return true;
	}

	//Equivalent to checkMoveFormat()
	public boolean moveInBoard(String moveString){
		//checks whether move is within board limits
		String validZeile = "0123456789";
		String validSpalte = "abcdefghij";

		char[] move = moveString.toCharArray();
		
		if(!validSpalte.contains(String.valueOf(move[0])) || !validZeile.contains(String.valueOf(move[1])) 
		|| !validSpalte.contains(String.valueOf(move[3])) || !validZeile.contains(String.valueOf(move[4])) || move[2] != '-') return false;

		return true;
	}
	
	public int[] getTranslatedMove(String moveString) {
		// c3-c4 -> [2625]
		int translatedMove[] = new int[4];
		char[] move = moveString.toCharArray();
		translatedMove[0] = spalteMove(move[0]);
		translatedMove[1] = move[1];
		translatedMove[2] = spalteMove(move[3]);
		translatedMove[3] = move[4];

		return translatedMove;
	}

	public int spalteMove(char move) {
		int spalte = -1;
		switch (move) {
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

	public boolean checkGeneral(int[] translatedMove, Player player) {
		int spalteMove1 = translatedMove[0];
		int zeileMove1 = translatedMove[1];
		int spalteMove2 = translatedMove[2];
		int zeileMove2 = translatedMove[3];
		String palastSpalte = "345";
		String palastZeileRot = "012";
		String palastZeileSchwarz = "789";
		//move within palace?
		if(!palastSpalte.contains(String.valueOf(spalteMove1)) || !palastSpalte.contains(String.valueOf(spalteMove2))){
			return false;
		}
		if(player == this.redPlayer && (!palastZeileRot.contains(String.valueOf(zeileMove1)) || !palastZeileRot.contains(String.valueOf(zeileMove2)))){
			return false;
		}
		if(player == this.blackPlayer && (!palastZeileSchwarz.contains(String.valueOf(zeileMove1)) || !palastZeileSchwarz.contains(String.valueOf(zeileMove2)))){
			return false;
		}
		//move one space?
		if (Math.abs(spalteMove1 - spalteMove2) > 1 || Math.abs(zeileMove1 - zeileMove2) > 1)
			return false;

		return true;
	}

	public boolean checkAdvisor(int[] translatedMove, Player player){
		int spalteMove1 = translatedMove[0];
		int zeileMove1 = translatedMove[1];
		int spalteMove2 = translatedMove[2];
		int zeileMove2 = translatedMove[3];
		String palastSpalte = "345";
		String palastZeileRot = "012";
		String palastZeileSchwarz = "789";
		//move within palace?
		if(!palastSpalte.contains(String.valueOf(spalteMove1)) || !palastSpalte.contains(String.valueOf(spalteMove2))){
			return false;
		}
		if(player == this.redPlayer && (!palastZeileRot.contains(String.valueOf(zeileMove1)) || !palastZeileRot.contains(String.valueOf(zeileMove2)))){
			return false;
		}
		if(player == this.blackPlayer && (!palastZeileSchwarz.contains(String.valueOf(zeileMove1)) || !palastZeileSchwarz.contains(String.valueOf(zeileMove2)))){
			return false;
		}
		//move one space diagonally?
		if (Math.abs(spalteMove1 - spalteMove2) != 1 || Math.abs(zeileMove1 - zeileMove2) != 1)
			return false;

		return true;
	}
}
