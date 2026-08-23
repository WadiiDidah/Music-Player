package com.example.architecturedistribu;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    private ImageView StartRecording,playMusic,next;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String AudioSavaPath = null;
    private TextView nomSong;
    private boolean enreg = false;
    private boolean play = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nomSong=  findViewById(R.id.nom_music);
        StartRecording = findViewById(R.id.music_icon_big);
        playMusic = findViewById(R.id.pause_play);
        next = findViewById(R.id.next);

        StartRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!enreg){
                    Toast.makeText(MainActivity.this, "L'enregistrement a commencé", Toast.LENGTH_SHORT).show();
                    startRecord();

                    enreg = true;
                }
                else{
                    stopRecord();
                    Toast.makeText(MainActivity.this, "La requette a été envoyé", Toast.LENGTH_SHORT).show();
                    try {
                        sendAudio();
                        nomSong.setText("Mons - da");
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    enreg=false;
                }

            }

        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("je suis dans ****** next");
                // Créez une URL pour l'API de votre service Flask
            }

        });
        playMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!play){
                    Toast.makeText(MainActivity.this, "Le morceau va commencer", Toast.LENGTH_SHORT).show();
                    startPlaying();


                }
                else{
                    System.out.println("don't exist");
                    play=false;
                }

            }

        });
    }
    public void sendAudio() throws IOException, JSONException {
        File audioFile = new File(AudioSavaPath);
        byte[] audioBytes = new byte[(int) audioFile.length()];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(audioFile));
            buf.read(audioBytes, 0, audioBytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

// Envoi des données audio au service web Flask
        String urlString = "http://192.168.1.79:5000/upload-audio";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        OutputStream outputStream = conn.getOutputStream();
        outputStream.write(audioBytes);
        outputStream.flush();
        outputStream.close();

// Lecture de la réponse du service web Flask
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject responseJson = new JSONObject(String.valueOf(response));

// Extraire les données de la réponse
        try {
            String type_operation = responseJson.getString("type_opeation");
            String nom = responseJson.getString("nom");
            System.out.println("le nom est "+nom);
            System.out.println("le type est "+type_operation);
            // Utiliser les données comme vous le souhaitez
            // ...

        } catch (JSONException e) {
            e.printStackTrace();
        }



        System.out.println(response.toString());

    }

    public void startRecord(){
        System.out.println("record start !!!");
        if (checkPermissions() == true) {

            AudioSavaPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/" + "record.mp3";

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(AudioSavaPath);

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                Toast.makeText(MainActivity.this, "Recording started", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }
    }


       public void stopRecord(){
           mediaRecorder.stop();
           mediaRecorder.release();
           Toast.makeText(MainActivity.this, "Recording stopped", Toast.LENGTH_SHORT).show();

           // Vérifier si le fichier audio a été correctement enregistré
           File file = new File(AudioSavaPath);
           System.out.println(AudioSavaPath);
           if (file.exists()) {
               Toast.makeText(MainActivity.this, "File saved at: " + AudioSavaPath, Toast.LENGTH_SHORT).show();
           } else {
               Toast.makeText(MainActivity.this, "Error saving file", Toast.LENGTH_SHORT).show();
           }
       }
       /*




        StopPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer != null) {

                    mediaPlayer.stop();
                    mediaPlayer.release();
                    Toast.makeText(MainActivity.this, "Stopped playing", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }*/
    public void startPlaying(){
        AudioSavaPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                +"/"+"record.mp3";
        File audioFile = new File(AudioSavaPath);
        if (!audioFile.exists()) {
            Toast.makeText(MainActivity.this, "File not found", Toast.LENGTH_SHORT).show();
            return;
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(AudioSavaPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(MainActivity.this, "Start playing", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkPermissions() {
        int first = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);
        int second = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return first == PackageManager.PERMISSION_GRANTED &&
                second == PackageManager.PERMISSION_GRANTED;
    }


}