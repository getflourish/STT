/**
 * @author Florian Schulz
 */
package com.getflourish.stt;

import java.io.File;
import ddf.minim.AudioRecorder;

import processing.core.PApplet;

public class LibTest extends PApplet {
	
	STT stt;
	String dataPath;
	
	public void setup () 
	{
		// Init STT automatically starts listening. Check getVolume() and use setThreshold() to fit your enviroment.
		stt = new STT(this);
		
		stt.setLanguage("en");
		stt.enableDebug();
		// stt.transcribeFile("bla.wav");
		// stt.enableAutoRecord();
	}
	
	public void draw() 
	{
		background(0);
	}
	
	public void transcribe (String utterance, float confidence) 
	{
		println(utterance);	
	}
	public void keyPressed () {
		stt.begin();
	}
	public void keyReleased () {
		stt.end();
	}
}

