# SPEECH TO TEXT LIBRARY FOR PROCESSING

http://stt.getflourish.com

**Introduction**

Speech recognition library based on Google’s web service that enables speech input on HTML5 input fields in Google Chrome.

**The Library**

The library listens to the microphone input of your computer and sends recordings of your voice to Google for further processing. If the transcription was successful, the transcribe method is called and you can do whatever you want with the result.


**Settings**

STT(PApplet p, boolean history) constructor takes the instance of PApplet (usually this) and an optional boolean value which is false by default. If you set it to true all recordings will be kept in the data folder.

**begin()** starts a recording until end() is called

**disableAutoRecord()** disables automatic recordings

**disableAutoThreshold()** disables the analysis of the 
environmental volume after STT initialized

**enableDebug()** enables console output with relevant information about the transcription process.

**enableAutoRecord()** analyzed the environment sound level and automatically records if anything louder than the average level is recognized

**enableAutoRecord(float threshold)** automatically records if the given volume threshold is reached

**enableAutoThreshold()** enables the analysis of the environmental sound level after STT initialized

**end()** ends a recording and starts the transcription process

**setLanguage(String language)** en, de, fr, etc. If the language is not supported it will automatically fall back to English.

**setThreshold(float value)** sets the threshold that is used for speech recognition. If the input volume goes above the threshold it will be used for recognition.


**Credits**

The library is based on some thoughts by Mike Pultz who wrote an article that shows how to use the technology offered by Google without a browser. The library has the following dependencies: Minim, Gson and Java FLAC Encoder.

**Contact**

Email me or follow me on Twitter. I’m a designer and I’m aware of bugs, errors and bad ways of coding. Anyway, as long as it works for me, I’m happy to share what I’ve got. Feel free to make any changes to the code.

Florian Schulz, June 2011

www.florianschulz.info