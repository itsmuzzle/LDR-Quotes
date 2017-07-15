package app.com.subtle_media.ldrquotes;

class Quote
{
    /* The quotes ID */
    private int quoteID;

    /* The quotes text */
    private String quoteText;

    /* Default Quote constructor */
    Quote(int quoteID, String quoteText)
    {
        this.quoteID = quoteID;
        this.quoteText = quoteText;
    }

    /* Return the quotes text */
    String getQuoteText()
    {
        return quoteText;
    }

    /* Return the quotes ID */
    int getQuoteID()
    {
        return quoteID;
    }
}

