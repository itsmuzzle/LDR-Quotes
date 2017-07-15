package app.com.subtle_media.ldrquotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAccess
{
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;

    /**
     * Private constructor to avoid object creation from outside classes.
     *
     * @param context the Context
     */
    private DatabaseAccess(Context context)
    {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DatabaseAccess
     */
    static DatabaseAccess getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    void open()
    {
        this.database = openHelper.getWritableDatabase();
    }

    /**
     * Close the database connection.
     */
    void close()
    {
        if (database != null)
        {
            this.database.close();
        }
    }

    /**
     * Read all quotes from the database.
     *
     * @return a List of quotes
     */
    List<Quote> getQuotes()
    {
        List<Quote> list = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM quotes", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Quote quote = new Quote(cursor.getInt(0), cursor.getString(1));
            list.add(quote);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    /**
     * Read all quotes from the database.
     *
     * @return a List of user created quotes
     */
    List<Quote> getCreatedQuotes()
    {
        List<Quote> list = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM created_quotes", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Quote quote = new Quote(cursor.getInt(0), cursor.getString(1));
            list.add(quote);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    /**
     * Save a user created quote to the database.
     */
    void saveCreatedQuote(EditText createEditText)
    {
        ContentValues cv = new ContentValues();
        cv.put("quote_text", createEditText.getText().toString());
        database.insert("created_quotes", null, cv);
    }

    /**
     * Delete a user created quote from the database.
     */
    void deleteCreatedQuote(int quoteId)
    {
        database.execSQL("DELETE FROM created_quotes WHERE quote_id = '" + quoteId + "'");
    }

}