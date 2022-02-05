package de.tuberlin.sese.swtpp.gameserver.model.xiangqi;

import de.tuberlin.sese.swtpp.gameserver.model.*;

import java.util.ArrayList;
//TODO: more imports from JVM allowed here
import java.util.Arrays;

import java.io.Serializable;

public class XiangqiGame extends Game implements Serializable{
//random comment
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
		return isRedNext() ? "r" : "b";
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
		// Verlauf: check if move is valid -> do the move (modify board) -> set next player -> add to history -> check if someone won
		if(!checkMove(moveString, player)) return false;
		if(!doMove(moveString)) return false;
		this.setNextPlayer(player == redPlayer ? blackPlayer : redPlayer);
		this.history.add(new Move(moveString,getBoard(),player));
		// check if someone won
		return true;
	}
	
	public boolean doMove(String moveString) {
		char[][] board = FENtoBoard(getBoard());
		int[] move = getTranslatedMove(moveString);
		char startFigur = board[move[0]][move[1]];
		char zielFigur = board[move[2]][move[3]];
		// You can't directly kill the general
		if (Character.toLowerCase(zielFigur)=='g') return false;
		board[move[0]][move[1]] = ' ';
		board[move[2]][move[3]] = startFigur;
		String newBoard = boardToFEN(board);
		setBoard(newBoard);
		return true;
	}
	

	public boolean checkMove(String moveString, Player player){
		char[][] boardArr = FENtoBoard(getBoard());
		int[] move = getTranslatedMove(moveString);
		if(!startZielIsValid(boardArr, move, player)) return false;
		if(!moveInBoard(moveString)) return false;
		if(!checkFigur(move, boardArr , player)) return false;
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
		// invert the column 
		// move = {zeile1, spalte1, zeile2, spalte2}
		translatedMove[0] = 9 - Character.getNumericValue(move[1]);
		translatedMove[1] = spalteMove(move[0]);
		translatedMove[2] = 9 - Character.getNumericValue(move[4]);
		translatedMove[3] = spalteMove(move[3]);

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
		char[][] board = new char[10][9];
		for(int i=0;i<10;i++) {
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
		for (int i=0;i<10;i++) {
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
			if(i!=9) state = state + "/";
		}
		return state;
	}
	
	public ArrayList<String> validMoves(Player player,char[][] board, String[] figuren) {
		String validSpalte = "abcdefghij";
		ArrayList<String> moveList = new ArrayList<String>();
		int counter = figuren.length;
		
		for(int n=0;n<counter;n++) {
			for(int i=0;i<10;i++) {
				for(int j=0;j<9;j++) {
					String moveTo=validSpalte.charAt(j)+Integer.toString(9-i);
					String moveString=figuren[n]+"-"+moveTo;
					if (checkMove(moveString, player)) moveList.add(moveString);
				}
			}
		}
		
		return moveList;
	}
	
	public ArrayList<String> validMoves(Player player, char[][] board) {
		String figuren[]= new String[16];
		String validSpalte = "abcdefghij";
		int counter = 0;
		boolean isRedPlayer = player == redPlayer;
		
		for(int i=0;i<10;i++) {
			for(int j=0;j<9;j++) {
				char figur = board[i][j];
				if((isRedPlayer && Character.isUpperCase(figur)) || (!isRedPlayer && Character.isLowerCase(figur))) {
					figuren[counter]=validSpalte.charAt(j)+Integer.toString(9-i);
					counter++;
				}
			}
		}
		
		return validMoves(player, board, figuren);
	}
	
	public ArrayList<String> validMoves(Player player) {
		return validMoves(player, FENtoBoard(getBoard()));
	}

	public boolean checkFigur(int[] translatedMove, char[][] board, Player player){
		char figur = board[translatedMove[0]][translatedMove[1]];
		switch (figur) {
			case 'G':
			case 'g':
				if (!checkGeneral(translatedMove, player))
					return false;
				break;
			case 'A':
			case 'a':
				if (!checkAdvisor(translatedMove, player))
					return false;
				break;
			case 'E':
			case 'e':
				if (!checkElephant(translatedMove, player))
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
				if (!checkCannon(board, translatedMove))
					return false;
				break;
			case 'S':
			case 's':
				if (!checkSoldier(translatedMove, player))
					return false;
				break;
			default:
				return false;
			}
		return true;
	}
	
	public boolean checkGeneral(int[] translatedMove, Player player) {
		int zeileMove1 = translatedMove[0];
		int spalteMove1 = translatedMove[1];
		int zeileMove2 = translatedMove[2];
		int spalteMove2 = translatedMove[3];
		String palastSpalte = "345";
		String palastZeileRot = "789";
		String palastZeileSchwarz = "012";
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
		if(Math.abs(zeileMove1 - zeileMove2) + Math.abs(spalteMove1 - spalteMove2) != 1)
			return false; //move one space?

		return true;
	}

	public boolean checkAdvisor(int[] translatedMove, Player player){
		int zeileMove1 = translatedMove[0];
		int spalteMove1 = translatedMove[1];
		int zeileMove2 = translatedMove[2];
		int spalteMove2 = translatedMove[3];
		String palastSpalte = "345";
		String palastZeileRot = "789";
		String palastZeileSchwarz = "012";
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
		if (Math.abs(spalteMove1 - spalteMove2) != 1 || Math.abs(zeileMove1 - zeileMove2) != 1)
			return false;

		return true;
	}

	public boolean checkElephant(int[] translatedMove, Player player){
		int zeileMove1 = translatedMove[0];
		int spalteMove1 = translatedMove[1];
		int zeileMove2 = translatedMove[2];
		int spalteMove2 = translatedMove[3];
		String redZeile = "56789";
		String blackZeile = "01234";
		//elephant stays in own field?
		if (player == this.redPlayer){
			if(!redZeile.contains(String.valueOf(zeileMove1)) || !redZeile.contains(String.valueOf(zeileMove2))) 
				return false;
		}
		if (player == this.blackPlayer){
			if(!blackZeile.contains(String.valueOf(zeileMove1)) || !blackZeile.contains(String.valueOf(zeileMove2))) 
				return false;
		}
		//move 2 paces diagonally?
		if (Math.abs(zeileMove1 - zeileMove2) != 2 || Math.abs(spalteMove1 - spalteMove2) != 2)
			return false;

		return true;
	}
	
	public boolean checkHorse(char[][] board, int[] translatedMove){
		int zeileMove1 = translatedMove[0];
		int spalteMove1 = translatedMove[1];
		int zeileMove2 = translatedMove[2];
		int spalteMove2 = translatedMove[3];
		
		if(zeileMove2-zeileMove1==-2) {
			if(Character.isAlphabetic(board[zeileMove1-1][spalteMove1])) return false;
		} else if(spalteMove2-spalteMove1==2) {
			if(Character.isAlphabetic(board[zeileMove1][spalteMove1+1])) return false;
		} else if(zeileMove2-zeileMove1==2) {
			if(Character.isAlphabetic(board[zeileMove1+1][spalteMove1])) return false;
		} else if(spalteMove2-spalteMove1==-2) {
			if(Character.isAlphabetic(board[zeileMove1][spalteMove1-1])) return false;
		}
		
		if ((Math.abs(zeileMove1- zeileMove2) == 1 && Math.abs(spalteMove1 - spalteMove2) == 2)
				|| (Math.abs(zeileMove1- zeileMove2) == 2 && Math.abs(spalteMove1 - spalteMove2) == 1)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean checkRook(char[][] board, int[] translatedMove){
		// can only move either vertical or horizontal
		if(translatedMove[2]-translatedMove[0] !=0 && translatedMove[3]-translatedMove[1] !=0) return false;
		if(!checkRookVertical(board, translatedMove))
			return false;
		if(!checkRookHorizontal(board, translatedMove))
			return false;
		return true;
	}

	public boolean checkRookVertical(char[][] board, int[] translatedMove){
		int zeileMove1 = translatedMove[0];
		int spalteMove1 = translatedMove[1];
		int zeileMove2 = translatedMove[2];
//		int spalteMove2 = translatedMove[3];
		
		//check above
		if (zeileMove1 - zeileMove2 > 1) {
			int steps = zeileMove1 - zeileMove2;
			for (int i = 1; i < steps; i++) {
				if (Character.isAlphabetic(board[zeileMove1 - i][spalteMove1]))
					return false;
			}
		}
		//check below
		else if (zeileMove1 - zeileMove2 < 1) {
			int steps = Math.abs(zeileMove1 - zeileMove2);
			for (int i = 1; i < steps; i++) {
				if (Character.isAlphabetic(board[zeileMove1 + i][spalteMove1]))
					return false;
			}
		}
		return true;
	}

	public boolean checkRookHorizontal(char[][] board, int[] translatedMove){
		int zeileMove1 = translatedMove[0];
		int spalteMove1 = translatedMove[1];
//		int zeileMove2 = translatedMove[2];
		int spalteMove2 = translatedMove[3];
		
		//check left
		if (spalteMove1 - spalteMove2 > 1) {
			int steps = spalteMove1 - spalteMove2;
			for (int i = 1; i < steps; i++) {
				if (Character.isAlphabetic(board[zeileMove1][spalteMove1 - i]))
					return false;
			}
		}
		//check right
		else if (spalteMove1 - spalteMove2 < 1) {
			int steps = Math.abs(spalteMove1 - spalteMove2);
			for (int i = 1; i < steps; i++) {
				if (Character.isAlphabetic(board[zeileMove1][spalteMove1 + i]))
					return false;
			}
		}
		return true;
	}

	public boolean checkCannon(char[][] board, int[] translatedMove){
		char startFigur = board[translatedMove[0]][translatedMove[1]];
		char zielFigur = board[translatedMove[2]][translatedMove[3]];
		
		// can only move either vertical or horizontal
		if(translatedMove[2]-translatedMove[0] !=0 && translatedMove[3]-translatedMove[1] !=0) return false;
		
		if(!Character.isAlphabetic(zielFigur)) {
			if(!checkCannonMove(board, translatedMove))
				return false;
		}
		else if((Character.isUpperCase(startFigur) && Character.isLowerCase(zielFigur))
		|| (Character.isLowerCase(startFigur) && Character.isUpperCase(zielFigur))){
			if(!checkCannonTake(board, translatedMove))
				return false;
		}
		return true;
	}

	public boolean checkCannonMove(char[][] board, int[] translatedMove){
		if(!checkRookVertical(board, translatedMove))
			return false;
		if(!checkRookHorizontal(board, translatedMove))
			return false;

		return true;
	}

	public boolean checkCannonTake(char[][] board, int[] translatedMove){
		int zeileMove1 = translatedMove[0];
		int spalteMove1 = translatedMove[1];
		int zeileMove2 = translatedMove[2];
		int spalteMove2 = translatedMove[3];
		
		// sonderfall
		if(Math.abs(spalteMove1 - spalteMove2) + Math.abs(zeileMove1 - zeileMove2) == 1)
			return false;
		if(!checkCannonTakeVertical(board, translatedMove))
			return false;
		if(!checkCannonTakeHorizontal(board, translatedMove))
			return false;

		return true;
	}

	public boolean checkCannonTakeVertical(char[][] board, int[] translatedMove){
		int zeileMove1 = translatedMove[0];
		int spalteMove1 = translatedMove[1];
		int zeileMove2 = translatedMove[2];
//		int spalteMove2 = translatedMove[3];
		//check above
		if (zeileMove1 - zeileMove2 > 1) {
			int steps = zeileMove1 - zeileMove2;
			int counter = 0;
			for (int i = 1; i < steps; i++) {
				if (Character.isAlphabetic(board[zeileMove1 - i][spalteMove1]))
					counter = counter + 1;
			}
			if(counter != 1)
				return false;
		}
		//check below
		else if (zeileMove1 - zeileMove2 < 1) {
			int steps = Math.abs(zeileMove1 - zeileMove2);
			int counter = 0;
			for (int i = 1; i < steps; i++) {
				if (Character.isAlphabetic(board[zeileMove1 + i][spalteMove1]))
					counter = counter + 1;
			}
			if(counter != 1 && zeileMove1 - zeileMove2 !=0)
				return false;
		}
		return true;
	}

	public boolean checkCannonTakeHorizontal(char[][] board, int[] translatedMove){
		int zeileMove1 = translatedMove[0];
		int spalteMove1 = translatedMove[1];
//		int zeileMove2 = translatedMove[2];
		int spalteMove2 = translatedMove[3];
		//check above
		if (spalteMove1 - spalteMove2 > 1) {
			int steps = spalteMove1 - spalteMove2;
			int counter = 0;
			for (int i = 1; i < steps; i++) {
				if (Character.isAlphabetic(board[zeileMove1][spalteMove1 - i]))
					counter = counter + 1;
			}
			if(counter != 1)
				return false;
		}
		//check below
		else if (spalteMove1 - spalteMove2 < 1) {
			int steps = Math.abs(spalteMove1 - spalteMove2);
			int counter = 0;
			for (int i = 1; i < steps; i++) {
				if (Character.isAlphabetic(board[zeileMove1][spalteMove1 + i]))
					counter = counter + 1;
			}
			if(counter != 1 && spalteMove1 - spalteMove2 !=0)
				return false;
		}
		return true;
	}

	public boolean checkSoldier(int[] translatedMove, Player player){
		
		if(player == this.redPlayer)
			if(!checkSoldierRed(translatedMove, player)) 
				return false;
		else if(player == this.blackPlayer)
			if(!checkSoldierBlack(translatedMove, player)) 
				return false;

		return true;
	}

	public boolean checkSoldierRed(int[] translatedMove, Player player){
		String redZeile = "56789";
		//soldier can only move forward
		if((translatedMove[0] - translatedMove[2]) != 1 || translatedMove[3] - translatedMove[1] != 0)
				return false;
		//if in enemy territory, soldier can move one step left or right
		if(!redZeile.contains(String.valueOf(translatedMove[0]))){
			if((translatedMove[0] - translatedMove[2]) != 0 || Math.abs(translatedMove[3] - translatedMove[1]) != 1)
				return false;
		}
		return true;
	}

	public boolean checkSoldierBlack(int[] translatedMove, Player player){
		String blackZeile = "01234";
		//soldier can only move forward
		if((translatedMove[0] - translatedMove[2]) != -1 && translatedMove[3] - translatedMove[1] != 0)
				return false;
		//if in enemy territory, soldier can move one step left or right
		if(!blackZeile.contains(String.valueOf(translatedMove[0]))){
			if((translatedMove[0] - translatedMove[2]) != 0 || Math.abs(translatedMove[3] - translatedMove[1]) != 1)
				return false;
		}
		return true;
	}


}