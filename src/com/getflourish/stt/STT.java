package com.getflourish.stt;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import processing.core.PApplet;

import ddf.minim.*;

import javaFlacEncoder.*;


/**
 * Converts speech to text using the x-webkit-speech technology found in Chrome.
 * @author Florian Schulz
 * 
 */
public class STT  {
	
	public final static int ERROR = 2;
	public final static int RECORDING = 0;
	public final static int SUCCESS = 1;
	public final static int TRANSCRIBING = 3;
	private boolean active = false;
	boolean analyzing;
	private boolean auto = false;
	
	private boolean autoThreshold = true;
	// path of recorded files
	String dataPath = "";
	private boolean debug = false;
	FLAC_FileEncoder encoder;
	
	int fileCount = 0;
	
	String fileName = "";
	boolean fired;
	AudioInput in;
	
	// timer interval
	int interval = 500;
	private String language = "en";
	String lastStatus = "";
	boolean log = false;
	private Minim minim;
	PApplet p;
	
	String path = "";
	
	AudioRecorder recorder;
	
	boolean recording = false;
	String recordsPath = "";
	String result = "";
	String status = "";
	private ArrayList<TranscriptionThread> threads;
	// volume threshold can be adjusted on runtime
	float threshold = 5f;
	
	Timer timer;

	Timer timer2;
	
	Method transcriptionEvent;
	
	Method transcriptionEvent2;
	private TranscriptionThread transcriptionThread;
	float volume;
	ArrayList<Float> volumes;
	
	/**
	 * @param _p instance of PApplet
	 */
	public STT (PApplet _p) {
		this(_p, false);
	}
	/**
	 * @param _p instance of PApplet
	 * @param history indicates whether or not recordings are stored in the data folder	
	 */
	public STT (PApplet _p, boolean history) {
		this.p = _p;
		this.log = history;  
		this.threads = new ArrayList<TranscriptionThread>();
		this.minim = new Minim(p);
		this.encoder = new FLAC_FileEncoder();		
		// get a LineIn from Minim, default bit depth is 16
		in = minim.getLineIn(Minim.MONO);
		this.recorder = minim.createRecorder(in, path + fileName + fileCount + ".wav", true);
        disableAutoRecord(); 
		this.listen();
	}
	TranscriptionThread addTranscriptionThread() {
	    transcriptionThread = new TranscriptionThread(language);
		transcriptionThread.debug = debug;
		transcriptionThread.start();
        threads.add(transcriptionThread);
		return transcriptionThread;
	}
	private void killTranscriptionThread (int i) {
		threads.get(i).interrupt();
		threads.remove(i);
	}
	private void analyzeEnv() {
		if (!analyzing) {
			timer2 = new Timer(2000);
			timer2.start();
			analyzing = true;
			volumes = new ArrayList<Float>();
		}
    	if (timer2 != null) {
    		if (!timer2.isFinished()) {
    			float volume = in.mix.level() * 1000;
    			volumes.add(volume);
    		} else {
    			float avg = 0.0f;
    			float max = 0.0f;
    			for (int i = 0; i < volumes.size(); i++) {
    				avg += volumes.get(i);
    				if (volumes.get(i) > max) max = volumes.get(i);
    			}
    			avg /= volumes.size();
    			threshold = (float) Math.ceil(max);
    			System.out.println(getTime() + " Volume threshold automatically set to " + threshold);
    			analyzing = false;
    		}	
    	}	
	}
	/**
	 * Starts a record
	 */
	public void begin () {
		if (!active) {
			onBegin();
			active = true;
			auto = false;
		}
	}
	public void disableAutoRecord () {
		auto = false;
		disableAutoThreshold();
		status = "STT info: Manual mode enabled. Use begin() / end() to manage recording.";
	}
	/**
	 * Disables the analysis of the environmental volume after initialization.
	 */
	public void disableAutoThreshold() {
		this.autoThreshold = false;
		analyzing = false;
	}
	public void disableDebug() {
		this.debug = false;
	}
	/**
	 * Records will be deleted
	 */
	public void disableHistory() {
		this.log = false;
	}
	private void dispatchTranscriptionEvent(String utterance, float confidence, int s) {
	    if (transcriptionEvent2 != null && !status.equals(lastStatus)) {
			try {
				transcriptionEvent2.invoke(p, new Object[] {utterance, confidence, s});
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {

			}
		}
	}
	public void dispose() {
	}
	public void draw() {	
		if (auto) handleAuto();
		// handles active threads and callbacks
		for (int i = 0; i < threads.size(); i++) {
			transcriptionThread = threads.get(i); 
			transcriptionThread.debug = debug;
			if (transcriptionThread.isAvailable()) {
				if (transcriptionEvent != null) {
					try {
						transcriptionEvent.invoke(p, new Object[] { transcriptionThread.getUtterance(), transcriptionThread.getConfidence()});
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {

					}
				} else if (transcriptionEvent2 != null) {
                    dispatchTranscriptionEvent(transcriptionThread.getUtterance(), transcriptionThread.getConfidence(), transcriptionThread.getStatus());
				}
				killTranscriptionThread(i);
				
			}

			if (debug && !status.equals(lastStatus)) {
				System.out.println(getTime() + " " + status);
				lastStatus = status;
			}
		}
	}
	
	/**
	 * Enables the automatic recording if the set voulme threshold has been reached  
 	*/
	public void enableAutoRecord () {
		auto = true;
		enableAutoThreshold();
		status = "STT info: Automatic mode enabled. Anything louder than threshold will be recorded.";
	}
	/**
	 * Enables the automatic recording if the given voulme threshold has been reached
	 * @param threshold the threshold that can be checked with getVolume() 
 	*/
	public void enableAutoRecord (float threshold) {
		auto = true;
		this.autoThreshold = true;
		this.threshold = threshold;
		status = "STT info: Automatic mode enabled. Anything louder than " + threshold + " will be recorded.";
	}
	/**
	 * Enables the analysis of the environmental volume after initialization.
	 */
	public void enableAutoThreshold() {
		this.autoThreshold = true;
		analyzing = false;
		analyzeEnv();
	}
	/**
	 * Enables logging of events like recording, transcribing, success, error.
	 */
	public void enableDebug() {
		this.debug = true;
		for (int i = 0; i < threads.size(); i++) {
			threads.get(i).debug = this.debug;
		}
	}
	/**
	 * Records will be kept in the data folder
	 */
	public void enableHistory() {
		this.log = true;
	}
	/**
	 * Ends a record
	 */
	public void end () {
		if (active) {
			onSpeechFinish();
			active = false;
			auto = false;
		}
	}
	private String getDateTime() {
	        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	        Date date = new Date();
	        return dateFormat.format(date);
	}
	public String getLanguage() {
		return language;
	}
	/**
	 * Returns the Minim instance for access in programs that need to use Minim beside STT
	 */
	public Minim getMinimInstance() {
		return minim;
	}
	public float getThreshold() {
		return threshold;
	}
	private String getTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
	}
	public float getVolume() {
        updateVolume();
		return volume;
	}
	private void handleAuto () {
	    if (analyzing) analyzeEnv();
	    updateVolume();	
		if (volume > threshold) {
            // start recording when someone says something louder than threshold
			onSpeech();
		} else {
		    // the magic begins. save it. transcribe it.
    		if (timer.isFinished() && volume < threshold && recorder.isRecording() && recording) {
    			onSpeechFinish();
    		} else if (timer.isFinished() && volume < threshold) {
    			startListening();
    		}
		}
	}
	private void initFileSystem ()
	{
		dataPath = p.dataPath("") + "/";
		recordsPath = getDateTime() + "/";
		if (log) {
			path = dataPath + recordsPath;
		} else {
			path = dataPath;
		}
				
		try {
			// create datafolder if it does not exist yet
			File datadir = new File(dataPath + "/");
			datadir.mkdir();
			
			if (log) {
				File recordsdir = new File(path);
				recordsdir.mkdir();
			}

		} catch (NullPointerException e) {
			System.err.println("Could not read files in directory: " + path);
		}
		timer = new Timer(interval);
		
		// calls draw every frame
		this.p.registerDraw(this);
		this.p.registerDispose(this);
	}
	
	private void listen() {
        addTranscriptionThread();
		initFileSystem();
		
		// listening repeats until something is heard
		timer.start();
		
		
		// setting up reflection method that is called in PApplet
		try {
			transcriptionEvent = p.getClass().getMethod("transcribe", 
					String.class, float.class);
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		}
		
		// setting up reflection method that is called in PApplet
		try {
			transcriptionEvent2 = p.getClass().getMethod("transcribe", 
					String.class, float.class, int.class);
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		}
		
		if (transcriptionEvent == null && transcriptionEvent2 == null) System.err.println("STT info: use transcribe(String word, float confidence, [int status]) in your main sketch to receive transcription events");
	
	}
	private void onBegin () 
	{
		status = "Recording";
        startListening();
	}
	private void onSpeech()
	{
		// resets the timer each time something is heard
		status = "Recording";
		timer.start();
		recording = true;
        dispatchTranscriptionEvent(transcriptionThread.getUtterance(), transcriptionThread.getConfidence(), STT.RECORDING);
	}
	public void onSpeechFinish()
	{
		status = "Transcribing";
		fired = false;
		recorder.endRecord();
		recorder.save();
		recording = false;
		
        dispatchTranscriptionEvent("", 0, STT.TRANSCRIBING);
		
		// Encode the wav to flac
		String flac = path + fileName + fileCount + ".flac";
		encoder.encode(new File(path + fileName + fileCount + ".wav"), new File(flac));
		boolean exists = (new File(flac)).exists();
		while(exists == false)
		{	
			exists = (new File(flac)).exists();		
		}
	
		if (exists) {
			 this.transcribe(flac);
		} else {
		    System.err.println("Could not transcribe. File was not encoded in time.");
		}
		
		// new file for new speech
		if (log) fileCount++;
	}

	/**
	 * @param language en, de, fr, etc. If the language is not supported it will automatically fall back to English.
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	
	/**
	 * Sets the volume threshold that is used to recognize speech and to filter background noise.
	 */
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}
	private void startListening () 
	{
		recorder.endRecord();
		recorder.save();
		// recorder = null;
		recorder = minim.createRecorder(in, path + fileName + fileCount + ".wav", true);
		recorder.beginRecord();
		timer.start();
	}
	private void stop() {
		// always close Minim audio classes when you are done with them
		in.close();
		minim.stop();
		p.stop();
	}
	public void transcribe(String _path) {
        addTranscriptionThread().startTranscription(_path);
	}
	public void transcribeFile (String _path) {
		status = "Transcribing";
		
		_path = path + "/" + _path;
		// Encode the wav to flac
		String flac = _path.substring(0, _path.length() - 4) + ".flac";
		encoder.encode(new File(_path), new File(flac));
		boolean exists = (new File(flac)).exists();
		while(exists == false)
		{	
			exists = (new File(flac)).exists();		
		}
	
		if (exists) {
			 this.transcribe(flac);
		} else {
		    System.err.println("Could not transcribe. File was not encoded in time.");
		}
		
		// new file for new speech
		if (log) fileCount++;
		
	}
	private void updateVolume() {
	    volume = in.mix.level() * 1000;
	}
	
}
