package com.example.academicarticlemanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TitleScreenLoggedIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen_logged_in);

        Button myArticlesButton = (Button) findViewById(R.id.myArticlesButton);
        Button viewArticlesButton = (Button) findViewById(R.id.viewArticlesButtonLogged);

        // View My Articles
        myArticlesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newActivity2 = new Intent(TitleScreenLoggedIn.this, MyArticles.class);
                startActivity(newActivity2);
            }
        });

        // View all articles
        viewArticlesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newActivity = new Intent(TitleScreenLoggedIn.this, AllArticles.class);
                startActivity(newActivity);
            }
        });
    }
}
