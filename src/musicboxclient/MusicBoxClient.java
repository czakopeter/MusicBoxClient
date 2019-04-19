package musicboxclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
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
    String msg = createMsg(args);
    int number;
    
    try (
      Socket s = new Socket(ADDRESS, PORT);
      Scanner sc = new Scanner(s.getInputStream());
      PrintWriter pw = new PrintWriter(s.getOutputStream());
      
      Scanner in = new Scanner(System.in);
      
      Synthesizer syn = MidiSystem.getSynthesizer();
    ) {
      syn.open();
      MidiChannel mc = syn.getChannels()[CHANNEL];
      
      pw.println(msg);
      pw.flush();
      
      while(true) {
        if(sc.hasNextLine()) {
          number = Integer.parseInt(sc.nextLine());
          break;
        }
      }
      
      System.out.println(number);

      Thread t = new Thread(() -> {
        String line;
        int note;
        while(true) {
          if(sc.hasNextLine()) {
            line = sc.nextLine();
            if("FIN".equals(line)) {
              System.out.println(line);
              break;}
            String[] d = line.split(" ");
            note = Integer.parseInt(d[0]);
            if(note != -1) {
              mc.noteOn(note, 50);
              System.out.println(d[1]);
            } else {
              mc.noteOff(note);
            }
          }
        }
        syn.close();
        System.out.print("End of playing. Please press Enter to close client.");
      });
      t.start();
//      t.join();

//      in.hasNextLine();
      
      while(true && t.isAlive()) {
        try {
          String line = in.nextLine();
          pw.println(line);
          pw.flush();
        }catch (IllegalStateException e) {}
      }
      System.out.println("CLIENT END");
      s.close();
    } catch (MidiUnavailableException | IOException ex) {
      Logger.getLogger(MusicBoxClient.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static String createMsg(String[] args) {
    StringBuffer sb = new StringBuffer("play ");
    for(String s : args) {
      sb.append(s).append(" ");
    }
    return sb.toString();
  }
}
