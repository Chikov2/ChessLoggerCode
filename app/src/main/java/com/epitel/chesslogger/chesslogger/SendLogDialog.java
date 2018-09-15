package com.epitel.chesslogger.chesslogger;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.validator.routines.EmailValidator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SendLogDialog extends Dialog implements View.OnClickListener {

    private Activity curAct;
    private String emailAddress;
    private String fullLog;

    SendLogDialog(Activity a, String email)
    {
        super(a);

        curAct = a;
        emailAddress = email;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_log);

        if (emailAddress.length() > 0)
        {
            EditText ed = this.findViewById(R.id.emailText);
            ed.setText(emailAddress, TextView.BufferType.EDITABLE);
        }

        Button btn = findViewById(R.id.sendLog);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        try {
            emailAddress = ((EditText) findViewById(R.id.emailText)).getText().toString();
            String logName = ((EditText) findViewById(R.id.logNameText)).getText().toString();
            Boolean log = ((CheckBox) findViewById(R.id.checkbox_log)).isChecked();
            Boolean position = ((CheckBox) findViewById(R.id.checkbox_position)).isChecked();

            if (!log && !position) return;
            if (!EmailValidator.getInstance().isValid(emailAddress)) return;

            hideKeyboard();
            if (log)
                fullLog = generateFullLog(logName) + "\n";
            if (position)
                fullLog += generateFENPosition();

            new SendLogFileTask(getContext()).execute(emailAddress, "\"" + logName + "\" arrived", fullLog);

            SharedPreferences sharedPref = curAct.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("email", emailAddress);
            editor.apply();

            dismiss();
        } catch(Exception ex) {
            new SendErrorsTask().execute("", "SendLogDialog.java onClick(): " + ex.getMessage(), "ERROR");
        }
    }

    private String generateFENPosition()
    {
        StringBuilder position = new StringBuilder("Position : https://lichess.org/editor");

        for (int i = 7; i >= 0; i--)
        {
            position.append('/');
            int counter = 0;
            for (int j = 0; j < 8; j++)
            {
                if (MainActivity.board[i][j].equals(""))
                    counter++;
                else
                {
                    if (counter > 0) position.append(Integer.toString(counter));
                    counter = 0;
                    if (MainActivity.board[i][j].charAt(0) == 'w')
                        position.append(Character.toUpperCase(MainActivity.board[i][j].charAt(1)));
                    else position.append(Character.toLowerCase(MainActivity.board[i][j].charAt(1)));
                }
            }

            if (counter > 0) position.append(Integer.toString(counter));
        }

        if (MainActivity.whiteMoves.size() == MainActivity.blackMoves.size())
            position.append("_w_");
        else position.append("_b_");

        String castle = "";
        if (MainActivity.whiteCanCastleShort) castle += "K";
        if (MainActivity.whiteCanCastleLong) castle += "Q";
        if (MainActivity.blackCanCastleShort) castle += "k";
        if (MainActivity.blackCanCastleLong) castle += "q";
        if (castle.length() == 0) castle = "-";

        position.append(castle);

        return position.toString();
    }

    private String generateFullLog(String logName)
    {
        StringBuilder log = new StringBuilder("[Name \"" + logName + "\"]" + "\n");
        log.append("[Date \"");
        log.append(new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime()));
        log.append("\"]\n");

        for (int i = 0; i < MainActivity.whiteMoves.size(); i++) {
            log.append(Integer.toString(i + 1));
            log.append(". ");
            log.append(MainActivity.whiteMoves.get(i).notation);
            log.append(" ");

            if (i < MainActivity.blackMoves.size()) {
                log.append(MainActivity.blackMoves.get(i).notation);
                log.append(" ");
            }
        }

        return log.toString();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) curAct.getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
