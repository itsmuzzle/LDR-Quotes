package app.com.subtle_media.ldrquotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class SelectActivity extends AppCompatActivity implements View.OnClickListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        initViews();
    }


    /* Initialise the views */
    private void initViews()
    {
        TextView tvAllQuotes = (TextView) findViewById(R.id.activity_select_all_quotes);
        TextView tvFavouriteQuotes = (TextView) findViewById(R.id.activity_select_favourite_quotes);
        TextView tvCreateYourOwn = (TextView) findViewById(R.id.activity_select_create_your_own);

        tvAllQuotes.setOnClickListener(this);
        tvFavouriteQuotes.setOnClickListener(this);
        tvCreateYourOwn.setOnClickListener(this);
    }

    /* Respond to clicks on the Menu Buttons */
    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.activity_select_all_quotes:
                Intent intent = new Intent(this, AllQuotesActivity.class);
                startActivity(intent);
                break;
            case R.id.activity_select_favourite_quotes:
                Intent intent1 = new Intent(this, MyQuotesActivity.class);
                startActivity(intent1);
                break;
            case R.id.activity_select_create_your_own:
                Intent intent2 = new Intent(this, CreateQuoteActivity.class);
                startActivity(intent2);
                break;
        }
    }
}
