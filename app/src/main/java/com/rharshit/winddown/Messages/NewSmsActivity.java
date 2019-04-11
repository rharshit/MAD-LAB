package com.rharshit.winddown.Messages;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.rharshit.winddown.R;
import com.rharshit.winddown.Util.Theme;

public class NewSmsActivity extends AppCompatActivity{

    EditText address, message;
    ImageButton send_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Theme.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_new);



        address = (EditText) findViewById(R.id.address);
        message = (EditText) findViewById(R.id.message);
        send_btn = (ImageButton) findViewById(R.id.send_btn);
        Intent intent=getIntent();
        address.setText( intent.getStringExtra( "Number" ) );



        send_btn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String str_addtes = address.getText().toString();
                String str_message = message.getText().toString();


                if (str_addtes.length() > 0 && str_message.length() > 0) {

                    if(Function.sendSMS(str_addtes, str_message))
                    {
                        Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

        });
    }
}

