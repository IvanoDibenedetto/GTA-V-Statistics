package com.makeitsimple.gtav_onlinestats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Header;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    int cookieCounter=0;
    WebView wb;
    ProgressBar pb;
    Spinner sp;
    boolean isLoaded = false;

    String cookies;
    int status=0;
    List<Header> cookie;

    TableLayout tl;

    String statistics ="";
    String overall="";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.this.setTitle("GTA V - Online statistics");
        setContentView(R.layout.activity_main);

        tl = findViewById(R.id.idTable);
        sp = findViewById(R.id.idSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.stat_detail, R.layout.spinner_item);

        adapter.setDropDownViewResource(com.makeitsimple.gtav_onlinestats.R.layout.spinner_dropdown);
        sp.setEnabled(false);
        sp.setAdapter(adapter);

        sp.setVisibility(View.INVISIBLE);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(sp.isEnabled()) {
                   String divId = adapterView.getItemAtPosition(i).toString().toLowerCase();

                    if(divId.equals("overall")){
                        parseOverallHTML();
                    }
                    else {
                        parseStatsHTML(divId);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        wb = findViewById(R.id.idWebView);
        pb = findViewById(R.id.idProgressbar);
        pb.setVisibility(View.INVISIBLE);

        cookie = new ArrayList<>();

            wb.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    cookies = CookieManager.getInstance().getCookie(url);
                    cookieCounter++;

                    if(cookies.contains("BearerToken")) {
                        wb.setVisibility(View.INVISIBLE);
                        pb.setVisibility(View.VISIBLE);
                    }

                    if (cookies.contains("BearerToken") && cookies.contains("CSRFToken") && url.contains(".socialclub.rockstargames.com") && cookieCounter >= 3) {

                        StringToHeaderAll(cookies);
                        wb.setVisibility(View.INVISIBLE);
                        pb.setVisibility(View.INVISIBLE);
                        sp.setEnabled(true);
                        sp.setVisibility(View.VISIBLE);
                        wb.destroy();
                        isLoaded=true;
                        OverallAPI(getApplicationContext());
                        StatisticsAPI(getApplicationContext());

                    }
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    Toast.makeText(getApplicationContext(),"Check your internet connection",Toast.LENGTH_LONG).show();
                    super.onReceivedError(view, request, error);
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                }
            });
            wb.getSettings().setJavaScriptEnabled(true);
            wb.getSettings().setDomStorageEnabled(true);

            wb.loadUrl("https://socialclub.rockstargames.com/profile/signin");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_refresh_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("ShowToast")
    public  void OverallAPI(final Context context){

        RequestQueue MyRequestQueue = Volley.newRequestQueue(context);
        String lan = getValueByName("rockstarweb_lang.prod");
        String url="";
         if(lan.length()>0) url = "https://" + getValueByName("rockstarweb_lang.prod")+"."+ UrlAPI.OVERAL;
             else url = "https://"+ UrlAPI.OVERAL;


            StringRequest jRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

               overall = response;
                parseOverallHTML();

                if(pb.getVisibility() == View.VISIBLE)
                    pb.setVisibility(View.INVISIBLE);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                status = error.networkResponse.statusCode;
                if(status==401)RefreshToken(context);
                Log.d("ERROR",error.toString());

            }
        })

        {
            @Override
            public Map<String, String> getHeaders()  {
                Map<String, String> params = new HashMap<>();
                params.put("Accept","*/*");
                params.put("Connection","keep-alive");
               // params.put("accept-language","it-IT,it;q=0.9,en-US;q=0.8,en;q=0.7");
               //  params.put("referer","https://"+getValueByName("rockstarweb_lang.prod")+".socialclub.rockstargames.com");
               //   params.put("Accept-Encoding","gzip,deflate,br");
               //  params.put("Content-Type", "text/html; charset=utf-8");
              // params.put("requestverificationcode","9D4pGB7e30I-DpY3iOiaCJJOo7mJxZ-QVyPS3eQNNp7tu5Q_RbUR6KbJAv6Suw1mIQOrohHWBHj5mk-PvvW3-agQAS3L9Y5cERiOFuuXnQMqOfTvXUN8Ohs6Z_oQbyrw0DnUdw2");
                params.put("Cookie", HeaderToStringAll());
                params.put("x-requested-with","XMLHttpRequest");
                params.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36");
                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {

                status = response.statusCode;
                return super.parseNetworkResponse(response);
            }
        };
        MyRequestQueue.add(jRequest);

    }

    public void StatisticsAPI(final Context context){
        RequestQueue MyRequestQueue = Volley.newRequestQueue(context);

        String lan = getValueByName("rockstarweb_lang.prod");
        String url="";

        if(lan.length()>0) url = "https://" + getValueByName("rockstarweb_lang.prod")+"."+ UrlAPI.STATISTICS;
             else url = "https://"+ UrlAPI.STATISTICS;


        StringRequest jRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                statistics = response;
                sp.setSelection(0);
                tl.setAlpha(1.0f);

                if(pb.getVisibility() == View.VISIBLE) {
                    pb.setVisibility(View.INVISIBLE);
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //errore dal server

                status = error.networkResponse.statusCode;
                if(status==401)RefreshToken(context);
                Log.d("ERROR",error.toString());

            }
        })

        {
            @Override
            public Map<String, String> getHeaders()  {
                Map<String, String> params = new HashMap<>();
                params.put("Accept","*/*");
                params.put("Connection","keep-alive");
                // params.put("accept-language","it-IT,it;q=0.9,en-US;q=0.8,en;q=0.7");
                //   params.put("referer","https://"+getValueByName("rockstarweb_lang.prod")+".socialclub.rockstargames.com");
                // params.put("Accept-Encoding","gzip,deflate,br");
                //  params.put("Content-Type", "text/html; charset=utf-8");
                // params.put("requestverificationcode","9D4pGB7e30I-DpY3iOiaCJJOo7mJxZ-QVyPS3eQNNp7tu5Q_RbUR6KbJAv6Suw1mIQOrohHWBHj5mk-PvvW3-agQAS3L9Y5cERiOFuuXnQMqOfTvXUN8Ohs6Z_oQbyrw0DnUdw2");
                params.put("Cookie", HeaderToStringAll());
                params.put("x-requested-with","XMLHttpRequest");
                params.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36");
                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {

                status = response.statusCode;
                return super.parseNetworkResponse(response);
            }
        };
        MyRequestQueue.add(jRequest);
    }

    public void RefreshToken(final Context context){
        String url="";
        RequestQueue MyRequestQueue = Volley.newRequestQueue(context);

        url = "https://socialclub.rockstargames.com/connect/refreshaccess"; // url del server


        StringRequest jRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("REFRESH_RESPONSE",response  );
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //errore dal server

                status = error.networkResponse.statusCode;

            }
        }){
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                params.put("accessToken",getValueByName("BearerToken"));
                return params;
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getHeaders()  {
                Map<String, String> params = new HashMap<>();
                params.put("Accept","*/*");
                // params.put("Accept-Encoding","gzip,deflate,br");
                 params.put("content-type", "application/x-www-form-urlencoded; charset=utf-8");
                // params.put("requestverificationcode","9D4pGB7e30I-DpY3iOiaCJJOo7mJxZ-QVyPS3eQNNp7tu5Q_RbUR6KbJAv6Suw1mIQOrohHWBHj5mk-PvvW3-agQAS3L9Y5cERiOFuuXnQMqOfTvXUN8Ohs6Z_oQbyrw0DnUdw2");
                params.put("Cookie", HeaderToStringAll());
                params.put("x-requested-with","XMLHttpRequest");
                params.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36");
                return params;
            }


            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {

                status = response.statusCode;
                List<Header> headers = response.allHeaders;

                //Header[name = "Set-Cookie", value="BearerToken=XXXXXXX"] -> splitto value (=) per creare nuovo Header.
                for(int i=0;i<headers.size();i++){
                    if(headers.get(i).getName().equals("Set-Cookie")){

                         Header h = StringToHeader(headers.get(i).getValue());
                        SostituisciCookie(h);
                    }
                }

                OverallAPI(context);
                return super.parseNetworkResponse(response);
            }
        };

        MyRequestQueue.add(jRequest);
    }

    public void SostituisciCookie(Header h){

        for(int i=0;i<cookie.size();i++){
            if(cookie.get(i).getName().equals(h.getName())){

                Header h2 = new Header(h.getName(), h.getValue().replace("domain;",""));
                cookie.remove(i);
                cookie.add(h2);
                break;
            }
        }
    }

    public void StringToHeaderAll(String cookies) {

        if (cookies != null) {
            String[] cookieParts = cookies.split(";"); // escape .

            for (int i = 0; i < cookieParts.length; i++) {

                    if(cookieParts[i].contains("=") && !cookieParts[i].contains("RMT")) {
                        String[] coppia = cookieParts[i].split("=");

                        if (!hasName(coppia[0])) {
                         //   Log.d("COPPIA",coppia[0]+"="+coppia[1]);
                            cookie.add(new Header(coppia[0], coppia[1]));
                        }
                    }
            }
        }
    }

    public String HeaderToStringAll(){
        StringBuilder sb = new StringBuilder();
        for (Header s : cookie)
        {
            sb.append(s.getName());
            sb.append("=");
            sb.append(s.getValue());
            sb.append(";");
        }
     //   Log.d("HEADER_TO_STRING",sb.toString());
        return sb.toString();
    }

    public Header StringToHeader(String s){
        String[] coppia = s.split("=");
        return new Header(" "+coppia[0],coppia[1]);
    }

    public String getValueByName(String name){

         String value="";

        for(int i=0;i<cookie.size();i++){
            if(cookie.get(i).getName().equals(" "+name)){
                value =  cookie.get(i).getValue();
            }
        }
        return value;
    }

    public boolean hasName(String name){
        for (Header s : cookie) {
                if(s.getName().equals(name))
                return true;
            }
        return false;
    }

    public void parseStatsHTML(String divId){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        tl.removeAllViews();
        Document doc = Jsoup.parse(statistics);


        Element carriera = doc.select("div#"+divId).first();
        int i =0,k=0;
        for (Element row : carriera.select("tr")) {

            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            for (Element tds : row.select("td")) {

                TextView td = new TextView(this);
                td.setText(tds.text());

                if(i%2==0) {
                    td.setWidth((int) (width * 0.6f));
                }
                else {
                    td.setWidth((int) (width * 0.4f));
                  td.setBackgroundResource(R.drawable.tabletd);
                  td.setGravity(Gravity.END);

                }

                tr.addView(td);
                i++;
            }
            if(k%2==0)
                tr.setBackgroundResource(R.drawable.tablerow_1);
            else
                tr.setBackgroundResource(R.drawable.tablerow_2);

            k++;
            tl.addView(tr);
        }


        if(tl.getParent() != null) {
            ((ViewGroup)tl.getParent()).removeView(tl); // <- fix
        }
        ScrollView sv  = findViewById(R.id.idScroll);
        sv.addView(tl);
        sv.fullScroll(View.FOCUS_UP);

        tl.setAlpha(1.0f);


    }

    public void parseOverallHTML() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        tl.removeAllViews();

        Document doc = Jsoup.parse(overall);
        Log.d("PARSE", overall);

        Element level = doc.select("h3").first();
        Element timePlay = doc.select("h4").first();
        Element rp = doc.select("h3.left").first();

        Element cashTitle = doc.select("span#cash-title").first();
        Element cashValue = doc.select("span#cash-value").first();
        Element bankTitle = doc.select("span#bank-title").first();
        Element bankValue = doc.select("span#bank-value").first();

        Element gare = doc.select("div[id=competitive-center").first();
        Element wins = doc.select("div[id=competitive-win").first();
        Element loses = doc.select("div[id=competitive-loss").first();
        Element extra = doc.select("div[id=competitive-extra").first();


        int nWin = Integer.parseInt(wins.text().replaceAll("[^0-9]", ""));
        int nLoss = Integer.parseInt(loses.text().replaceAll("[^0-9]", ""));

        String sWin = wins.text().replaceAll("[0-9]", "");
        String sLoss = loses.text().replaceAll("[0-9]", "");
        String[] timePlays = timePlay.text().split(":");

        DecimalFormat decimal = new DecimalFormat("0.00");
        String pWin = "("+decimal.format((float) (nWin * 100 * 1.0) / (nWin + nLoss))+"%)";
        String pLoss = "("+decimal.format((float) (nLoss * 100 * 1.0) / (nWin + nLoss))+"%)";

        Typeface typeface = ResourcesCompat.getFont(this, R.font.bellotatext_bold);


        //riga1
        {
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView td = new TextView(this);
            td.setTextSize(18);
            td.setTypeface(typeface);
            td.setText(bankTitle.text().toUpperCase());
            td.setWidth((int) (width * 0.5f));
            tr.addView(td);

            td = new TextView(this);
            td.setTypeface(typeface);
            td.setTextSize(18);
            td.setBackgroundResource(R.drawable.tabletd);
            td.setGravity(Gravity.END);
            td.setText(bankValue.text());
            td.setWidth((int) (width * 0.5f));
            tr.addView(td);

            tr.setBackgroundResource(R.drawable.tablerow_1);
            tl.addView(tr);
        }

        //riga2
        {
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView td = new TextView(this);
            td.setTextSize(18);
            td.setTypeface(typeface);
            td.setText(cashTitle.text().toUpperCase());
            td.setWidth((int) (width * 0.5f));
            tr.addView(td);

            td = new TextView(this);
            td.setTextSize(18);
            td.setTypeface(typeface);
            td.setBackgroundResource(R.drawable.tabletd);
            td.setGravity(Gravity.END);
            td.setText(cashValue.text());
            td.setWidth((int) (width * 0.5f));
            tr.addView(td);

            tr.setBackgroundResource(R.drawable.tablerow_2);
            tl.addView(tr);
        }

        //riga3
        {
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView td = new TextView(this);
            td.setTextSize(18);
            td.setTypeface(typeface);
            td.setText(timePlays[0].toUpperCase());
            td.setWidth((int) (width * 0.5f));
            tr.addView(td);

            td = new TextView(this);
            td.setTypeface(typeface);
            td.setTextSize(18);
            td.setBackgroundResource(R.drawable.tabletd);
            td.setGravity(Gravity.END);
            td.setText(timePlays[1]);
            td.setWidth((int) (width * 0.5f));
            tr.addView(td);

            tr.setBackgroundResource(R.drawable.tablerow_1);
            tl.addView(tr);
        }

        //riga4
        {
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView td = new TextView(this);
            td.setTextSize(18);
            td.setTypeface(typeface);
            td.setText(gare.text().toUpperCase());
            td.setWidth((int) (width * 0.5f));
            tr.addView(td);

            td = new TextView(this);
            td.setTextSize(18);
            td.setTypeface(typeface);
            td.setBackgroundResource(R.drawable.tabletd);
            td.setGravity(Gravity.END);
            td.setText(String.valueOf(nWin+nLoss));
            td.setWidth((int) (width * 0.5f));
            tr.addView(td);

            tr.setBackgroundResource(R.drawable.tablerow_2);
            tl.addView(tr);
        }

        //riga5
        {
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView td = new TextView(this);
            td.setTextSize(18);
            td.setTypeface(typeface);
            td.setText(sWin.toUpperCase());
            td.setWidth((int) (width * 0.5f));
            tr.addView(td);

            td = new TextView(this);
            td.setTextSize(18);
            td.setTypeface(typeface);
            td.setBackgroundResource(R.drawable.tabletd);
            td.setGravity(Gravity.END);
            td.setText(nWin + "   " + pWin);
            td.setWidth((int) (width * 0.5f));
            tr.addView(td);

            tr.setBackgroundResource(R.drawable.tablerow_1);
            tl.addView(tr);
        }

        //riga6
        {
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView td = new TextView(this);
            td.setTextSize(18);
            td.setTypeface(typeface);
            td.setText(sLoss.toUpperCase());
            td.setWidth((int) (width * 0.5f));
            tr.addView(td);

            td = new TextView(this);
            td.setTextSize(18);
            td.setTypeface(typeface);
            td.setBackgroundResource(R.drawable.tabletd);
            td.setGravity(Gravity.END);
            td.setText(nLoss + "   " + pLoss);
            td.setWidth((int) (width * 0.5f));
            tr.addView(td);

            tr.setBackgroundResource(R.drawable.tablerow_2);
            tl.addView(tr);
        }

        if(tl.getParent() != null) {
            ((ViewGroup)tl.getParent()).removeView(tl); // <- fix
        }
        ScrollView sv  = findViewById(R.id.idScroll);
        sv.addView(tl);
        sv.fullScroll(View.FOCUS_UP);
        tl.setAlpha(1.0f);

        if(!sp.isEnabled()){
            sp.setEnabled(true);
            sp.setVisibility(View.VISIBLE);
        }
    }

    public void UpdateData(MenuItem item) throws IOException, InterruptedException {

        if (!ConnectionHelper.isConnected()) {
            Toast.makeText(this, "Check your internet connection", Toast.LENGTH_LONG).show();
        } else {

            if (isLoaded) {
                Toast.makeText(this, "Updating online statistics..", Toast.LENGTH_SHORT).show();

                pb.setVisibility(View.VISIBLE);
                tl.setAlpha(0.5f);
                OverallAPI(this);
                StatisticsAPI(this);
            }
        }
    }

    public void LoadEvents(MenuItem item) {
        Toast.makeText(this,"Online events coming soon...",Toast.LENGTH_LONG).show();
    }
}

