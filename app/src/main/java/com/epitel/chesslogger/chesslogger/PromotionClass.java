package com.epitel.chesslogger.chesslogger;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

class PromotionClass extends Dialog implements View.OnClickListener {
    private String player;
    private Activity c;
    private int X;
    private int Y;

    PromotionClass(Activity a, String newPlayer, int newX, int newY)
    {
        super(a);

        X = newX; Y = newY;
        this.c = a;
        player = newPlayer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.promotion);
            Button tempB;
            if (player.equals("w")) {
                tempB = findViewById(R.id.btn_promotion_queen);
                tempB.setBackgroundResource(R.drawable.whitequeen);
                tempB.setOnClickListener(this);

                tempB = findViewById(R.id.btn_promotion_rook);
                tempB.setBackgroundResource(R.drawable.whiterook);
                tempB.setOnClickListener(this);

                tempB = findViewById(R.id.btn_promotion_bishop);
                tempB.setBackgroundResource(R.drawable.whitebishop);
                tempB.setOnClickListener(this);

                tempB = findViewById(R.id.btn_promotion_knight);
                tempB.setBackgroundResource(R.drawable.whiteknight);
                tempB.setOnClickListener(this);
            } else {
                tempB = findViewById(R.id.btn_promotion_queen);
                tempB.setBackgroundResource(R.drawable.blackqueen);
                tempB.setOnClickListener(this);

                tempB = findViewById(R.id.btn_promotion_rook);
                tempB.setBackgroundResource(R.drawable.blackrook);
                tempB.setOnClickListener(this);

                tempB = findViewById(R.id.btn_promotion_bishop);
                tempB.setBackgroundResource(R.drawable.blackbishop);
                tempB.setOnClickListener(this);

                tempB = findViewById(R.id.btn_promotion_knight);
                tempB.setBackgroundResource(R.drawable.blackknight);
                tempB.setOnClickListener(this);
            }
        } catch(Exception ex) {
            new SendErrorsTask().execute("", "PromotionClass.java onCreate(): " + ex.getMessage(), "ERROR");
        }
    }

    @Override
    public void onClick(View v){
        try {
            String result = "";

            switch (v.getId()) {
                case R.id.btn_promotion_bishop:
                    result = player + "B";
                    break;
                case R.id.btn_promotion_knight:
                    result = player + "N";
                    break;
                case R.id.btn_promotion_rook:
                    result = player + "R";
                    break;
                case R.id.btn_promotion_queen:
                    result = player + "Q";
                    break;
            }

            dismiss();

            View view = c.findViewById(R.id.chessBoardMain);
            ViewGroup viewGroup = (ViewGroup) view;
            ((ChessBoard) viewGroup.getChildAt(0)).modifyOnQueening(X, Y, result);
        } catch(Exception ex) {
            new SendErrorsTask().execute("", "PromotionClass.java onClick(): " + ex.getMessage(), "ERROR");
        }
    }

}
