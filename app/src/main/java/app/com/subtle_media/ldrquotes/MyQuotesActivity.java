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
import java.util.Date;
import java.util.List;

public class MyQuotesActivity extends SelectActivity implements View.OnClickListener
{
    /* The ArrayList for storing all quotes */
    private List<Quote> mQuoteList;

    /* The TextView to display the quote text */
    private TextView mQuoteText;

    /* The ID of the current quote */
    private int mCurrentQuoteId;

    /* Provides access to the Quote Database */
    private DatabaseAccess mDatabaseAccess;

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
        setContentView(R.layout.activity_my_quotes);
        initViews();
        initAds();
        initQuotes();
    }

    /* Initialise the views */
    private void initViews()
    {
        Button homeButton = (Button) findViewById(R.id.my_quotes_home_button);
        homeButton.setOnClickListener(this);
        Button deleteButton = (Button) findViewById(R.id.my_quotes_delete_button);
        deleteButton.setOnClickListener(this);
        Button shareButton = (Button) findViewById(R.id.my_quotes_share_button);
        shareButton.setOnClickListener(this);
        Button backButton = (Button) findViewById(R.id.my_quotes_back_button);
        backButton.setOnClickListener(this);
        Button nextButton = (Button) findViewById(R.id.my_quotes_next_button);
        nextButton.setOnClickListener(this);
        mBottomLayout = (LinearLayout) findViewById(R.id.my_quotes_bottom_layout);
        mCopyright = (TextView) findViewById(R.id.my_quotes_copyright);
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

        mDatabaseAccess = DatabaseAccess.getInstance(this);
        mDatabaseAccess.open();
        mQuoteList = mDatabaseAccess.getCreatedQuotes();
        setQuote();
    }

    /* Set the current quote */
    private void setQuote()
    {
        if (hasCreatedQuotes())
        {
            /* If there are quotes in the array then we can display the fist one */
            mQuoteText.setText(mQuoteList.get(mCurrentQuoteId).getQuoteText());
        }
        else
        {
            /* If the array is empty then display a default error message */
            mCurrentQuoteId = -1;
            mQuoteText.setText(R.string.no_created_quotes);
        }
    }

    /* Return true if the user has created quotes saved */
    public Boolean hasCreatedQuotes()
    {
        return mQuoteList.size() > 0;
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.my_quotes_home_button:
                onHomeButtonPressed();
                break;
            case R.id.my_quotes_delete_button:
                onDeleteButtonPressed();
                break;
            case R.id.my_quotes_share_button:
                onShareButtonPressed();
                break;
            case R.id.my_quotes_back_button:
                onBackButtonPressed();
                break;
            case R.id.my_quotes_next_button:
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

    /* Respond to clicks on the Delete Button */
    private void onDeleteButtonPressed()
    {
        if (hasCreatedQuotes())
        {
            mDatabaseAccess.deleteCreatedQuote(mQuoteList.get(mCurrentQuoteId).getQuoteID());
            Toast.makeText(getApplicationContext(), R.string.my_quotes_quote_deleted,
                    Toast.LENGTH_LONG).show();
            mQuoteList.remove(mCurrentQuoteId);
            setQuote();
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                    R.string.need_to_add_quotes_before_deleting, Toast.LENGTH_LONG).show();
        }
    }

    /* Respond to clicks on the Share Button */
    private void onShareButtonPressed()
    {
        if (hasCreatedQuotes())
        {
            if (shouldAskPermissions())
            {
                askPermissions();
            }
            new ScreenshotTask().execute();
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                    R.string.need_to_add_quotes_before_sharing, Toast.LENGTH_LONG).show();
        }
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

    /* Close the database connection on destroying the activity */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mDatabaseAccess.close();
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

    /* Task to save the current quote to the users phone */
    private class ScreenshotTask extends AsyncTask<Bitmap, Void, Boolean>
    {
        /* The current Activity */
        private Activity activity;

        @Override
        protected void onPreExecute()
        {
            /* Store the current Activity so we can use it later */
            this.activity = MyQuotesActivity.this;

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
