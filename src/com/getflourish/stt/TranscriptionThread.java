package com.getflourish.stt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;

/**
 * Each transcription is handled in a separate thread to ensure that the program remains responsive while the thread is waiting for 
 * a response from the server.
 * @author Florian Schulz
 */

public class TranscriptionThread extends Thread {
	boolean running;

	private float confidence;
	public boolean debug = false;
	private int status;
	boolean available = false;
	private String utterance;

	private String record;
	private String lang;

	public TranscriptionThread(String lang) {
		this.lang = lang;
		running = false;
	}

	public void startTranscription(String _record) {
		this.record = _record;
		this.running = true;
	}

	public void run() {
		while (true) {
			if (running) {
				transcribe(this.record);
				running = false;
			} else {
				try {
					sleep(500);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * 
	 * @param _path location of the audio file encoded as FLAC
	 * @return transcription of the audio file as String
	 */
	public String transcribe(String _path) {
		this.available = false;
		// Gets the file for the specified path
		String path = _path;
		File file = new File(path);

		// I nearly died here. Sends the recording to Google via Post (thanks to
		// ClientHttpRequest Class)
		String response = "";
		ClientHttpRequest r;
		try {
			r = new ClientHttpRequest("https://www.google.com/speech-api/v1/recognize?xjerr=1&client=chromium&lang=" + lang);
			r.setParameter("file", file);
			InputStream stream = r.post();
			response = convertStreamToString(stream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String s = "";
		// Parse JSON
		Gson gson = new Gson();
		Response transcription = gson.fromJson(response, Response.class);
		if (transcription != null) {
			if (transcription.status == 0) {
				// returns the transcription
				this.confidence = transcription.hypotheses[0].confidence;
				this.status = transcription.status;
				this.utterance = transcription.hypotheses[0].utterance;
				this.available = true;
			} else {
				// no result, could not be transcribed
				this.confidence = 0;
				this.status = transcription.status;
				this.utterance = "";
				this.available = true;
			}
			if (debug) {
				switch (this.status) {
				case 0:
					s = "Recognized: " + this.utterance + " (confidence: " + this.confidence + ")";
					this.available = true;
					status = STT.SUCCESS;
					break;
				case 3:
					s = "We lost some words on the way.";
					status = STT.ERROR;
					break;
				case 5:
					s = "Speech could not be interpreted.";
					status = STT.ERROR;
					break;
				default:
					s = "Did you say something?";
					status = STT.ERROR;
					break;
				}
			} else {
				if (this.status == 0) status = STT.SUCCESS;
				else status = STT.ERROR;
			}
		} else {
			s = "Speech could not be interpreted! Try to shorten the recording.";
			status = STT.ERROR;
		}
		if(debug) {
			System.out.println(getTime() + " " + s);	
		}
		return this.utterance;
	}

	private String convertStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
	
	public float getConfidence () 
	{
		return this.confidence;
	}
	public String getUtterance ()
	{
		return this.utterance;
	}
	public int getStatus () {
		return this.status;
	}
	/**
	 * @return true if audio processing was successfull
	 */
	public boolean isAvailable()
	{
		return this.available;
	}
	private String getTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
	}
}
