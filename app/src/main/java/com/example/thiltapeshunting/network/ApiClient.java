package com.example.thiltapeshunting.network;

import android.os.Handler;
import android.os.Looper;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

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

    public void crearPlayer(String nome, PlayerCallback callback) {
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

                if (con.getResponseCode() >= 200 && con.getResponseCode() < 300) {
                    JSONObject json = new JSONObject(readResponse(con));
                    player = new Player(json.getInt("id"), json.optString("nome", nome));
                }
                con.disconnect();
            } catch (Exception ignored) {
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
                con.disconnect();
            } catch (Exception ignored) {
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
                con.disconnect();
            } catch (Exception ignored) {
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

                if (con.getResponseCode() >= 200 && con.getResponseCode() < 300) {
                    JSONArray arr = new JSONArray(readResponse(con));
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
            } catch (Exception ignored) {
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
                if (status >= 200 && status < 300) {
                    String body = readResponse(con);
                    JSONObject json = new JSONObject(body);
                    ok = json.optBoolean("capturado", false);
                }
                con.disconnect();
            } catch (Exception ignored) {
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

                if (con.getResponseCode() >= 200 && con.getResponseCode() < 300) {
                    JSONArray arr = new JSONArray(readResponse(con));
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
            } catch (Exception ignored) {
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

                if (con.getResponseCode() >= 200 && con.getResponseCode() < 300) {
                    JSONObject obj = new JSONObject(readResponse(con));
                    status = new GameStatus(
                            obj.getInt("playerId"),
                            obj.getInt("totalThiltapes"),
                            obj.getInt("totalCapturados"),
                            obj.getBoolean("finalizada")
                    );
                }
                con.disconnect();
            } catch (Exception ignored) {
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

                if (con.getResponseCode() >= 200 && con.getResponseCode() < 300) {
                    JSONArray arr = new JSONArray(readResponse(con));
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
            } catch (Exception ignored) {
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
                con.disconnect();
            } catch (Exception ignored) {
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

                if (con.getResponseCode() >= 200 && con.getResponseCode() < 300) {
                    JSONArray arr = new JSONArray(readResponse(con));
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
            } catch (Exception ignored) {
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
