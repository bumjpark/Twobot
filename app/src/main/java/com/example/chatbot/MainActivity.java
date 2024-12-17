package com.example.chatbot;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private TextView geminiResponse, chatGptResponse;
    private ImageButton geminiExpandButton, chatGptExpandButton;
    private EditText inputField;
    private ImageView selectedImageView;
    private RequestQueue requestQueue;
    private View initialMessage;

    private static final int CAMERA_PERMISSION_CODE = 100;

    private SpeechRecognizerHelper speechRecognizerHelper;
    private PhotoPickerHelper photoPickerHelper;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private Dialog dialog;
    private List<ChatMessage> chatHistory; // ChatMessage 타입의 대화 기록 리스트
    private RecyclerView chatRecyclerView;

    private List<ChatMessage> geminiChatHistory; // Gemini 대화 기록
    private List<ChatMessage> chatGptChatHistory; // ChatGPT 대화 기록
    private ChatMessageAdapter geminiAdapter;
    private ChatMessageAdapter chatGptAdapter;
    private RecyclerView geminiRecyclerView;
    private RecyclerView chatGptRecyclerView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        // View 초기화
        ImageButton photoButton = findViewById(R.id.photoButton);
        ImageView selectedImageView = findViewById(R.id.selectedImageView);
        geminiExpandButton = findViewById(R.id.geminiExpandButton);
        chatGptExpandButton = findViewById(R.id.chatGptExpandButton);
        inputField = findViewById(R.id.inputField);
        initialMessage = findViewById(R.id.initialMessage);
        selectedImageView = findViewById(R.id.selectedImageView);
        ImageButton sendButton = findViewById(R.id.sendButton);
        ImageButton microphoneButton = findViewById(R.id.microphoneButton);


        // Gemini와 ChatGPT 대화 기록 초기화
        geminiChatHistory = new ArrayList<>();
        chatGptChatHistory = new ArrayList<>();

        // RecyclerView 초기화
        geminiRecyclerView = findViewById(R.id.geminiRecyclerView);
        chatGptRecyclerView = findViewById(R.id.chatGptRecyclerView);

        geminiRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatGptRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        geminiAdapter = new ChatMessageAdapter(geminiChatHistory);
        chatGptAdapter = new ChatMessageAdapter(chatGptChatHistory);

        geminiRecyclerView.setAdapter(geminiAdapter);
        chatGptRecyclerView.setAdapter(chatGptAdapter);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        ImageButton menuButton = findViewById(R.id.menuButton);

        // 네트워크 요청 큐 초기화
        requestQueue = Volley.newRequestQueue(this);

        // 음성 인식 및 사진 선택 헬퍼 초기화
        speechRecognizerHelper = new SpeechRecognizerHelper(this);


        ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        photoPickerHelper.handleCameraResult(result.getData());
                    }
                }
        );
        photoPickerHelper = new PhotoPickerHelper(this, cameraLauncher, selectedImageView, detectedText -> {
            inputField.setText(detectedText); // 인식된 텍스트를 입력 필드에 설정
            sendChatRequest(); // 자동으로 전송
        });
        photoPickerHelper = new PhotoPickerHelper(this, cameraLauncher, selectedImageView, detectedText -> {
            // 인식된 텍스트를 입력 필드에 설정하고 전송
            inputField.setText(detectedText);
            sendChatRequest();
        });
        // 전송 버튼 클릭 이벤트
        sendButton.setOnClickListener(v -> sendChatRequest());
        // 음성 인식 버튼 클릭 이벤트
        microphoneButton.setOnClickListener(v -> speechRecognizerHelper.startSpeechToText());

        // 사진 선택 버튼 클릭 이벤트
        photoButton.setOnClickListener(v -> photoPickerHelper.openCamera());


        // 메뉴 버튼 클릭 시 사이드 메뉴 열기
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        // 메뉴 항목 선택 이벤트 처리
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_chatbots) {
                // 기본 화면으로 이동
                Toast.makeText(this, "Two Bot 선택", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_debate) {
                // 토론 화면으로 이동
                startActivity(new Intent(MainActivity.this, DebateActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            return false;
        });


        // 전체보기 버튼 클릭 이벤트
        geminiExpandButton.setOnClickListener(v -> showFullResponseDialog("Gemini 응답", true));
        chatGptExpandButton.setOnClickListener(v -> showFullResponseDialog("ChatGPT 응답", false));


    }


    private void sendChatRequest() {
        String prompt = inputField.getText().toString().trim();
        if (prompt.isEmpty()) {
            Toast.makeText(this, "질문을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 사용자 메시지를 히스토리에 추가
        geminiChatHistory.add(new ChatMessage("User", prompt));
        chatGptChatHistory.add(new ChatMessage("User", prompt));

        // 서버 요청 URL 설정
        String url = "http://10.0.2.2:5000/chat";

        // 대화 히스토리를 JSON 배열로 변환
        JSONArray historyArray = new JSONArray();
        try {
            for (ChatMessage message : chatGptChatHistory) {
                JSONObject messageJson = new JSONObject();
                String role = message.getSender().equalsIgnoreCase("User") ? "user" : "assistant";
                messageJson.put("role", role);
                messageJson.put("content", message.getMessage());
                historyArray.put(messageJson);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // 요청 본문 생성
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("prompt", prompt);
            requestBody.put("history", historyArray);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    try {
                        String chatGpt = response.getString("chatgpt_response");
                        String gemini = response.getString("gemini_response");

                        // Gemini와 ChatGPT의 응답을 히스토리에 추가
                        geminiChatHistory.add(new ChatMessage("Gemini", gemini));
                        chatGptChatHistory.add(new ChatMessage("assistant", chatGpt));

                        // UI 업데이트
                        updateChatViews();

                        // 초기 메시지 숨기기 및 카드 보이기
                        initialMessage.setVisibility(View.GONE);
                        findViewById(R.id.geminiCard).setVisibility(View.VISIBLE);
                        findViewById(R.id.chatGptCard).setVisibility(View.VISIBLE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "응답 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(MainActivity.this, "서버 연결 실패: " + error.toString(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );

        // 타임아웃 시간 설정 (예: 60초)
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(jsonObjectRequest);

        // 입력 필드 초기화
        inputField.setText("");
    }


    private void updateChatViews() {
        // RecyclerView에 추가된 데이터 반영
        geminiAdapter.notifyDataSetChanged();
        chatGptAdapter.notifyDataSetChanged();

        // RecyclerView를 마지막 메시지로 스크롤
        geminiRecyclerView.scrollToPosition(geminiChatHistory.size() - 1);
        chatGptRecyclerView.scrollToPosition(chatGptChatHistory.size() - 1);
    }


    // 전체보기 다이얼로그
    private void showFullResponseDialog(String title, boolean isGemini) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_full_response);

        TextView titleTextView = dialog.findViewById(R.id.dialogTitle);
        RecyclerView dialogRecyclerView = dialog.findViewById(R.id.dialogRecyclerView);
        EditText dialogInputField = dialog.findViewById(R.id.dialogInputField);
        ImageButton closeButton = dialog.findViewById(R.id.closeButton);
        ImageButton sendButton = dialog.findViewById(R.id.dialogSendButton);
        ImageButton dialogMicrophoneButton = dialog.findViewById(R.id.dialogMicrophoneButton);
        ImageButton dialogPhotoButton = dialog.findViewById(R.id.dialogPhotoButton);

        titleTextView.setText(title);

        // 대화 기록에서 최신 대화를 가져와 표시
        List<ChatMessage> chatHistory = isGemini ? geminiChatHistory : chatGptChatHistory;
        ChatMessageAdapter adapter = new ChatMessageAdapter(chatHistory);
        dialogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dialogRecyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        dialogRecyclerView.scrollToPosition(chatHistory.size() - 1);
        // 닫기 버튼 이벤트
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // 전송 버튼 이벤트
        sendButton.setOnClickListener(v -> {
            String userInput = dialogInputField.getText().toString().trim();
            if (!userInput.isEmpty()) {
                chatHistory.add(new ChatMessage("User", userInput));
                adapter.notifyDataSetChanged();
                dialogRecyclerView.scrollToPosition(chatHistory.size() - 1);
                sendFullDialogRequest(userInput, isGemini, adapter);
                dialogInputField.setText("");
            } else {
                Toast.makeText(this, "질문을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 음성 인식 버튼 이벤트
        dialogMicrophoneButton.setOnClickListener(v -> speechRecognizerHelper.startSpeechToText());

        // 사진 선택 버튼 이벤트
        dialogPhotoButton.setOnClickListener(v -> photoPickerHelper.hashCode());

        // 다이얼로그 크기 설정
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.95),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.9)
        );

        dialog.show();
    }

    // 대화 내용을 갱신하는 메서드
    private void updateChatView(TextView contentTextView, List<ChatMessage> chatHistory) {
        StringBuilder conversation = new StringBuilder();
        for (ChatMessage message : chatHistory) {
            conversation.append(message.getSender()).append(": ").append(message.getMessage()).append("\n\n");
        }
        contentTextView.setText(conversation.toString().trim());
    }


    private void sendFullDialogRequest(String input, boolean isGemini, ChatMessageAdapter adapter) {
        String url = "http://10.0.2.2:5000/chat";

        // 기존 채팅 기록을 포함한 history 배열 생성
        JSONArray historyArray = new JSONArray();
        List<ChatMessage> chatHistory = isGemini ? geminiChatHistory : chatGptChatHistory;

        try {
            for (ChatMessage message : chatHistory) {
                JSONObject messageJson = new JSONObject();
                messageJson.put("role", message.getSender().equals("User") ? "user" : "assistant");
                messageJson.put("content", message.getMessage());
                historyArray.put(messageJson);
            }

            // 사용자 입력을 history에 추가
            JSONObject userInputJson = new JSONObject();
            userInputJson.put("role", "user");
            userInputJson.put("content", input);
            historyArray.put(userInputJson);

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // 요청 본문 생성
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("history", historyArray);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    try {
                        String responseText = isGemini ?
                                response.getString("gemini_response") :
                                response.getString("chatgpt_response");

                        chatHistory.add(new ChatMessage(isGemini ? "Gemini" : "ChatGPT", responseText));
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "응답 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(MainActivity.this, "서버 연결 실패: " + error.toString(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
}