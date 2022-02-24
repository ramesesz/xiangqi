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
		// check if move is valid
		if(!checkMove(moveString, player)) return false;
		// do the move
		String newBoard = doMove(moveString, player);
		// check if player is still checked
		if(isCheck(player,newBoard)) return false;
		// check if todesblick
		if(isTodesBlick(newBoard)) return false;
		// set the new board
		else setBoard(newBoard);
		// set next player
		this.setNextPlayer(player == redPlayer ? blackPlayer : redPlayer);
		// add to history
		this.history.add(new Move(moveString,getBoard(),player));
		// check if someone won
		ArrayList<String> validMoves = validMoves(player==redPlayer?blackPlayer:redPlayer, FENtoBoard(newBoard));
		if (validMoves.size() == 0){
			regularGameEnd(player);
		} 
		//if(isCheckmate(player==redPlayer?blackPlayer:redPlayer, newBoard)) regularGameEnd(player);		return true;
		return true;
	}
	
	public String doMove(String moveString, Player player) {
		char[][] board = FENtoBoard(getBoard());
		int[] move = getTranslatedMove(moveString);
		char startFigur = board[move[0]][move[1]];
		// char zielFigur = board[move[2]][move[3]];
		// You can't directly kiill the general
		//if (Character.toLowerCase(zielFigur)=='g') return "cannot eat general!";
		board[move[0]][move[1]] = ' ';
		board[move[2]][move[3]] = startFigur;
		// if(isTodesBlick(board)) return "";
		String newBoard = boardToFEN(board);
		return newBoard;
	}

	public boolean checkMove(String moveString, Player player, char[][] board){
		if(!moveInBoard(moveString)) return false;
		char[][] boardArr = board;
		int[] move = getTranslatedMove(moveString);
		if(!startZielIsValid(boardArr, move, player)) return false;
		if(!checkFigur(move, boardArr , player)) return false;
		return true;
	}
	
	public boolean checkMove(String moveString, Player player, String board) {
		return checkMove(moveString, player, FENtoBoard(board));
	}

	public boolean checkMove(String moveString, Player player){
		return checkMove(moveString, player, getBoard());
	}

	public boolean startZielIsValid(char[][] board, int[] translatedMove, Player player){
		int zeileMove1 = translatedMove[0];
		int spalteMove1 = translatedMove[1];
		int zeileMove2 = translatedMove[2];
		int spalteMove2 = translatedMove[3];
		//same start and destination?
		if(spalteMove1 == spalteMove2 && zeileMove1 == zeileMove2) 
			return false;
		//piece to be moved belongs to enemy?
		if(!Character.isAlphabetic(board[zeileMove1][spalteMove1]) 
		|| player == this.redPlayer && Character.isLowerCase(board[zeileMove1][spalteMove1]) 
		|| player == this.blackPlayer && Character.isUpperCase(board[zeileMove1][spalteMove1])) 
			return false;
		//piece at dest belongs to player?
		if(player == this.redPlayer && Character.isUpperCase(board[zeileMove2][spalteMove2])
		|| player == this.blackPlayer && Character.isLowerCase(board[zeileMove2][spalteMove2]))
			return false;

		return true;
	}

	//Equivalent to checkMoveFormat()
	public boolean moveInBoard(String moveString){
		//checks whether move is within board limits
		String validZeile = "0123456789";
		String validSpalte = "abcdefghij";
		
		char[] move = moveString.toCharArray();
		
		if(moveString.length() !=5 || move[2] != '-') return false;
		
		if(!validSpalte.contains(String.valueOf(move[0])) || !validZeile.contains(String.valueOf(move[1])) 
		|| !validSpalte.contains(String.valueOf(move[3])) || !validZeile.contains(String.valueOf(move[4]))) return false;

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
	
	public ArrayList<String> validMoves(Player player, char[][] board, String[] figuren, int count) {
		String validSpalte = "abcdefghij";
		ArrayList<String> moveList = new ArrayList<String>();
		int counter = count;
		
		for(int n=0;n<counter;n++) {
			for(int i=0;i<10;i++) {
				for(int j=0;j<9;j++) {
					String moveTo=validSpalte.charAt(j)+Integer.toString(9-i);
					String moveString=figuren[n]+"-"+moveTo;
					if (checkMove(moveString, player, board)) {
						String newBoard = doMove(moveString, player);
						if(isTodesBlick(newBoard)) continue;
						if(!isCheck(player, newBoard)) {
							moveList.add(moveString);
						}
					} 
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
		//sammeln figuren
		for(int i=0;i<10;i++) {
			for(int j=0;j<9;j++) {
				char figur = board[i][j];
				if((isRedPlayer && Character.isUpperCase(figur)) || (!isRedPlayer && Character.isLowerCase(figur))) {
					figuren[counter]=validSpalte.charAt(j)+Integer.toString(9-i);
					counter++;
				}
			}
		}
		
		return validMoves(player, board, figuren, counter);
	}
	
	public ArrayList<String> validMoves(Player player) {
		return validMoves(player, FENtoBoard(getBoard()));
	}
	
	public boolean isTodesBlick(char[][] board){
		//z.b e0e9
		char[] redBlackCoord = (getGeneralCoordinate(this.redPlayer) + getGeneralCoordinate(this.blackPlayer)).toCharArray();
		int zeileMove1 = 9 - Character.getNumericValue(redBlackCoord[1]);
		int spalteMove1 = spalteMove(redBlackCoord[0]);
		int zeileMove2 = 9 - Character.getNumericValue(redBlackCoord[3]);
		int spalteMove2 = spalteMove(redBlackCoord[2]);

		//if in the same column
		if(spalteMove1 == spalteMove2){
			int start = Math.min(zeileMove1, zeileMove2);
			int end = Math.max(zeileMove1, zeileMove2);
			for (int i = start+1; i < end; i++) {
				if (Character.isAlphabetic(board[i][spalteMove1]))
					return false;
			}
		} else {
			return false;
		}

		return true;
	}

	public boolean isTodesBlick(String board){
		return isTodesBlick(FENtoBoard(board));
	}
	
	public String getGeneralCoordinate(Player player, char[][] board) {
		String validSpalte = "abcdefghij";
		boolean isRedPlayer = player == redPlayer;
		for(int i=0;i<10;i++) {
			for(int j=0;j<9;j++) {
				char figur = board[i][j];
				if((isRedPlayer && figur == 'G') || (!isRedPlayer && figur == 'g')) {
					return validSpalte.charAt(j)+Integer.toString(9-i);
				}
			}
		}
		return "";
	}

	public String getGeneralCoordinate(Player player, String board) {
		return getGeneralCoordinate(player, FENtoBoard(board));
	}

	public String getGeneralCoordinate(Player player) {
		return getGeneralCoordinate(player, getBoard());
	}

	public boolean isCheck(Player player, char[][] board) {
		String[] figuren = new String[16];
		String validSpalte = "abcdefghij";
		int counter = 0;
		boolean isRedPlayer = player == redPlayer;
		Player oppositePlayer = player == redPlayer ? blackPlayer : redPlayer;
		
		for(int i=0;i<10;i++) {
			for(int j=0;j<9;j++) {
				char figur = board[i][j];
				if((!isRedPlayer && Character.isUpperCase(figur)) || (isRedPlayer && Character.isLowerCase(figur))) {
					figuren[counter]=validSpalte.charAt(j)+Integer.toString(9-i);
					counter++;
				}
			}
		}
		
		for(int n=0;n<counter;n++) {
			String moveString = figuren[n]+"-"+getGeneralCoordinate(player, board);
			if (checkMove(moveString, oppositePlayer, board)) {
				String newBoard = doMove(moveString, oppositePlayer);
				if(isTodesBlick(newBoard)) continue;
				return true;
			}
		}
		return false;
	}

	public boolean isCheck(Player player, String board) {
		return isCheck(player, FENtoBoard(board));
	}

	public boolean isCheck(Player player) {
		return isCheck(player, FENtoBoard(getBoard()));
	}

	public boolean isCheckmate(Player player, char[][] board) {
		ArrayList<String> validMoves = validMoves(player, board);
		if (validMoves.size() == 0) return true;
		return false;
	}

	public boolean isCheckmate(Player player, String board) {
		return isCheckmate(player, FENtoBoard(board));
	}

	public boolean isCheckmate(Player player) {
		return isCheckmate(player, getBoard());
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
				if (!checkElephant(board, translatedMove, player))
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

	public boolean checkElephant(char board[][], int[] translatedMove, Player player){
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
		//is there intervening peice?
		int interveneZeile = (zeileMove1 + zeileMove2)/2;
		int interveneSpalte = (spalteMove1 + spalteMove2)/2;

		if(Character.isAlphabetic(board[interveneZeile][interveneSpalte]))
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
		if(translatedMove[2]-translatedMove[0] !=0 && translatedMove[3]-translatedMove[1] !=0) 
			return false;
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
		//int spalteMove2 = translatedMove[3];
		
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
		//int zeileMove2 = translatedMove[2];
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
		if(translatedMove[2]-translatedMove[0] !=0 && translatedMove[3]-translatedMove[1] !=0) 
			return false;
		
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
		//int spalteMove2 = translatedMove[3];
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
		//int zeileMove2 = translatedMove[2];
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
		//can only move vertical or horizontal
		if(translatedMove[2]-translatedMove[0] !=0 && translatedMove[3]-translatedMove[1] !=0) 
			return false;
		if(player == this.redPlayer)
			if(!checkSoldierRed(translatedMove, player)) 
				return false;
		if(player == this.blackPlayer)
			if(!checkSoldierBlack(translatedMove, player)) 
				return false;

		return true;
	}

	public boolean checkSoldierRed(int[] translatedMove, Player player){
		int zeileMove1 = translatedMove[0];
		int spalteMove1 = translatedMove[1];
		int zeileMove2 = translatedMove[2];
		int spalteMove2 = translatedMove[3];
		String redZeile = "56789";
		
		//if in own territory
		if(redZeile.contains(String.valueOf(zeileMove1))){
			//soldier can only move forward 1 step
			if((zeileMove1 - zeileMove2) != 1)
				return false;
		}
		//if in enemy territory
		else{
			//move exactly one space
			if(Math.abs(zeileMove1 - zeileMove2) + Math.abs(spalteMove1 - spalteMove2) != 1)
				return false; 
			//cannot move backwards
			if((zeileMove1 - zeileMove2) == -1)
				return false;
		}
		return true;
	}

	public boolean checkSoldierBlack(int[] translatedMove, Player player){
		int zeileMove1 = translatedMove[0];
		int spalteMove1 = translatedMove[1];
		int zeileMove2 = translatedMove[2];
		int spalteMove2 = translatedMove[3];
		String blackZeile = "01234";
		//if in own territory
		if(blackZeile.contains(String.valueOf(zeileMove1))){
			//soldier can only move forward 1 step
			if((zeileMove2 - zeileMove1) != 1)
				return false;
		}
		//if in enemy territory
		else{
			//move exactly one space
			if(Math.abs(zeileMove1 - zeileMove2) + Math.abs(spalteMove1 - spalteMove2) != 1)
				return false; 
			//cannot move backwards
			if((zeileMove1 - zeileMove2) == 1)
				return false;
		}
		return true;
	}


}