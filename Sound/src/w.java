import com.voicerss.tts.AudioFormat;
import com.voicerss.tts.Languages;
import com.voicerss.tts.VoiceParameters;
import com.voicerss.tts.VoiceProvider;

import javax.sound.sampled.DataLine;

public class w {

    private static javax.sound.sampled.AudioFormat format;

    public static void main(String args[]) throws Exception {
        format = new javax.sound.sampled.AudioFormat(44100, 16, 1, true, false);
        com.voicerss.tts.VoiceProvider tts = new VoiceProvider("8fe517e0754842509460bff1acb9faa5");
        com.voicerss.tts.VoiceParameters params = new VoiceParameters("He boot too big for he goddamn feet.", Languages.English_GreatBritain);
        params.setCodec(com.voicerss.tts.AudioCodec.WAV);
        params.setFormat(AudioFormat.Format_44KHZ.AF_44khz_16bit_mono);
        params.setBase64(false);
        params.setSSML(false);
        params.setRate(0);

        byte[] voice = tts.speech(params);
        play(voice);
    }

    private static void play(byte[] voice) {
        javax.sound.sampled.SourceDataLine line = null;
        javax.sound.sampled.DataLine.Info info = new DataLine.Info(javax.sound.sampled.SourceDataLine.class, format);
        try {
            line = (javax.sound.sampled.SourceDataLine) javax.sound.sampled.AudioSystem.getLine(info);
            line.open(format);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        line.start();
        line.write(voice, 0, voice.length);
        line.drain();
        line.close();
    }

}
