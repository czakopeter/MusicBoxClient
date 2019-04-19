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
    boolean end = false;
    setTransformationTable();
    String msg = createMsg(args);
    int playingNr = -1;
    
    
    try (
      Socket s = new Socket(ADDRESS, PORT);
      Scanner sc = new Scanner(s.getInputStream());
      PrintWriter pw = new PrintWriter(s.getOutputStream());
      Synthesizer syn = MidiSystem.getSynthesizer();
    ) {
      syn.open();
      MidiChannel mc = syn.getChannels()[CHANNEL];
      
      while(!end) {
        if(msg == null) {
          System.out.println("Press enter to close client");
          msg = sc.nextLine();
        }
        switch(msg.split(" ")[0]) {
          case "play":
            playMusic(sc,pw,playingNr,mc);
            break;
            case "":
            break;
          default:
            pw.println(msg);
            pw.flush();
        }
      }
      
      syn.close();
      
      System.out.println("CLIENT CLOSE");
      s.close();
    } catch (MidiUnavailableException | IOException ex) {
      Logger.getLogger(MusicBoxClient.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static void playMusic(Scanner sc, PrintWriter pw, int playingNr, MidiChannel mc) {
    playingNr = Integer.parseInt(sc.nextLine());
    System.out.println("Number of playing: " + playingNr);
    
    String line;
    int note = 0;
    boolean end = false;
    
    while(!end) {
      if(sc.hasNextLine()) {
        line = sc.nextLine();
        switch(line) {
          case "FIN":
            end = true;
            break;
          case "R":
            mc.noteOff(note);
            break;
          default:
            String[] v = line.split(" ");
            note = transform(v[0]);
            mc.noteOn(note, 50);
            System.out.print(v[1]);
            break;
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
