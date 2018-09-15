package com.epitel.chesslogger.chesslogger;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static String[][] board = new String[8][8];
    public static String result;
    public static ArrayList<Move> whiteMoves = new ArrayList<>();
    public static ArrayList<Move> blackMoves = new ArrayList<>();
    public static boolean whiteCanCastleShort = true;
    public static boolean whiteCanCastleLong = true;
    public static boolean blackCanCastleShort = true;
    public static boolean blackCanCastleLong = true;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                /*case R.id.navigation_undo:
                    return true;*/
                case R.id.navigation_send:
                    SendLog();
                    return true;
                case R.id.navigation_exit:
                    System.exit(0);
                    return true;
            }
            return false;
        }
    };

    public void SendLog()
    {
        try {
            SharedPreferences pref = this.getPreferences(Context.MODE_PRIVATE);
            String email = pref.getString("email", "");

            SendLogDialog popup = new SendLogDialog(this, email);
            popup.setCanceledOnTouchOutside(true);
            popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popup.show();
        } catch(Exception ex)
        {
            new SendErrorsTask().execute("", "SendLog: " + ex.getMessage(), "ERROR");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            ChessBoard cb = new ChessBoard(this);
            setContentView(R.layout.activity_main);

            LinearLayout ll = findViewById(R.id.chessBoardMain);
            ll.addView(cb);

            BottomNavigationView navigation = findViewById(R.id.navigation);
            navigation.getMenu().setGroupCheckable(0, false, true);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        } catch(Exception ex) {
            new SendErrorsTask().execute("", "MainActivity.java onCreate(): " + ex.getMessage(), "ERROR");
        }
    }

}
