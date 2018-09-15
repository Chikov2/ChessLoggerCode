package com.epitel.chesslogger.chesslogger;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class ChessBoard extends View {
    int top = 0;
    int bottom = 0;
    int left = 0;
    int right = 0;
    int prevX = -1;
    int prevY = -1;
    int sizeCellX = 0;
    int sizeCellY = 0;
    int offset = 10;

    boolean wCCSc = false;
    boolean wCCLc = false;
    boolean bCCSc = false;
    boolean bCCLc = false;
    boolean whiteToMove = true;

    Pair<Integer, Integer> whiteKingCoords = new Pair<>(7, 4);
    Pair<Integer, Integer> blackKingCoords = new Pair<>(0, 4);

    String[][] testBoard = new String[8][8];

    Bitmap blackKing = BitmapFactory.decodeResource(getResources(), R.drawable.blackking);
    Bitmap whiteKing = BitmapFactory.decodeResource(getResources(), R.drawable.whiteking);
    Bitmap blackRook = BitmapFactory.decodeResource(getResources(), R.drawable.blackrook);
    Bitmap whiteRook = BitmapFactory.decodeResource(getResources(), R.drawable.whiterook);
    Bitmap blackBishop = BitmapFactory.decodeResource(getResources(), R.drawable.blackbishop);
    Bitmap whiteBishop = BitmapFactory.decodeResource(getResources(), R.drawable.whitebishop);
    Bitmap blackQueen = BitmapFactory.decodeResource(getResources(), R.drawable.blackqueen);
    Bitmap whiteQueen = BitmapFactory.decodeResource(getResources(), R.drawable.whitequeen);
    Bitmap blackKnight = BitmapFactory.decodeResource(getResources(), R.drawable.blackknight);
    Bitmap whiteKnight = BitmapFactory.decodeResource(getResources(), R.drawable.whiteknight);
    Bitmap blackPawn = BitmapFactory.decodeResource(getResources(), R.drawable.blackpawn);
    Bitmap whitePawn = BitmapFactory.decodeResource(getResources(), R.drawable.whitepawn);

    public ChessBoard(Context context) {
        super(context);
        try {

            InitializeBoard();
            setBackground(getResources().getDrawable(R.drawable.chess_board));
        } catch(Exception ex) {
            new SendErrorsTask().execute("", "ChessBoard.java ChessBoard(): " + ex.getMessage(), "ERROR");
        }
    }

    public void modifyOnQueening(int newX, int newY, String movingPiece)
    {
        MainActivity.board[newY][newX] = movingPiece;
        Pair<Integer, Integer> coords;

        if (movingPiece.charAt(0) == 'b')
        {
            String move = MainActivity.blackMoves.get(MainActivity.blackMoves.size() - 1).notation;
            move += "=" + movingPiece.charAt(1);
            move = move.replace("+","");
            coords = whiteKingCoords;

            if (inCheck(MainActivity.board, coords, 'w'))
                move += '+';

            MainActivity.blackMoves.get(MainActivity.blackMoves.size() - 1).notation = move;
        }
        else
        {
            String move = MainActivity.whiteMoves.get(MainActivity.whiteMoves.size() - 1).notation;
            move += "=" + movingPiece.charAt(1);
            move = move.replace("+","");
            coords = blackKingCoords;
            if (inCheck(MainActivity.board, coords, 'b'))
                move += '+';

            MainActivity.whiteMoves.get(MainActivity.whiteMoves.size() - 1).notation = move;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
            top = this.getTop();
            bottom = this.getBottom();
            left = this.getLeft();
            right = this.getRight();

            sizeCellX = (right - left) / 8;
            sizeCellY = (bottom - top) / 8;

            //Log.e("SizeX", Integer.toString(sizeCellX));
            //Log.e("SizeY", Integer.toString(sizeCellX));

            DrawBoard(canvas);
        } catch(Exception ex) {
            new SendErrorsTask().execute("", "ChessBoard.java onDraw(): " + ex.getMessage(), "ERROR");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        MainActivity.result = "";
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            prevX = (int) event.getX();
            prevY = (int) event.getY();

            if (prevX < left && prevX > right && prevY > bottom && prevY < top) {
                prevY = -1;
                prevX = -1;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP && prevX != -1 && prevY != -1) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            Log.e("X", Integer.toString(x));
            Log.e("Y", Integer.toString(y));
            Log.e("top", Integer.toString(top));
            Log.e("bottom", Integer.toString(bottom));

            Pair<Integer, Integer> newC = getCoords(x, y);
            Pair<Integer, Integer> oldC = getCoords(prevX, prevY);

            makeMove(newC, oldC);
        }
        // tell the View to redraw the Canvas
        invalidate();

        // tell the View that we handled the event
        return true;
    }

    private Pair<Integer, Integer> getCoords(int X, int Y) {
        return new Pair<>((Y - top) / sizeCellY, (X - left) / sizeCellX);
    }

    private void makeMove(Pair<Integer, Integer> newC, Pair<Integer, Integer> oldC) {
        try {
            if (!isValidMove(newC, oldC)) {
                Activity activity = (Activity) this.getContext();
                MediaPlayer mp = MediaPlayer.create(activity, R.raw.error);
                mp.start();

                return;
            }

            whiteToMove = !whiteToMove;
        } catch(Exception ex) {
            new SendErrorsTask().execute("", "ChessBoard.java makeMove(): " + ex.getMessage(), "ERROR");
        }
    }

    private char possibleOtherPiece(String[][] boardX, Pair<Integer,Integer> oldC, Pair<Integer,Integer> newC, String movingPiece)
    {
        String[][] boardT = new String[8][8];

        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                boardT[i][j] = boardX[i][j];

        boardT[oldC.first][oldC.second] = movingPiece;
        boardT[newC.first][newC.second] = "";

        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (boardT[i][j].equals(movingPiece) && (i != oldC.first || j != oldC.second)) {
                    Pair<Integer, Integer> coords = new Pair<>(i, j);
                    switch (boardT[i][j].charAt(1)) {
                        case 'N':
                            if (KnightLogic(newC, coords))
                                if (j != oldC.second)
                                    return (char)(oldC.second + 'a');
                                else return (char)(oldC.first + '1');
                            break;
                        case 'B':
                            if (BishopLogic(boardT, newC, coords))
                                if (j != oldC.second)
                                    return (char)(oldC.second + 'a');
                                else return (char)(oldC.first + '1');
                            break;
                        case 'R':
                            if (RookLogic(boardT, newC, coords))
                                if (j != oldC.second)
                                    return (char)(oldC.second + 'a');
                                else return (char)(oldC.first + '1');
                            break;
                        case 'P':
                            if (PawnCaptureLogic(boardT, newC, coords, movingPiece.charAt(0)) || PawnRegularLogic(boardT, newC, coords, movingPiece.charAt(0)))
                                if (j != oldC.second)
                                    return (char)(oldC.second + 'a');
                                else return (char)(oldC.first + '1');
                            break;
                    }
                }

        return 'x';
    }

    private boolean isValidMove(Pair<Integer, Integer> newC, Pair<Integer, Integer> oldC) {
        wCCLc = false;
        wCCSc = false;
        bCCLc = false;
        bCCSc = false;

        String special = "";
        String pieceCaptured = "";

        //System.arraycopy(MainActivity.board, 0, testBoard, 0, 8);
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                testBoard[i][j] = MainActivity.board[i][j];

        String movingPiece = MainActivity.board[oldC.first][oldC.second];

        // starting position same as ending
        if (oldC.first == newC.first && oldC.second == newC.second) return false;

        // no piece selected
        if (movingPiece.length() == 0) return false;

        //wrong color piece
        if (movingPiece.charAt(0) == 'b' && whiteToMove) return false;
        if (movingPiece.charAt(0) == 'w' && !whiteToMove) return false;

        //trying to eat your own piece
        if (!MainActivity.board[newC.first][newC.second].equals("") && MainActivity.board[newC.first][newC.second].charAt(0) == movingPiece.charAt(0))
            return false;

        boolean validMove = false;

        switch (movingPiece.charAt(1)) {
            case 'N':
                if (KnightLogic(newC, oldC)) validMove = true;
                break;
            case 'B':
                if (BishopLogic(MainActivity.board, newC, oldC)) validMove = true;
                break;
            case 'R':
                if (RookLogic(MainActivity.board, newC, oldC)) validMove = true;
                break;
            case 'Q':
                if (RookLogic(MainActivity.board, newC, oldC) || BishopLogic(MainActivity.board, newC, oldC)) validMove = true;
                break;
            case 'P':
                if (PawnRegularLogic(MainActivity.board, newC, oldC, movingPiece.charAt(0)) || PawnCaptureLogic(MainActivity.board, newC, oldC, movingPiece.charAt(0)))
                    validMove = true;
                break;
            case 'K':
                if (KingLogic(newC, oldC, movingPiece.charAt(0))) validMove = true;
                break;
        }

        if (validMove) {
            testBoard[newC.first][newC.second] = movingPiece;
            testBoard[oldC.first][oldC.second] = "";
        } else {
            // castling
            if (movingPiece.equals("wK")) {
                if (MainActivity.whiteCanCastleShort && newC.first == 7 && newC.second == 6
                        && !inCheck(testBoard, new Pair(7, 4), 'w')
                        && !inCheck(testBoard, new Pair(7, 5), 'w')
                        && testBoard[7][5].equals("") && testBoard[7][6].equals("")) {
                    MainActivity.whiteCanCastleShort = false;
                    MainActivity.whiteCanCastleLong = false;
                    wCCSc = true;
                    wCCLc = true;
                    testBoard[7][6] = "wK";
                    testBoard[7][5] = "wR";
                    testBoard[7][7] = "";
                    testBoard[7][4] = "";
                    validMove = true;
                    whiteKingCoords = newC;
                    special = "0-0";
                } else if (MainActivity.whiteCanCastleLong && newC.first == 7 && newC.second == 2
                        && !inCheck(testBoard, new Pair(7, 3), 'w')
                        && !inCheck(testBoard, new Pair(7, 4), 'w')
                        && testBoard[7][1].equals("") && testBoard[7][2].equals("") && testBoard[7][3].equals("")) {
                    MainActivity.whiteCanCastleShort = false;
                    MainActivity.whiteCanCastleLong = false;
                    wCCSc = true;
                    wCCLc = true;
                    testBoard[7][2] = "wK";
                    testBoard[7][3] = "wR";
                    testBoard[7][4] = "";
                    testBoard[7][0] = "";
                    testBoard[7][1] = "";
                    validMove = true;
                    whiteKingCoords = newC;
                    special = "0-0-0";
                }
            } else if (movingPiece.equals("bK")) {
                if (MainActivity.blackCanCastleShort && newC.first == 0 && newC.second == 6
                        && !inCheck(testBoard, new Pair(0, 4), 'b')
                        && !inCheck(testBoard, new Pair(0, 5), 'b')
                        && testBoard[0][5].equals("") && testBoard[0][6].equals("")) {
                    MainActivity.blackCanCastleShort = false;
                    MainActivity.blackCanCastleLong = false;
                    bCCSc = true;
                    bCCLc = true;
                    testBoard[0][6] = "bK";
                    testBoard[0][5] = "bR";
                    testBoard[0][7] = "";
                    testBoard[0][4] = "";
                    validMove = true;
                    blackKingCoords = newC;
                    special = "0-0";
                } else if (MainActivity.blackCanCastleLong && newC.first == 0 && newC.second == 2
                        && !inCheck(testBoard, new Pair(0, 3), 'b')
                        && !inCheck(testBoard, new Pair(0, 4), 'b')
                        && testBoard[0][1].equals("") && testBoard[0][2].equals("") && testBoard[0][3].equals("")) {
                    MainActivity.blackCanCastleShort = false;
                    MainActivity.blackCanCastleLong = false;
                    bCCSc = true;
                    bCCLc = true;
                    testBoard[0][2] = "bK";
                    testBoard[0][3] = "bR";
                    testBoard[0][0] = "";
                    testBoard[0][1] = "";
                    testBoard[0][4] = "";
                    validMove = true;
                    blackKingCoords = newC;
                    special = "0-0-0";
                }
            }

            //en-passant;
            if (movingPiece.charAt(1) == 'P') {
                validMove = PawnEnPassantLogic(newC, oldC, movingPiece.charAt(0));
                if (validMove)
                {
                    testBoard[oldC.first][oldC.second] = "";
                    testBoard[newC.first][newC.second] = movingPiece;
                    if (movingPiece.charAt(0) == 'b')
                        testBoard[newC.first - 1][newC.second] = "";
                    else testBoard[newC.first + 1][newC.second] = "";
                    special = "en-passant";
                }
            }
        }

        if (!validMove) return false;

        //region did the last move affect castling
        if (movingPiece.charAt(1) == 'R') {
            if (movingPiece.charAt(0) == 'w') {
                if (MainActivity.whiteCanCastleShort && oldC.first == 7 && oldC.second == 0) {
                    MainActivity.whiteCanCastleShort = false;
                    wCCSc = true;
                }
                if (MainActivity.whiteCanCastleLong && oldC.first == 7 && oldC.second == 7) {
                    MainActivity.whiteCanCastleLong = false;
                    wCCLc = true;
                }
            } else {
                if (MainActivity.blackCanCastleShort && oldC.first == 0 && oldC.second == 0) {
                    MainActivity.blackCanCastleShort = false;
                    bCCSc = true;
                }
                if (MainActivity.blackCanCastleLong && oldC.first == 0 && oldC.second == 7) {
                    MainActivity.blackCanCastleLong = false;
                    bCCLc = true;
                }
            }
        }

        if (movingPiece.charAt(1) == 'K') {
            if (movingPiece.charAt(0) == 'w') {
                if (MainActivity.whiteCanCastleShort) {
                    MainActivity.whiteCanCastleShort = false;
                    wCCSc = true;
                }
                if (MainActivity.whiteCanCastleLong) {
                    MainActivity.whiteCanCastleLong = false;
                    wCCLc = true;
                }
            } else {
                if (MainActivity.blackCanCastleShort) {
                    MainActivity.blackCanCastleShort = false;
                    bCCSc = true;
                }
                if (MainActivity.blackCanCastleLong) {
                    MainActivity.blackCanCastleLong = false;
                    bCCLc = true;
                }
            }
        }
        //endregion


        Log.e("Starting", Integer.toString(oldC.first) + "," + Integer.toString(oldC.second));
        Log.e("Ending", Integer.toString(newC.first) + "," + Integer.toString(newC.second));

        Pair<Integer, Integer> coords = whiteKingCoords;
        if (movingPiece.charAt(0) == 'b') coords = blackKingCoords;

        if (inCheck(testBoard, coords, movingPiece.charAt(0))) {
            Log.e("ERROR: IN CHECK", "ERROR: IN CHECK");

            if (movingPiece.equals("wK"))
                whiteKingCoords = oldC;
            else if (movingPiece.equals("bK"))
                blackKingCoords = oldC;

            if (wCCLc) {
                MainActivity.whiteCanCastleLong = true;
                wCCLc = false;
            }
            if (wCCSc) {
                MainActivity.whiteCanCastleShort = true;
                wCCSc = false;
            }
            if (bCCLc) {
                MainActivity.blackCanCastleLong = true;
                bCCLc = false;
            }

            if (bCCSc) {
                MainActivity.blackCanCastleShort = true;
                bCCSc = false;
            }

            return false;
        }

        if (("PNBR").indexOf(movingPiece.charAt(1)) != -1) {
            char toAdd = possibleOtherPiece(testBoard, oldC, newC, movingPiece);
            if (toAdd != 'x')
                special += toAdd;
        }

        //region promotion
        boolean queening = false;
        if (movingPiece.equals("wP") && newC.first == 0) {
            Activity activity = (Activity) this.getContext();
            PromotionClass popup = new PromotionClass(activity, "w", newC.second, newC.first); //(Activity a, String newPlayer, int newX, int newY)
            popup.setCanceledOnTouchOutside(false);
            popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popup.show();
            queening = true;
        } else if (movingPiece.equals("bP") && newC.first == 7) {
            Activity activity = (Activity) this.getContext();
            PromotionClass popup = new PromotionClass(activity, "b", newC.second, newC.first);
            popup.setCanceledOnTouchOutside(false);
            popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popup.show();
            queening = true;
        }
        //endregion

        if (MainActivity.board[newC.first][newC.second].length() > 1)
            pieceCaptured += MainActivity.board[newC.first][newC.second].charAt(1);

        Move newMove = new Move();
        newMove.beginPos = new Pair<>(oldC.first, oldC.second);
        newMove.endPos = new Pair<>(newC.first, newC.second);
        newMove.special = special;
        newMove.pieceCaptured = pieceCaptured;
        newMove.piece = movingPiece.charAt(1);

        newMove.generateNotation();

        coords = blackKingCoords;
        if (movingPiece.charAt(0) == 'b') {
            coords = whiteKingCoords;
            if (!queening && inCheck(testBoard, coords, 'w'))
                newMove.notation += '+';
            if (bCCSc) newMove.shortCastleChanged = true;
            if (bCCLc) newMove.longCastleChanged = true;
            MainActivity.blackMoves.add(newMove);

        } else {
            if (!queening && inCheck(testBoard, coords, 'b'))
                newMove.notation += '+';
            if (wCCSc) newMove.shortCastleChanged = true;
            if (wCCLc) newMove.longCastleChanged = true;
            MainActivity.whiteMoves.add(newMove);
        }

        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                MainActivity.board[i][j] = testBoard[i][j];
        //System.arraycopy(testBoard, 0, MainActivity.board, 0, 8);

        return true;
    }

    private boolean inCheck(String[][] boardT, Pair<Integer, Integer> coords, char movingPieceColor)
    {
        char capturePieceColor = 'w';
        if (movingPieceColor == 'w') capturePieceColor = 'b';

        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (!boardT[i][j].equals("") && boardT[i][j].charAt(0) != movingPieceColor) {
                    Pair<Integer, Integer> oldC = new Pair<>(i, j);
                    switch (boardT[i][j].charAt(1)) {
                        case 'N':
                            if (KnightLogic(coords, oldC))
                                return true;
                            break;
                        case 'B':
                            if (BishopLogic(boardT, coords, oldC))
                                return true;
                            break;
                        case 'R':
                            if (RookLogic(boardT, coords, oldC))
                                return true;
                            break;
                        case 'Q':
                            if (BishopLogic(boardT, coords, oldC) || RookLogic(boardT, coords, oldC))
                                return true;
                            break;
                        case 'P':
                            if (PawnCaptureLogic(boardT, coords, oldC, capturePieceColor))
                                return true;
                            break;
                    }
                }

        return false;
    }

    private boolean KingLogic(Pair<Integer,Integer> newC, Pair<Integer, Integer> oldC, char movingPieceColor) {
        boolean validMove = false;
        if (Math.abs(newC.first - oldC.first) <= 1 && Math.abs(newC.second - oldC.second) <= 1) {
            validMove = true;
        }

        if (validMove) {
            if (movingPieceColor == 'w')
                whiteKingCoords = newC;
            else if (movingPieceColor == 'b')
                blackKingCoords = newC;
        }

        return validMove;
    }

    private boolean PawnCaptureLogic(String[][] boardT, Pair<Integer,Integer> newC, Pair<Integer, Integer> oldC, char movingPieceColor) {
        if ((movingPieceColor == 'b') && (oldC.first + 1 == newC.first && (oldC.second + 1 == newC.second || oldC.second - 1 == newC.second)
                    && !boardT[newC.first][newC.second].equals("") && boardT[newC.first][newC.second].charAt(0) == 'w'))
                return true;
        else if ((movingPieceColor == 'w') && (oldC.first - 1 == newC.first && (oldC.second + 1 == newC.second || oldC.second - 1 == newC.second)
                    && !boardT[newC.first][newC.second].equals("") && boardT[newC.first][newC.second].charAt(0) == 'b'))
                return true;

        return false;
    }

    private boolean PawnEnPassantLogic(Pair<Integer,Integer> newC, Pair<Integer, Integer> oldC, char movingPieceColor)
    {
        if (movingPieceColor == 'b')
        {
           Move lastMove = MainActivity.whiteMoves.get(MainActivity.whiteMoves.size() - 1);
           if (lastMove.piece != 'P') return false;
           if (lastMove.beginPos.first != 6 || lastMove.endPos.first != 4) return false;
           if (oldC.first != 4 || newC.first != 5) return false;
           if (oldC.second + 1 == newC.second && newC.second == lastMove.endPos.second) return true;
           if (oldC.second - 1 == newC.second && newC.second == lastMove.endPos.second) return true;
        }
        else
        {
            Move lastMove = MainActivity.blackMoves.get(MainActivity.blackMoves.size() - 1);
            if (lastMove.piece != 'P') return false;
            if (lastMove.beginPos.first != 1 || lastMove.endPos.first != 3) return false;
            if (oldC.first != 3 || newC.first != 2) return false;
            if (oldC.second + 1 == newC.second && newC.second == lastMove.endPos.second) return true;
            if (oldC.second - 1 == newC.second && newC.second == lastMove.endPos.second) return true;
        }

        return false;
    }

    private boolean PawnRegularLogic(String[][] boardT, Pair<Integer,Integer> newC, Pair<Integer, Integer> oldC, char movingPieceColor) {
        if (movingPieceColor == 'b')
        {
            //regular 1 square move
            if (oldC.first + 1 == newC.first && oldC.second == newC.second && boardT[newC.first][newC.second].equals(""))
                return true;

            //first 2 square move
            if (oldC.first + 2 == newC.first && oldC.second == newC.second && oldC.first == 1
                    && boardT[newC.first][newC.second].equals("") && boardT[oldC.first + 1][oldC.second].equals(""))
                return true;
        }
        else
        {
            //regular 1 square move
            if (oldC.first - 1 == newC.first && oldC.second == newC.second && boardT[newC.first][newC.second].equals(""))
                return true;

            //first 2 square move
            if (oldC.first - 2 == newC.first && oldC.second == newC.second && oldC.first == 6
                    && boardT[newC.first][newC.second].equals("") && boardT[oldC.first - 1][oldC.second].equals(""))
                return true;
        }

        return false;
    }

    private boolean RookLogic(String[][] boardT, Pair<Integer,Integer> newC, Pair<Integer, Integer> oldC) {
        if (oldC.first != newC.first && oldC.second != newC.second) return false;

        int smallerY = Math.min(newC.first, oldC.first);
        int smallerX = Math.min(newC.second, oldC.second);

        for (int i = 1; i < Math.max(newC.first, oldC.first) - smallerY; i++)
            if (!boardT[smallerY + i][newC.second].equals("")) return false;
        for (int i = 1; i < Math.max(newC.second, oldC.second) - smallerX; i++)
            if (!boardT[newC.first][smallerX + i].equals("")) return false;

        return true;
    }

    private boolean BishopLogic(String[][] boardT, Pair<Integer,Integer> newC, Pair<Integer, Integer> oldC)
    {
        if (Math.abs(oldC.first - newC.first) != Math.abs(oldC.second - newC.second)) return false;

        int counter = Math.abs(oldC.first - newC.first);

        if (oldC.first < newC.first && oldC.second < newC.second)
            for (int i = 1; i < counter; i++)
                if (!boardT[oldC.first + i][oldC.second + i].equals("")) return false;

        if (oldC.first > newC.first && oldC.second > newC.second)
            for (int i = 1; i < counter; i++)
                if (!boardT[oldC.first - i][oldC.second - i].equals("")) return false;

        if (oldC.first > newC.first && oldC.second < newC.second)
            for (int i = 1; i < counter; i++)
                if (!boardT[oldC.first - i][oldC.second + i].equals("")) return false;

        if (oldC.first < newC.first && oldC.second > newC.second)
            for (int i = 1; i < counter; i++)
                if (!boardT[oldC.first + i][oldC.second - i].equals("")) return false;

        return true;
    }

    private boolean KnightLogic(Pair<Integer,Integer> newC, Pair<Integer, Integer> oldC) {
        if (Math.abs(oldC.first - newC.first) + Math.abs(oldC.second - newC.second) != 3)
            return false;
        if (oldC.first == newC.first || oldC.second == newC.second) return false;

        return true;
    }

    private void DrawBoard(Canvas canvas) {
        try {

            List<String> listAbb = new ArrayList<>();
            listAbb.add("wP");
            listAbb.add("wR");
            listAbb.add("wN");
            listAbb.add("wB");
            listAbb.add("wK");
            listAbb.add("wQ");
            listAbb.add("bP");
            listAbb.add("bR");
            listAbb.add("bN");
            listAbb.add("bB");
            listAbb.add("bK");
            listAbb.add("bQ");

            List<Bitmap> listBitmap = new ArrayList<>();
            listBitmap.add(whitePawn);
            listBitmap.add(whiteRook);
            listBitmap.add(whiteKnight);
            listBitmap.add(whiteBishop);
            listBitmap.add(whiteKing);
            listBitmap.add(whiteQueen);
            listBitmap.add(blackPawn);
            listBitmap.add(blackRook);
            listBitmap.add(blackKnight);
            listBitmap.add(blackBishop);
            listBitmap.add(blackKing);
            listBitmap.add(blackQueen);


            Bitmap toDraw;
            for (int i = 0; i < 8; i++)
                for (int j = 0; j < 8; j++) {
                    switch (MainActivity.board[i][j]) {
                        case "":
                            continue;

                        default:
                            toDraw = listBitmap.get(listAbb.indexOf(MainActivity.board[i][j]));
                            break;
                    }

                    canvas.drawBitmap(Bitmap.createScaledBitmap(toDraw, sizeCellX * 9 / 10, sizeCellY * 9 / 10, true), sizeCellX * j, sizeCellY * i, new Paint());
                }
        } catch(Exception ex) {
            new SendErrorsTask().execute("", "ChessBoard.java drawBoard(): " + ex.getMessage(), "ERROR");
        }
    }

    private void InitializeBoard()
    {
        MainActivity.board = new String[][]{
                {"bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR"},
                {"bP", "bP", "bP", "bP", "bP", "bP", "bP", "bP"},
                {"", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", ""},
                {"wP", "wP", "wP", "wP", "wP", "wP", "wP", "wP"},
                {"wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR"},
        };
    }
}
