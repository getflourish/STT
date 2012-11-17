# SPEECH TO TEXT LIBRARY FOR PROCESSING

**Introduction**

I’ve been involved in a project about the German language that led to some research on speech recognition libraries. There are some good grammar-based libraries that can recognize given commands (Voce) but I’ve been looking for something that could be used for free dictation. Google Chrome already supports a HTML5 feature that enables speech recognition for input fields.

I was really surprised how good it works for several languages and decided to make it available as an open source library for Processing.

**The Library**

The library listens to the microphone input of your computer and sends recordings of your voice to Google for further processing. If the transcription was successful, the transcribe method is called and you can do whatever you want with the result. You can find some more details in the very early JavaDoc.


**Settings**

STT(PApplet, boolean) constructor takes the instance of PApplet (usually this) and an optional boolean value which is false by default. If you set it to true all recordings will be kept in the data folder.

**begin()** starts a record until end() is called
**disableAutoRecord()** disables automatic records
**disableAutoThreshold()** disables the analysis of the environmental volume after STT initialized
**enableDebug()** enables console output with relevant information about the transcription process.
**enableAutoRecord()** automatically records if the given volume threshold is reached
**enableAutoThreshold()** enables the analysis of the environmental volume after STT initialized
**end()** ends a record and starts transcription process
**setLanguage(String)** en, de, fr, etc. If the language is not supported it will automatically fall back to English.
**setThreshold(float)** sets the threshold that is used for speech recognition. If the input volume goes above the threshold it will be used for recognition.

**Example**

/*
// This is a basic example to demonstrate how the Speech-To-Text Library 
// can be used. See http://stt.getflourish.com for more information on
// available settings.
//
// Florian Schulz 2011, www.getflourish.com
*/

import com.getflourish.stt.*;

STT stt;
String result;

void setup ()
{
  size(600, 200);
  // Init STT with default manual record mode
  stt = new STT(this);
  stt.enableDebug();
  stt.setLanguage("en"); 
  
  // Some text to display the result
  textFont(createFont("Arial", 24));
  result = "Say something!";
}

void draw ()
{
  background(0);
  text(result, mouseX, mouseY);
}

// Method is called if transcription was successfull 
void transcribe (String utterance, float confidence) 
{
  println(utterance);
  result = utterance;
}

// Use any key to begin and end a record
public void keyPressed () {
  stt.begin();
}
public void keyReleased () {
  stt.end();
}

view rawexample.pdeThis Gist brought to you by GitHub.
Credits

The library is based on some thoughts by Mike Pultz who wrote an article that shows how to use the technology offered by Google without a browser. The library has the following dependencies: Minim, Gson and Java FLAC Encoder.

**Contact**

Email me or follow me on Twitter. I’m a designer and I’m aware of bugs, errors and bad ways of coding. Anyway, as long as it works for me, I’m happy to share what I’ve got. Feel free to make any changes to the code.

Florian Schulz, June 2011