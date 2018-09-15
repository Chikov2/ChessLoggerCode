package com.epitel.chesslogger.chesslogger;

import android.util.Pair;

public class Move {
    char piece;
    Pair<Integer, Integer> beginPos;
    Pair<Integer, Integer> endPos;
    String pieceCaptured;
    String notation;
    String special;
    boolean shortCastleChanged;
    boolean longCastleChanged;

    Move() {
        pieceCaptured = "";
        special = "";
        notation = "";
    }

    public void generateNotation() {
        if (special.equals("0-0") || special.equals("0-0-0")) {
            notation = special;
            return;
        }

        if (piece != 'P')
            notation += piece;
        if (special.length() > 0 && ((special.charAt(0) >= 'a' && special.charAt(0) <= 'h') || (special.charAt(0) >= '1' && special.charAt(0) >= '8')))
            notation += special.charAt(0);

        if (pieceCaptured.length() > 0) {
            if (piece == 'P')
                notation += (char) ('a' + beginPos.second);
            notation += "x";
        }

        notation += (char) ('a' + endPos.second);
        notation += (char) ('1' + (7 - endPos.first));
    }
}
