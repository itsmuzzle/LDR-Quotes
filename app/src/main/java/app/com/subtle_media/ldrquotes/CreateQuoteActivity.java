package app.com.subtle_media.ldrquotes;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class CreateQuoteActivity extends AppCompatActivity implements View.OnClickListener
{

    /* EditText to received the created quote  */
    private EditText createQuoteEditText;

    /* Goodle AdMob Interstitial */
    private InterstitialAd mInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_a_quote);
        initAds();
        initViews();
    }

    /* Initialise the views */
    private void initViews()
    {
        /* Button in the Bottom Navigation Layout to return to the home screen */
        Button createQuoteHomeButton = (Button) findViewById(R.id.create_a_quote_home_button);
        createQuoteHomeButton.setOnClickListener(this);

        /* Button in the Bottom Navigation Layout to save a created a quote */
        Button createQuoteButton = (Button) findViewById(R.id.create_a_quote_create_button);
        createQuoteButton.setOnClickListener(this);

        /* EditText to received the created quote  */
        createQuoteEditText = (EditText) findViewById(R.id.create_a_quote_editText);

        /* This is a little hacky but it works. Basically, it's not possible to set maxLines
         * on a multiLine EditText, and its not possible to make the text wrap in respect to the
         * layouts padding with a normal EditText.
         *
         * This overrides the return key of the multiLine EditText so that we can enjoy delicious
         * padding but we don't have to worry about users exploiting with lots of new lines */
        createQuoteEditText.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                return keyCode == KeyEvent.KEYCODE_ENTER;
            }
        });
    }

    /* Initialise the ads */
    private void initAds()
    {
        mInterstitial = new InterstitialAd(this);
        mInterstitial.setAdUnitId(getString(R.string.interstitial_add_unit_id));
        mInterstitial.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.create_a_quote_home_button:
                onHomeButtonClicked();
                break;
            case R.id.create_a_quote_create_button:
                onCreateQuoteButtonClicked();
                break;
        }
    }

    /* Respond to clicks on the Home Button */
    private void onHomeButtonClicked()
    {
        NavUtils.navigateUpFromSameTask(this);
        mInterstitial.show();
    }

    /* Respond to clicks on the Create Button */
    private void onCreateQuoteButtonClicked()
    {
        if (createQuoteEditText.getText().toString().length() >= 5)
        {
            /* If the user-entered quote is at least 5 chars, add it to the database */
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
            databaseAccess.open();
            databaseAccess.saveCreatedQuote(createQuoteEditText);
            databaseAccess.close();

            /* Display a message to inform the user that the quote has been saved successfully */
            Toast.makeText(this, R.string.create_quote_saved_successfully, Toast.LENGTH_LONG).show();

            /* Reset the EditText */
            createQuoteEditText.setText("");
        }
        else
        {
            /* If the user-entered quote is less than 5 chars then display a message to warn them */
            Toast.makeText(this, R.string.create_quote_minimum_chars_error, Toast.LENGTH_LONG).show();
        }
    }
}
