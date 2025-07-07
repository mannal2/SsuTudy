package com.example.ssutudy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ssutudy.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    int dan;
    String studentNum;
    String pw;
    FirebaseFirestore db;
    boolean loginState;
    public String name;
    SharedPreferences spf;
    final int autoLoginDay=1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dan=0;
        db = FirebaseFirestore.getInstance();
        name="";

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
                    binding.login.setText("재로그인");
                    binding.login.setEnabled(true);
                    view.loadUrl("javascript:document.getElementById('userid').value='" + studentNum +
                            "';document.getElementById('pwd').value='" + pw + "';" +
                            "document.getElementsByClassName('btn_login')[0].click();");
                }else if(dan==1){
                    binding.login.setText("로그인중");
                    binding.login.setEnabled(false);
                    Log.d("html", "dan1");
                    view.loadUrl("https://saint.ssu.ac.kr/irj/portal");
                }else if(dan==2){
                    Log.d("html", "dan2");
                    view.loadUrl("javascript:document.getElementsByClassName('btn_login')[0].click();");
                } else if (dan == 3) {
                    Log.d("html", "dan3");
                    view.loadUrl("https://saint.ssu.ac.kr/irj/portal");
                } else if(dan==4) {
                    Log.d("html", "dan4");
                    view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);");
                    view.loadUrl("https://ecc.ssu.ac.kr:8443/sap/bc/webdynpro/SAP/ZCMW2110#");
                }
                else if(dan==5){
                    Log.d("html", "dan5");
                    view.evaluateJavascript("var originalXhrOpen = XMLHttpRequest.prototype.open;" +
                            "XMLHttpRequest.prototype.open = function(method, url) {" +
                            "  this.addEventListener('load', function() {" +
                            "    Android.receiveAjaxResponse(this.responseText);" +
                            "  });" +
                            "  originalXhrOpen.apply(this, arguments);" +
                            "};", null);
                }
                dan++;
            }
        });

        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dan=0;
                studentNum = binding.studentNumber.getText().toString();
                pw = binding.password.getText().toString();
                if(studentNum.isEmpty() || pw.isEmpty()) return;
                loginState=false;
                binding.webview.loadUrl("https://smartid.ssu.ac.kr/Symtra_sso/smln.asp");

            }
        });
    }

    class JavascriptInterface {

        public List<List<String>> results = new ArrayList<>();


        @android.webkit.JavascriptInterface
        public void getHtml(String response){
            String regexName=">(.*)님 접속을 환영합니다.";
            String[] words = response.split("\n");
            for (String word : words) {
                Matcher matcherName = Pattern.compile(regexName).matcher(word);
                if (matcherName.find()) {
                    DocumentReference docRef = db.collection("users").document(studentNum);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                //자동로그인 설정
                                spf = getSharedPreferences("auto_login", Context.MODE_PRIVATE);
                                Date dt = new Date();
                                Calendar c = Calendar.getInstance();
                                c.setTime(dt);
                                c.add(Calendar.DATE, autoLoginDay);
                                dt = c.getTime();
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                String dd = formatter.format(dt);
                                SharedPreferences.Editor editor = spf.edit();
                                editor.putString("studentNum", studentNum);
                                editor.putString("limitDate", dd);
                                editor.apply();

                                if(document.exists()){ //이미 회원가입 되어있을때
                                    //홈으로 이동
                                    binding.login.setText("로그인");
                                    binding.login.setEnabled(true);
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    intent.putExtra("studentNum", studentNum);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }else{
                                    loginState = true;
                                    String foundName = matcherName.group(1);
                                    name=foundName;
                                    Log.d("html", "회원가입");
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("name", foundName);
                                    data.put("studentNum", studentNum);
                                    data.put("totalStudyTime", 0);
                                    data.put("state", "쉬는중");
                                    data.put("alias", foundName);
                                    data.put("image_url", "blank");
                                    db.collection("users").document(studentNum).set(data);
                                }
                            }
                        }
                    });
                    break;
                }
            }
        }

        @android.webkit.JavascriptInterface
        public void receiveAjaxResponse(String response) {
            if(response.startsWith("<updates>") && loginState) {
                String longString = response;
                int last1 = longString.lastIndexOf("비고");
                int last2 = longString.lastIndexOf("본인&#x20;신청");
                if (last1 ==-1 || last2==-1) {
                    Toast.makeText(LoginActivity.this, "유세인트 화면의 이전학기 버튼이나 \n다음학기 버튼을 눌러주세요", Toast.LENGTH_LONG).show();
                    return;
                }
                last2 += ("본인&#x20;신청".length()+1);

                //Log.d("html", last1 + " : " + last2);
                String text = longString.substring(last1, last2);

                int maxLogSize = 4000;
                for (int i = 0; i <= longString.length() / maxLogSize; i++) {
                    int start = i * maxLogSize;
                    int end = Math.min((i + 1) * maxLogSize, longString.length());
                    //Log.d("html", longString.substring(start, end));
                }

                String regexDigits = ">(\\d{10})<";
                String regexCourse = "lsTextView--wrap\">(.*)</span></span>";
                String regexTime1 = "([가-힣])&#x20;([0-9]+)&#x3a;([0-9]+)-([0-9]+)&#x3a;([0-9]+)&#x20;&#x28;(.*)-";
                String regexTime2 = "([가-힣])&#x20;([가-힣])&#x20;([0-9]+)&#x3a;([0-9]+)-([0-9]+)&#x3a;([0-9]+)&#x20;&#x28;(.*)-";
                String regexTrigger = "본인&#x20;신청";
                String regexTrigger2 = "관리자&#x20;지정";

                boolean trigger1 = true; // 트리거 문자열 발견 여부
                boolean trigger2 = true;
                Map<String, Object> data = new HashMap<>();
                List<String> currentRow = new ArrayList<>();

                // 문자열을 공백으로 나누어 처리
                text=text.replace("<br>", " ");
                String[] words = text.split(" ");

                for (String word : words) {

                    Matcher matcherTrigger = Pattern.compile(regexTrigger).matcher(word);
                    Matcher matcherTrigger2 = Pattern.compile(regexTrigger2).matcher(word);
                    if (matcherTrigger.find() || matcherTrigger2.find()) {
                        trigger1 = true;
                        trigger2 = true;
                        data.put("studentNum", studentNum);
                        data.put("courseStudyTime", 0);
                        data.put("courseTime", currentRow);
                        db.collection("course").add(data);
                        //Log.d("html", "db전송");
                        data.clear();
                        currentRow.clear();
                        continue;
                    }

                    // 10자리 숫자 찾기
                    Matcher matcherDigits = Pattern.compile(regexDigits).matcher(word);
                    if (matcherDigits.find() && trigger1) {
                        String foundNumber = matcherDigits.group(1);
                        data.put("courseNum", foundNumber);
                        trigger1 = false;
                        //Log.d("html", foundNumber);

                    }
                    // 한글 단어 찾기
                    Matcher matcherCourse = Pattern.compile(regexCourse).matcher(word);
                    if (matcherCourse.find() && trigger2 && !trigger1) {
                        String foundCourse = decodeHtml(matcherCourse.group(1));
                        data.put("courseName", foundCourse);
                        trigger2 = false;
                        //Log.d("html", foundCourse);
                    }

                    Matcher matcherTime1 = Pattern.compile(regexTime1).matcher(word);
                    Matcher matcherTime2 = Pattern.compile(regexTime2).matcher(word);
                    if(matcherTime2.find() && !trigger1 && !trigger2){
                        String foundTime1 = matcherTime2.group(1) + " " + matcherTime2.group(3) + ":" + matcherTime2.group(4) + "-"+matcherTime2.group(5) + ":" + matcherTime2.group(6) + " " + decodeHtml(matcherTime2.group(7));
                        String foundTime2 = matcherTime2.group(2) + " " + matcherTime2.group(3) + ":" + matcherTime2.group(4) + "-"+matcherTime2.group(5) + ":" + matcherTime2.group(6) + " " + decodeHtml(matcherTime2.group(7));
                        currentRow.add(foundTime1);
                        currentRow.add(foundTime2);

                        //Log.d("html", foundTime1);
                        //Log.d("html", foundTime2);
                    }
                    else if(matcherTime1.find() && !trigger1 && !trigger2){
                        String foundTime = matcherTime1.group(1) + " " + matcherTime1.group(2) + ":" + matcherTime1.group(3) + "-"+matcherTime1.group(4) + ":" + matcherTime1.group(5) +" " + decodeHtml(matcherTime1.group(6));
                        currentRow.add(foundTime);

                        //Log.d("html", foundTime);
                    }
                }
                Intent intent = new Intent(LoginActivity.this, InitActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("studentNum", studentNum);
                startActivity(intent);
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
                    .replace("&#x3a;", ":")
                    .replace("&#x2b;", "+");
        }
    }
}