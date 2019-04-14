package musicboxclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
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
  private static final int CHANNEL = 10;
  
  public static void main(String[] args) {
    try (
      Socket s = new Socket(ADDRESS, PORT);
      Scanner sc = new Scanner(s.getInputStream());
      PrintWriter pw = new PrintWriter(s.getOutputStream());
      
      Synthesizer syn = MidiSystem.getSynthesizer();
    ) {
      syn.open();
      MidiChannel mc = syn.getChannels()[CHANNEL];
      
      System.out.println("CLIENT START");
      
      String command = "play";
      String title = "Fairy";
      int tempo = 125;
      int trans = 0;
      
      String msg = command +" "+ title +" "+ tempo +" "+ trans;
      //String msg = "play Boci 500 0";
      pw.println(msg);
      pw.flush();
      
      System.out.println("CLIENT SEND PLAY: " + msg);
      
      String line;
      while(true) {
        System.out.println("HERE");
        if(sc.hasNextLine()) {
          line = sc.nextLine();
          if("FIN".equals(line)) {break;}
          String[] d = line.split(" ");
          if(!"-1".equals(d[0])) {
            mc.noteOn(Integer.parseInt(d[0]), 50);
          }
          Thread.sleep(tempo);
        }
      }
      System.out.println("CLIENT END");
      s.close();
    } catch (MidiUnavailableException | IOException | InterruptedException ex) {
      Logger.getLogger(MusicBoxClient.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
