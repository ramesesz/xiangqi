package de.tuberlin.sese.swtpp.gameserver.test.xiangqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import de.tuberlin.sese.swtpp.gameserver.model.xiangqi.XiangqiGame;

public final class CustomTest {
    
    XiangqiGame xiangqi = new XiangqiGame();
    String startFEN = "rheagaehr/9/1c5c1/s1s1s1s1s/9/9/S1S1S1S1S/1C5C1/9/RHEAGAEHR";
    
    public void printBoardArr(char[][] boardArr) {
    	for(int i=0;i<boardArr.length;i++) {
    		for(int j=0;j<boardArr[i].length;j++) {
    			System.out.print(boardArr[i][j]+" ");
    		}
    		System.out.println("");
    	}
    }
    
    public void printBoard(String board) {
    	printBoardArr(xiangqi.FENtoBoard(board));
    }
    
    @Test
    public void testTranslatedMove() {
        int[] move = xiangqi.getTranslatedMove("a0-a2");
        int[] expectedMove = {9,0,7,0};
//        System.out.println(Arrays.toString(move));
        assertTrue(Arrays.equals(move,expectedMove));
    }
    
    @Test
    public void testGetBoard() {
    	String board = xiangqi.getBoard();
//    	assertEquals(board,"rheagaehr/9/1c5c1/s1s1s1s1s/9/9/S1S1S1S1S/1C5C1/9/RHEAGAEHR");
    	char[][] boardArr = xiangqi.FENtoBoard(board);
    	String newBoard = xiangqi.boardToFEN(boardArr);
    	assertEquals(board, newBoard);
//    	System.out.println(newBoard);
    }
    
    @Test
    public void testMove() {
    	xiangqi.setBoard(startFEN);
    	xiangqi.tryMove("a0-a2", xiangqi.getNextPlayer());
//    	printBoard(xiangqi.getBoard());
    }
    
    @Test
    public void testMoveInBoard() {
    	assertTrue(xiangqi.moveInBoard("a0-a2"));
    	assertFalse(xiangqi.moveInBoard("z9-a2"));
    }
    
    public void testGetChar() {
        
    }

}