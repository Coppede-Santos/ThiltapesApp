package com.example.thiltapeshunting.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.thiltapeshunting.model.GameStatus;
import com.example.thiltapeshunting.model.Player;
import com.example.thiltapeshunting.model.Thiltape;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private static final OkHttpClient okHttpClient;

    static {
        CookieHandler.setDefault(cookieManager);
        okHttpClient = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public interface PlayerCallback {
        void onResult(Player player);
    }

    public interface ThiltapeListCallback {
        void onResult(List<Thiltape> thiltapes);
    }

    public interface BooleanCallback {
        void onResult(boolean ok);
    }

    public interface GameStatusCallback {
        void onResult(GameStatus status);
    }

    public interface PlayerListCallback {
        void onResult(List<Player> players);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void loginAdmin(String user, String pass, BooleanCallback callback) {
        executor.execute(() -> {
            boolean ok = false;
            try {
                URL url = new URL(ApiConfig.BASE_URL + "/admin/login");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setDoOutput(true);

                String payload = "usuario=" + user + "&senha=" + pass;
                writeBody(con, payload);

                int code = con.getResponseCode();
                Log.d(TAG, "loginAdmin status: " + code);
                
                // Salva os cookies recebidos do servidor
                String setCookie = con.getHeaderField("Set-Cookie");
                if (setCookie != null) {
                    android.webkit.CookieManager.getInstance().setCookie(ApiConfig.BASE_URL, setCookie);
                }

                // Se redirecionar ou der 200, consideramos sucesso
                ok = (code >= 200 && code < 400);
                con.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error in loginAdmin", e);
            }
            boolean finalOk = ok;
            mainHandler.post(() -> callback.onResult(finalOk));
        });
    }

    public void criarPlayer(String nome, PlayerCallback callback) {
        executor.execute(() -> {
            Player player = null;
            try {
                URL url = new URL(ApiConfig.BASE_URL + "/player");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("nome", nome);
                writeBody(con, payload.toString());

                int responseCode = con.getResponseCode();
                Log.d(TAG, "criarPlayer: status=" + responseCode);
                if (responseCode >= 200 && responseCode < 300) {
                    String body = readResponse(con);
                    Log.d(TAG, "criarPlayer: response=" + body);
                    JSONObject json = new JSONObject(body);
                    player = new Player(json.getInt("id"), json.optString("nome", nome));
                }
                con.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error in criarPlayer", e);
            }

            Player finalPlayer = player;
            mainHandler.post(() -> callback.onResult(finalPlayer));
        });
    }

    public void atualizarPlayer(int id, String nome, BooleanCallback callback) {
        executor.execute(() -> {
            boolean ok = false;
            try {
                URL url = new URL(ApiConfig.BASE_URL + "/admin/players/" + id);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("PUT");
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("nome", nome);
                writeBody(con, payload.toString());

                ok = (con.getResponseCode() >= 200 && con.getResponseCode() < 300);
                Log.d(TAG, "deletarThiltape: status=" + con.getResponseCode());
                con.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error in deletarThiltape", e);
            }
            boolean finalOk = ok;
            mainHandler.post(() -> callback.onResult(finalOk));
        });
    }

    public void deletarPlayer(int id, BooleanCallback callback) {
        executor.execute(() -> {
            boolean ok = false;
            try {
                URL url = new URL(ApiConfig.BASE_URL + "/admin/players/" + id);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("DELETE");
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);

                ok = (con.getResponseCode() >= 200 && con.getResponseCode() < 300);
                Log.d(TAG, "deletarThiltape: status=" + con.getResponseCode());
                con.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error in deletarThiltape", e);
            }
            boolean finalOk = ok;
            mainHandler.post(() -> callback.onResult(finalOk));
        });
    }

    public void buscarThiltapes(int playerId, double lat, double lng, ThiltapeListCallback callback) {
        executor.execute(() -> {
            List<Thiltape> itens = new ArrayList<>();
            try {
                String endpoint = String.format("%s/thiltapes?playerId=%s&lat=%s&lng=%s", ApiConfig.BASE_URL, playerId, lat, lng);
                URL url = new URL(endpoint);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);

                int responseCode = con.getResponseCode();
                Log.d(TAG, "buscarThiltapes: status=" + responseCode);
                if (responseCode >= 200 && responseCode < 300) {
                    String body = readResponse(con);
                    Log.d(TAG, "buscarThiltapes: response=" + body);
                    JSONArray arr = new JSONArray(body);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        itens.add(new Thiltape(
                                obj.getInt("id"),
                                obj.getString("nome"),
                                obj.optString("raridade", "Comum"),
                                obj.optString("foto", ""),
                                obj.getDouble("lat"),
                                obj.getDouble("lng")
                        ));
                    }
                }
                con.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error in buscarThiltapes", e);
            }

            mainHandler.post(() -> callback.onResult(itens));
        });
    }

    public void capturar(int playerId, int thiltapeId, double lat, double lng, BooleanCallback callback) {
        executor.execute(() -> {
            boolean ok = false;
            try {
                URL url = new URL(ApiConfig.BASE_URL + "/capturar");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("playerId", playerId);
                payload.put("thiltapeId", thiltapeId);
                payload.put("lat", lat);
                payload.put("lng", lng);
                writeBody(con, payload.toString());

                int status = con.getResponseCode();
                Log.d(TAG, "capturar: status=" + status);
                if (status >= 200 && status < 300) {
                    String body = readResponse(con);
                    Log.d(TAG, "capturar: response=" + body);
                    JSONObject json = new JSONObject(body);
                    ok = json.optBoolean("capturado", false);
                }
                con.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error in capturar", e);
            }

            boolean finalOk = ok;
            mainHandler.post(() -> callback.onResult(finalOk));
        });
    }

    public void listarPokedex(int playerId, ThiltapeListCallback callback) {
        executor.execute(() -> {
            List<Thiltape> itens = new ArrayList<>();
            try {
                URL url = new URL(ApiConfig.BASE_URL + "/pokedex?playerId=" + playerId);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);

                int responseCode = con.getResponseCode();
                Log.d(TAG, "listarPokedex: status=" + responseCode);
                if (responseCode >= 200 && responseCode < 300) {
                    String body = readResponse(con);
                    Log.d(TAG, "listarPokedex: response=" + body);
                    JSONArray arr = new JSONArray(body);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        itens.add(new Thiltape(
                                obj.getInt("id"),
                                obj.getString("nome"),
                                obj.optString("raridade", "Comum"),
                                obj.optString("foto", ""),
                                obj.optDouble("lat", 0),
                                obj.optDouble("lng", 0)
                        ));
                    }
                }
                con.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error in listarPokedex", e);
            }

            mainHandler.post(() -> callback.onResult(itens));
        });
    }

    public void statusPartida(int playerId, GameStatusCallback callback) {
        executor.execute(() -> {
            GameStatus status = null;
            try {
                URL url = new URL(ApiConfig.BASE_URL + "/status-partida?playerId=" + playerId);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);

                int responseCode = con.getResponseCode();
                Log.d(TAG, "statusPartida: status=" + responseCode);
                if (responseCode >= 200 && responseCode < 300) {
                    String body = readResponse(con);
                    Log.d(TAG, "statusPartida: response=" + body);
                    JSONObject obj = new JSONObject(body);
                    status = new GameStatus(
                            obj.getInt("playerId"),
                            obj.getInt("totalThiltapes"),
                            obj.getInt("totalCapturados"),
                            obj.getBoolean("finalizada")
                    );
                }
                con.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error in statusPartida", e);
            }

            GameStatus finalStatus = status;
            mainHandler.post(() -> callback.onResult(finalStatus));
        });
    }

    public void buscarTodosAdmin(ThiltapeListCallback callback) {
        executor.execute(() -> {
            List<Thiltape> itens = new ArrayList<>();
            try {
                URL url = new URL(ApiConfig.BASE_URL + "/admin/thiltapes");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);
                
                // Força o envio dos cookies para manter a sessão do admin
                String cookies = android.webkit.CookieManager.getInstance().getCookie(ApiConfig.BASE_URL);
                if (cookies != null) {
                    con.setRequestProperty("Cookie", cookies);
                }

                int responseCode = con.getResponseCode();
                Log.d(TAG, "buscarTodosAdmin: status=" + responseCode);
                if (responseCode >= 200 && responseCode < 300) {
                    String body = readResponse(con);
                    Log.d(TAG, "listarPokedex: response=" + body);
                    JSONArray arr = new JSONArray(body);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        itens.add(new Thiltape(
                                obj.getInt("id"),
                                obj.getString("nome"),
                                obj.optString("raridade", "Comum"),
                                obj.optString("foto", ""),
                                obj.optDouble("lat", 0),
                                obj.optDouble("lng", 0)
                        ));
                    }
                }
                con.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error in listarPokedex", e);
            }
            mainHandler.post(() -> callback.onResult(itens));
        });
    }

    public void deletarThiltape(int id, BooleanCallback callback) {
        executor.execute(() -> {
            boolean ok = false;
            try {
                URL url = new URL(ApiConfig.BASE_URL + "/admin/thiltapes/" + id);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("DELETE");
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);

                ok = (con.getResponseCode() >= 200 && con.getResponseCode() < 300);
                Log.d(TAG, "deletarThiltape: status=" + con.getResponseCode());
                con.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error in deletarThiltape", e);
            }
            boolean finalOk = ok;
            mainHandler.post(() -> callback.onResult(finalOk));
        });
    }

    public void buscarRanking(PlayerListCallback callback) {
        executor.execute(() -> {
            List<Player> itens = new ArrayList<>();
            try {
                URL url = new URL(ApiConfig.BASE_URL + "/admin/ranking");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);

                String cookies = android.webkit.CookieManager.getInstance().getCookie(ApiConfig.BASE_URL);
                if (cookies != null) {
                    con.setRequestProperty("Cookie", cookies);
                }

                int responseCode = con.getResponseCode();
                Log.d(TAG, "buscarRanking: status=" + responseCode);
                if (responseCode >= 200 && responseCode < 300) {
                    String body = readResponse(con);
                    Log.d(TAG, "buscarRanking: response=" + body);
                    JSONArray arr = new JSONArray(body);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        itens.add(new Player(
                                obj.getInt("id"),
                                obj.getString("nome"),
                                obj.getInt("capturas")
                        ));
                    }
                }
                con.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error in buscarRanking", e);
            }
            mainHandler.post(() -> callback.onResult(itens));
        });
    }

    private void writeBody(HttpURLConnection con, String payload) throws Exception {
        OutputStream os = con.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
        writer.write(payload);
        writer.flush();
        writer.close();
        os.close();
    }

    private String readResponse(HttpURLConnection con) throws Exception {
        InputStream input = new BufferedInputStream(con.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        input.close();
        return sb.toString();
    }
}
