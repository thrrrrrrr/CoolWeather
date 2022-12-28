package com.coolweather.android.gson;

public class Suggestion {
    public SuggestionItem suggestion1;
    public SuggestionItem suggestion2;

    public Suggestion(SuggestionItem suggestion1, SuggestionItem suggestion2) {
        this.suggestion1 = suggestion1;
        this.suggestion2 = suggestion2;
    }

    public Suggestion(){};
}
