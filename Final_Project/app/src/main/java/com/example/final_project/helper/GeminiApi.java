package com.example.final_project.helper;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.example.final_project.BuildConfig;

public class GeminiApi {
//    private static final String BASE_URL =
//            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
//
//    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
//    private final OkHttpClient client = new OkHttpClient();
//    private final Gson gson = new Gson();
//
//    public interface Callback {
//        void onSuccess(String reply);
//        void onError(Exception e);
//    }
//
//    public void sendMessage(String userMessage, Callback callback) {
//        String systemInstruction = "Bạn là một chuyên gia về ẩm thực và nấu ăn. " +
//                "Bạn có thể trả lời các câu hỏi về món ăn, nguyên liệu, công thức, dinh dưỡng hoặc các chủ đề liên quan đến thực phẩm. " +
//                "Nếu người dùng chào hỏi hoặc nói chuyện thân mật (ví dụ: 'hello', 'chào bạn', 'bạn khỏe không'), hãy trả lời thân thiện. " +
//                "Tuy nhiên, nếu câu hỏi rõ ràng không liên quan đến ẩm thực, nấu ăn hay thực phẩm, hãy trả lời ngắn gọn: 'Xin lỗi, tôi chỉ trả lời về thức ăn và món ăn thôi.'";
//
//        // Tạo JSON yêu cầu gửi tới API
//        JsonArray contents = new JsonArray();
//
//        JsonObject userPart = new JsonObject();
//        userPart.addProperty("text", systemInstruction + "\nNgười dùng: " + userMessage);
//
//        JsonObject userContent = new JsonObject();
//        userContent.addProperty("role", "user");
//
//        JsonArray parts = new JsonArray();
//        parts.add(userPart);
//        userContent.add("parts", parts);
//
//        contents.add(userContent);
//
//        JsonObject requestJson = new JsonObject();
//        requestJson.add("contents", contents);
//
//        RequestBody body = RequestBody.create(requestJson.toString(), JSON);
//        Request request = new Request.Builder()
//                .url(BASE_URL)
//                .addHeader("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
//                .post(body)
//                .build();
//
//        new Thread(() -> {
//            try (Response response = client.newCall(request).execute()) {
//                if (!response.isSuccessful())
//                    throw new IOException("Unexpected code " + response);
//
//                String responseBody = response.body().string();
//                JsonObject json = gson.fromJson(responseBody, JsonObject.class);
//
//                String botReply = json.getAsJsonArray("candidates")
//                        .get(0).getAsJsonObject()
//                        .getAsJsonObject("content")
//                        .getAsJsonArray("parts")
//                        .get(0).getAsJsonObject()
//                        .get("text").getAsString();
//
//                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(botReply));
//            } catch (Exception e) {
//                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
//            }
//        }).start();
//    }

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // ---- Singleton client (reuse) ----
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(chain -> {
                // simple retry/backoff for 408/429/5xx
                Request req = chain.request();
                int tries = 0;
                long backoff = 600; // ms
                IOException lastEx = null;
                while (tries < 2) {
                    try {
                        Response res = chain.proceed(req);
                        int code = res.code();
                        if (code != 408 && code != 429 && (code < 500 || code >= 600)) {
                            return res; // OK or client error other than 408/429
                        }
                        res.close();
                    } catch (IOException ex) {
                        lastEx = ex;
                    }
                    try { Thread.sleep(backoff); } catch (InterruptedException ignored) {}
                    backoff *= 2;
                    tries++;
                }
                if (lastEx != null) throw lastEx;
                return chain.proceed(req);
            })
            .build();

    private final Gson gson = new Gson();

    public interface Callback {
        void onSuccess(String reply);
        void onError(Exception e);
    }

    public void sendMessage(String userMessage, Callback callback) {
        String systemInstruction =
                "Bạn là chuyên gia ẩm thực, chỉ trả lời về món ăn/nguyên liệu/công thức. " +
                        "Luôn trả lời ngắn gọn, có bước nấu. Nếu thiếu nguyên liệu, hãy gợi ý thay thế.";


        // build request JSON
        JsonArray contents = new JsonArray();
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", systemInstruction + "\nNgười dùng: " + userMessage);
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray parts = new JsonArray();
        parts.add(userPart);
        userContent.add("parts", parts);
        contents.add(userContent);

        JsonObject requestJson = new JsonObject();
        requestJson.add("contents", contents);

        RequestBody body = RequestBody.create(requestJson.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
                .post(body)
                .build();

        // async (không block UI) + timeout/retry đã cấu hình ở CLIENT
        new Thread(() -> {
            try (Response response = CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful())
                    throw new IOException("HTTP " + response.code() + " - " + response.message());

                String responseBody = response.body().string();
                JsonObject json = gson.fromJson(responseBody, JsonObject.class);

                // parse an toàn (tránh NPE)
                String botReply = "";
                JsonArray candidates = json.has("candidates") ? json.getAsJsonArray("candidates") : null;
                if (candidates != null && candidates.size() > 0) {
                    JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
                    if (content != null) {
                        JsonArray replyParts = content.getAsJsonArray("parts");
                        if (replyParts != null && replyParts.size() > 0) {
                            JsonElement t = replyParts.get(0).getAsJsonObject().get("text");
                            if (t != null) botReply = t.getAsString();
                        }
                    }
                }
                String finalReply = botReply.isEmpty() ? "Xin lỗi, mình chưa nhận được phản hồi hợp lệ." : botReply;
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(finalReply));

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        }).start();
    }
}
