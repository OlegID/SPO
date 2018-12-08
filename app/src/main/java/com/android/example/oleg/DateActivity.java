package com.android.example.oleg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class DateActivity extends AppCompatActivity {

    private static String content = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);
        EditText interval = findViewById(R.id.editText);
        interval.setText(content);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickStartService(View view)
    {
        EditText interval = findViewById(R.id.editText);
        content = interval.getText().toString();
        try {
            int time = Integer.parseInt(content);
            if (time > 0) {
                startService(new Intent(this, ParseService.class).putExtra("time", time));
            }
            else if(time == 0)
            {
                stopService(new Intent(this, ParseService.class));
            }
            else {
                Toast.makeText(getApplicationContext(), "Число должно быть больше 0", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Введите число", Toast.LENGTH_SHORT).show();
        }
    }
}
