package com.sam_chordas.android.stockhawk.model;

public class Results
{
    private Quote[] quote;

    public Quote[] getQuote ()
    {
        return quote;
    }

    public void setQuote (Quote[] quote)
    {
        this.quote = quote;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [quote = "+quote+"]";
    }
}
