package com.example.ssutudy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ssutudy.databinding.ActivityTestBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestActivity extends AppCompatActivity {
    private ActivityTestBinding binding;
    int dan, step;
    String cookies;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dan=0;
        step=0;
        int studentNum = 20212985;
        String pw = "ssut1!";
        String name;


        binding.webview.getSettings().setJavaScriptEnabled(true);
        binding.webview.addJavascriptInterface(new JavascriptInterface(), "Android");
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);
        cookieManager.flush();
        binding.webview.clearCache(true);


        binding.webview.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(dan==0) {
                    view.loadUrl("javascript:document.getElementById('userid').value='" + studentNum +
                            "';document.getElementById('pwd').value='" + pw + "';" +
                            "document.getElementsByClassName('btn_login')[0].click();");
                }else if(dan==1){
                    Log.d("html", "로그인성공");
                    view.loadUrl("https://saint.ssu.ac.kr/irj/portal");
                }else if(dan==2){
                    view.loadUrl("javascript:document.getElementsByClassName('btn_login')[0].click();");
                } else if (dan == 3) {
                    view.loadUrl("https://saint.ssu.ac.kr/irj/portal");
                } else if(dan==4){
                    view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                    view.loadUrl("https://ecc.ssu.ac.kr:8443/sap/bc/webdynpro/SAP/ZCMW2110#");
                }else if(dan==5){
                    view.evaluateJavascript("var originalXhrOpen = XMLHttpRequest.prototype.open;" +
                            "XMLHttpRequest.prototype.open = function(method, url) {" +
                            "  this.addEventListener('load', function() {" +
                            "    Android.receiveAjaxResponse(this.responseText);" +
                            "  });" +
                            "  originalXhrOpen.apply(this, arguments);" +
                            "};", null);
                }else{

                }
                dan++;
            }
        });
        binding.webview.loadUrl("https://smartid.ssu.ac.kr/Symtra_sso/smln.asp");

        
    }

    class JavascriptInterface {
        public String name="";
        public List<List<String>> results = new ArrayList<>();

        @android.webkit.JavascriptInterface
        public void getHtml(String response){
            String regexName=">(.*)님 접속을 환영합니다.";
            String[] words = response.split("\n");
            for (String word : words) {
                Matcher matcherName = Pattern.compile(regexName).matcher(word);
                if (matcherName.find()) {
                    String foundName = matcherName.group(1);
                    Log.d("html", foundName);
                    name=foundName;
                    break;
                }
            }

        }

        @android.webkit.JavascriptInterface
        public void receiveAjaxResponse(String response) {
            if(response.startsWith("<updates>")) {
                String longString = response;
                int last1 = longString.lastIndexOf("출력");
                int last2 = longString.lastIndexOf("본인&#x20;신청");
                Log.d("html", last1 + " : " + last2);
                String text = longString.substring(last1, last2);

                int maxLogSize = 4000;
                for (int i = 0; i <= longString.length() / maxLogSize; i++) {
                    int start = i * maxLogSize;
                    int end = Math.min((i + 1) * maxLogSize, longString.length());
                    //Log.d("html", longString.substring(start, end));
                }

                String regexDigits = ">(\\d{10})<";
                String regexCourse = "lsTextView--wrap\">(.*)</span></span>";
                String regexTime1 = "([가-힣])&#x20;([0-9]+)&#x3a;([0-9]+)-([0-9]+)&#x3a;([0-9]+)";
                String regexTime2 = "([가-힣])&#x20;([가-힣])&#x20;([0-9]+)&#x3a;([0-9]+)-([0-9]+)&#x3a;([0-9]+)";
                String regexTrigger = "본인&#x20;신청";

                // 2차원 배열을 List로 구현
                List<List<String>> result = new ArrayList<>();
                boolean trigger1 = true; // 트리거 문자열 발견 여부
                boolean trigger2 = true;

                // 문자열을 공백으로 나누어 처리
                text=text.replace("<br>", " ");
                String[] words = text.split(" ");
                List<String> currentRow = new ArrayList<>();
                for (String word : words) {

                    Matcher matcherTrigger = Pattern.compile(regexTrigger).matcher(word);
                    if (matcherTrigger.find()) {
                        trigger1 = true;
                        trigger2 = true;
                        result.add(currentRow);
                        currentRow.clear();
                        continue;
                    }

                    // 10자리 숫자 찾기
                    Matcher matcherDigits = Pattern.compile(regexDigits).matcher(word);
                    if (matcherDigits.find() && trigger1) {
                        String foundNumber = matcherDigits.group(1);
                        currentRow.add(foundNumber);
                        trigger1 = false;
                        Log.d("html", foundNumber);

                    }
                    // 한글 단어 찾기
                    Matcher matcherCourse = Pattern.compile(regexCourse).matcher(word);
                    if (matcherCourse.find() && trigger2 && !trigger1) {
                        String foundCourse = decodeHtml(matcherCourse.group(1));
                        currentRow.add(foundCourse);
                        trigger2 = false;
                        Log.d("html", foundCourse);
                    }

                    Matcher matcherTime1 = Pattern.compile(regexTime1).matcher(word);
                    Matcher matcherTime2 = Pattern.compile(regexTime2).matcher(word);
                    if(matcherTime2.find() && !trigger1 && !trigger2){
                        String foundTime1 = matcherTime2.group(1) + " " + matcherTime2.group(3) + ":" + matcherTime2.group(4) + "-"+matcherTime2.group(5) + ":" + matcherTime2.group(6);
                        String foundTime2 = matcherTime2.group(2) + " " + matcherTime2.group(3) + ":" + matcherTime2.group(4) + "-"+matcherTime2.group(5) + ":" + matcherTime2.group(6);
                        currentRow.add(foundTime1);
                        currentRow.add(foundTime2);
                        Log.d("html", foundTime1);
                        Log.d("html", foundTime2);
                    }
                    else if(matcherTime1.find() && !trigger1 && !trigger2){
                        String foundTime = matcherTime1.group(1) + " " + matcherTime1.group(2) + ":" + matcherTime1.group(3) + "-"+matcherTime1.group(4) + ":" + matcherTime1.group(5);
                        currentRow.add(foundTime);
                        Log.d("html", foundTime);
                    }
                }
                results=result;
            }
        }
        public String decodeHtml(String input){
            return input
                    .replace("&#xb7;", "·")
                    .replace("&#x20;", " ")
                    .replace("&#x28;", "(")
                    .replace("&#x29;", ")")
                    .replace("&nbsp;", " ")
                    .replace("&#x5b;", "[")
                    .replace("&#x5d;", "]")
                    .replace("&#x3a;", ":");
        }
    }
}



