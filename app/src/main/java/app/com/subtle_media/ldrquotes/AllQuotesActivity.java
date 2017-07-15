package app.com.subtle_media.ldrquotes;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AllQuotesActivity extends SelectActivity implements View.OnClickListener
{
    /* The ArrayList for storing all quotes */
    private List<Quote> mQuoteList;

    /* The TextView to display the quote text */
    private TextView mQuoteText;

    /* The ID of the current quote */
    private int mCurrentQuoteId;

    /* Google AdMob Interstitial */
    private InterstitialAd mInterstitial;

    /* The Bottom Navigation Layout */
    private LinearLayout mBottomLayout;

    /* The Copyright text displayed when taking a screenshot */
    private TextView mCopyright;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initAds();
        initQuotes();
    }

    /* Initialise the views */
    private void initViews()
    {
        Button homeButton = (Button) findViewById(R.id.all_quotes_home_button);
        homeButton.setOnClickListener(this);
        Button shareButton = (Button) findViewById(R.id.all_quotes_share_button);
        shareButton.setOnClickListener(this);
        Button backButton = (Button) findViewById(R.id.all_quotes_back_button);
        backButton.setOnClickListener(this);
        Button nextButton = (Button) findViewById(R.id.all_quotes_next_button);
        nextButton.setOnClickListener(this);
        mBottomLayout = (LinearLayout) findViewById(R.id.all_quotes_bottom_layout);
        mCopyright = (TextView) findViewById(R.id.all_quotes_copyright);

    }

    /* Initialise the ads */
    private void initAds()
    {
        mInterstitial = new InterstitialAd(this);
        mInterstitial.setAdUnitId(getString(R.string.interstitial_add_unit_id));
        mInterstitial.loadAd(new AdRequest.Builder().build());
    }

    /* Initialise the quotes */
    private void initQuotes()
    {
        mQuoteList = new ArrayList<>();
        mQuoteText = (TextView) findViewById(R.id.my_quotes_quoteText);

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        mQuoteList = databaseAccess.getQuotes();
        databaseAccess.close();
        Collections.shuffle(mQuoteList);
        setQuote();
    }

    /* Set the current quote */
    private void setQuote()
    {
        /* If there are quotes in the array then we can set the quote text */
        if (mQuoteList.size() > 0)
        {
            mQuoteText.setText(mQuoteList.get(mCurrentQuoteId).getQuoteText());
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.all_quotes_home_button:
                onHomeButtonPressed();
                break;
            case R.id.all_quotes_share_button:
                onShareButtonPressed();
                break;
            case R.id.all_quotes_back_button:
                onBackButtonPressed();
                break;
            case R.id.all_quotes_next_button:
                onNextButtonPressed();
                break;
        }
    }

    /* Respond to clicks on the Home Button */
    private void onHomeButtonPressed()
    {
        NavUtils.navigateUpFromSameTask(this);
        mInterstitial.show();
    }

    /* Respond to clicks on the Share Button */
    private void onShareButtonPressed()
    {
        if (shouldAskPermissions())
        {
            askPermissions();
        }
            new ScreenshotTask().execute();
    }

    /* Return true if we should ask for permissions to store screenshots  */
    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    /* Ask for READ/WRITE Permissions to store screenshots */
    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    /* Respond to clicks on the Back Button */
    private void onBackButtonPressed()
    {
        if (mCurrentQuoteId > 0)
        {
            mCurrentQuoteId--;
            setQuote();
        }
    }

    /* Respond to clicks on the Next Button */
    private void onNextButtonPressed()
    {
        if (mCurrentQuoteId < mQuoteList.size() - 1)
        {
            mCurrentQuoteId++;
            setQuote();
        }
    }

    /* Task to save the current quote to the users phone */
    private class ScreenshotTask extends AsyncTask<Bitmap, Void, Boolean>
    {
        /* The current Activity */
        private Activity activity;

        @Override
        protected void onPreExecute()
        {
            /* Store the current Activity so we can use it later */
            this.activity = AllQuotesActivity.this;

            /* Set the bottom navigation layout to View.GONE */
            mBottomLayout.setVisibility(View.GONE);

            /* Set the copyright text to View.VISIBLE */
            mCopyright.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Bitmap... bitmaps)
        {
            /* Grab the current date and time for naming of the image file */
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date now = new Date();
            String fileName = formatter.format(now) + ".png";

            /* Try to take a screenshot and write the image file to the users device */
            try
            {
                /* Create a Cache Directory */
                File cacheDir = new File(
                        Environment.getExternalStorageDirectory().toString(),
                        "DCIM/LDRQuotes");

                Boolean cacheDirExists = cacheDir.exists();

                /* If the cache directory does not exist then we need to create one */
                if (!cacheDirExists)
                {
                    cacheDirExists = cacheDir.mkdirs();
                }

                /* If the cache directory does exist then proceed with the task */
                if (cacheDirExists)
                {
                    /* Create a path to the new file */
                    String path = new File(
                            Environment.getExternalStorageDirectory().toString(),
                            "DCIM/LDRQuotes") + "/" + fileName;

                    /* Take a screenshot and save it to the path */
                    Utils.savePic(Utils.takeScreenShot(activity), path);

                    /* Tell the Gallery about the new file so it's immediately available */
                    MediaScannerConnection.scanFile(activity,
                            new String[] { path }, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                }
                            });
                }
                return true;
            }
            catch (NullPointerException ignored)
            {
                ignored.printStackTrace();
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            /* Set the copyright text to View.INVISIBLE */
            mCopyright.setVisibility(View.INVISIBLE);

            /* Set the bottom navigation layout to View.VISIBLE */
            mBottomLayout.setVisibility(View.VISIBLE);

            /* Display a message to the user that the screenshot has been saved to their gallery  */
            Toast.makeText(getApplicationContext(), R.string.quote_saved_to_devices_gallery,
                    Toast.LENGTH_SHORT).show();
        }
    }
}


