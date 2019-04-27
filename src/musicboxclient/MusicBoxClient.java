package musicboxclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class MusicBoxClient {
  private static final String ADDRESS = "localhost";
  private static final int PORT = 40000;
  private static final int CHANNEL = 0;
  private static final HashMap<String, Integer> VOICE_TO_CODE = new HashMap<>();
  
  public static void main(String[] args) {
    setTransformationTable();
    String msg = createMsg(args);
    
    try (
      Socket s = new Socket(ADDRESS, PORT);
      Scanner sc = new Scanner(s.getInputStream());
      PrintWriter pw = new PrintWriter(s.getOutputStream());
      Synthesizer syn = MidiSystem.getSynthesizer();
    ) {
      syn.open();
      MidiChannel mc = syn.getChannels()[CHANNEL];
      
      playMusic(sc,pw,msg,mc);
      
      syn.close();
      s.close();

    } catch (MidiUnavailableException | IOException ex) {
      Logger.getLogger(MusicBoxClient.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static void playMusic(Scanner sc, PrintWriter pw, String msg, MidiChannel mc) {
    pw.println(msg);
    pw.flush();
    
    String[] tmp = sc.nextLine().split(" ");
    if("playing".equals(tmp[0])) {
      String[] s;
      int note = 0;
      boolean end = false;

      while(!end) {
        if(sc.hasNextLine()) {
          mc.noteOff(note);
          s = sc.nextLine().split(" ");
          switch(s[0]) {
            case "FIN":
              end = true;
              break;
            case "R":
              mc.noteOff(note);
              break;
            default:
              note = transform(s[0]);
              mc.noteOn(note, 50);
              System.out.print(s[1] + " ");
              break;
          }
        }
      }
    }
  }
  
  private static String createMsg(String[] args) {
    StringBuilder sb = new StringBuilder("play");
    for(String s : args) {
      sb.append(" ").append(s);
    }
    return sb.toString();
  }
  
  private static void setTransformationTable() {
    try(
      Scanner sc = new Scanner(new File("voiceToCode.txt"));
    ) {
      while(sc.hasNextLine()) {
        String[] line = sc.nextLine().split(";");
        VOICE_TO_CODE.put(line[0], Integer.parseInt(line[1]));
      }
    } catch (FileNotFoundException e) {System.err.println("File not found");}
  }

  private static int transform(String voice) {
    String[] v = voice.split("/");
    if(v.length > 1) {
      return VOICE_TO_CODE.get(v[0]) + 12*Integer.parseInt(v[1]);
    }
    return VOICE_TO_CODE.get(v[0]);
  }
}