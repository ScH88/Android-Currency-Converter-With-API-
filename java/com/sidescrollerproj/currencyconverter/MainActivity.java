package com.sidescrollerproj.currencyconverter;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import  java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    //TextView that will display the converted result, another for the currency logo preceding the EditText...
    //...and another for the currency logo preceding the result TextView
    private TextView resultText, currencyFromLogo, currencyToLogo;
    //Button for the button that the user will click
    private Button button;
    //EditText for the input box the user will enter a number in
    private EditText editText;
    //Spinner objects for selecting the type of currency the user can select to convert from and to
    private Spinner spinnerFrom, spinnerTo;
    //Integers for the current index of both spinners
    private int indexTo, indexFrom;
    //Double for the current input value
    private double inputValue;
    //String for retrieving a String value from a JSON query
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Call the superclass (AppCompatActivity) onCreate method
        super.onCreate(savedInstanceState);
        //Set the content view as the "activity_main" file in the "layout" subdirectory in the "res"/resources directory
        setContentView(R.layout.activity_main);
        //Define the EditText reference by finding it's XML file equivelant by passing it's unique ID to the findViewById method
        editText = (EditText) findViewById(R.id.editText);
        //Define the result text value by finding it's XML file equivelant by passing it's unique ID to the findViewById method
        resultText = (TextView) findViewById(R.id.resultText);
        //Define the currency (from) logo reference by finding it's XML file equivelant by passing it's unique ID to the findViewById method
        currencyFromLogo = (TextView) findViewById(R.id.currency_from_logo);
        //Define the currency (to) logo reference by finding it's XML file equivelant by passing it's unique ID to the findViewById method
        currencyToLogo = (TextView) findViewById(R.id.currency_to_logo);
        //Define the Button reference by finding it's XML file equivelant by passing it's unique ID to the findViewById method
        button = (Button) findViewById(R.id.button);
        //Define the Spinner (convert from) reference by finding it's XML file equivelant by passing it's unique ID to the findViewById method
        spinnerFrom = (Spinner) findViewById(R.id.spinnerFrom);
        //Define the Spinner (convert to) reference by finding it's XML file equivelant by passing it's unique ID to the findViewById method
        spinnerTo = (Spinner) findViewById(R.id.spinnerTo);
        //Instantiate an ArrayAdapter, creating it using this application context, the "array_currency" from the "strings" xml...
        //.. and the "simple_spinner_item" to adapt it to run on a Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currency, android.R.layout.simple_spinner_item);
        //Define the adapter's drop-down type as a single choice selection dialog
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        //Set the Spinner (convert from)'s adapter as the same adapter
        spinnerFrom.setAdapter(adapter);
        //Set the Spinner (convert to)'s adapter as the one we defined just before
        spinnerTo.setAdapter(adapter);
        //Give the spinner index integer (convert from) value a default of 0
        indexFrom = 0;
        //Give the spinner index integer (convert to) value a default of 0
        indexTo = 0;

        //Attach a new OnItemSelectedListener to the spinner(convert from)
        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //Override this OnItemSelectedListener's onItemSelected method
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                //Define indexFrom variable as the Spinner(convert from)'s current index
                indexFrom = position;
                //If the position selected is 0/first/GBP
                if (position == 0) {
                    //Set the currencyLogo (from) TextView value to the British pound symbol
                    currencyFromLogo.setText("£");
                 //If the position selected is 1/USD
                } else if (position == 1) {
                    //Set the currencyLogo (from) TextView value to the dollar symbol
                    currencyFromLogo.setText("$");
                    //If the position selected is 2/EUR
                } else if (position == 2) {
                    //Set the currencyLogo (from) TextView value to the euro symbol
                    currencyFromLogo.setText("€");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //Attach a new OnItemSelectedListener to the spinner(convert to)
        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //Override this OnItemSelectedListener's onItemSelected method
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                //Define indexTo variable as the Spinner(convert to)'s current index
                indexTo = position;
                //If the position selected is 0/first/GBP
                if (position == 0) {
                    //Set the currencyLogo (to) TextView value to the British pound symbol
                    currencyToLogo.setText("£");
                    //If the position selected is 1/USD
                } else if (position == 1) {
                    //Set the currencyLogo (to) TextView value to the dollar symbol
                    currencyToLogo.setText("$");
                    //If the position selected is 2/EUR
                } else if (position == 2) {
                    //Set the currencyLogo (to) TextView value to the euro symbol
                    currencyToLogo.setText("€");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        //Attach a new OnClickListener to the button
        button.setOnClickListener(new View.OnClickListener() {
            //Override this OnClickListener's onClick method
            @Override
            public void onClick(View view) {
                //Set the text of the result textView as a call to retrieve the "wait_text" string value from the "strings"...
                //subdirectory in the res/resources directory, followed by ellipsis
                resultText.setText(getString(R.string.wait_text) + "....");
                //If the editText's (trimmed and converted to String) length is greater than 0, and does not consist of just a "."
                if (editText.getText().toString().trim().length() > 0 && !editText.getText().toString().trim().equals(".")) {
                    //If the (convert to) index is the same as the (convert from) index
                    if (indexTo == indexFrom) {
                        //Set the text of the result TextView as the same amount as the EditText input
                        //Use String.format and %.2f to cut the number of decimals to 2
                        resultText.setText(String.format("%.2f", Double.parseDouble(editText.getText().toString().trim())));
                    //Otherwise
                    } else {
                        //Define the input value the EditText's value, converted to Double
                        inputValue = Double.parseDouble(editText.getText().toString());
                        //Create a new instance of the nested CalculatePrice/AsyncTast class
                        //...and call it's execute/doInBackground method
                        new CalculatePrice().execute();
                    }
                } else {
                    //Set the text value of the resultText TextView back to the "result_placeholder" String from the Strings....
                    //...XML file in the "values" subdirectory in the res/resources directory
                    resultText.setText(getString(R.string.result_placeholder));
                    //Call the Toast class to display the following message on-screen
                    Toast.makeText(getApplicationContext(), "Double value must be inputted", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Nested class CalcuatePrice. Custom subclass of AsyncTask
    public class CalculatePrice extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        //Override the superclass' doInBackground, passing it a String
        @Override
        protected String doInBackground(String... strings) {
            //Local String for the base currency selected, with a default value of null
            String baseCurr = "";
            //Local String for the currency the current input value will be converted to, with a default value of null
            String convertTo = "";
            //If the current Spinner(convert from) index is 0/GBP
            if (indexFrom == 0) {
                //Set the base currency String as "GBP"
                baseCurr = "GBP";
             //If the current Spinner(convert from) index is 1/USD
            } else if (indexFrom == 1) {
                //Set the base currency String as "USD"
                baseCurr = "USD";
             //If the current Spinner(convert from) index is 2/EUR
            } else if (indexFrom == 2) {
                //Set the base currency String as "EUR"
                baseCurr = "EUR";
            }
            //If the current Spinner(convert to) index is 0/GBP
            if (indexTo == 0) {
                //Set the convertTo String to "GBP"
                convertTo = "GBP";
             //If the current Spinner(convert to) index is 1/USD
            } else if (indexTo == 1) {
                //Set the convertTo String to "USD"
                convertTo = "USD";
             //If the current Spinner(convert to) index is 2/EUR
            } else if (indexTo == 2) {
                //Set the convertTo String to "EUR"
                convertTo = "EUR";
            }
            //Try Statement
            try {
                //Create a string for the website URL we will retrieve the JSON...
                //...then pass it to the getJSONResult method in order to get the String return value from it's JSON querying
                String siteURL = getJSONResult("https://api.fixer.io/latest?base=" + baseCurr + "&symbols=" + convertTo);
                //Define the result as a String extracted from the 1st(and only) JSONObject from inside the "rates" array, itself...
                //...from inside the JSONObject returned from the query
                result = new JSONObject(siteURL).getJSONObject("rates").getString(convertTo);
            //If a JSONException is caught
            }  catch (JSONException e) {
                //Print the stack trace of this exception to console
                e.printStackTrace();
             //If an IOException is caught
            } catch (IOException i) {
                //Print the stack trace of this exception to console
                i.printStackTrace();
            }
            //Return the result this query
            return result;
        }

        @Override
        protected void onPostExecute(String string) {
            //Set the text of the result TextView as the input value multiplied by (cast to Double) result from the JSON query
            //Use String.format and %.2f to cut the number of decimals to 2
            resultText.setText(String.format("%.2f", inputValue * Double.parseDouble(result)));
        }

        public String getJSONResult(String url) throws ClientProtocolException, IOException{
            //StringBuilder object - functions as a String but can be modified
            StringBuilder sb = new StringBuilder();
            //HTTPClient used for executing an oncoming URL method
            HttpClient client = new DefaultHttpClient();
            //Instance of HttpGet - Prepares to retrieve information (in the form of an Entity) as identified by the URL passed to it
            HttpGet httpGet = new HttpGet(url);
            //Instance of HttpResponse - is initialized by the client executing the HttpGet query
            HttpResponse httpResponse = client.execute(httpGet);
            //Get the information from the HttpResponse and store as a HttpEntity object
            HttpEntity entity = httpResponse.getEntity();
            //Create an inputStream using the content of the HttpEntity. This will allow an InputStreamReader/BufferedReader read through it all
            InputStream content = entity.getContent();
            //Create a new BufferedReader instance. This will receive an InputStreamReader that will read from the InputStream just before
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
            //String for the current line being read
            String currentLine;
            //While the current line of the BufferedReader is not null, while setting the currentLine String's value as the line
            while ((currentLine = reader.readLine()) != null) {
                //Append the current line to the StringBuilder
                sb.append(currentLine);
            }
            //Return the StringBuilder object
            return sb.toString();
        }
    }
}
