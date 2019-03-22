import Logging.ConsoleLogger;
import Sockets.WizardGoServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        try
        {
            //start server
            System.out.println("Starting server");
            WizardGoServer server = new WizardGoServer(7716, 7717, new ConsoleLogger());

            //wait for user command input
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                System.out.print("s:");
                String s = br.readLine();

                System.out.println(s);
            }
        }
        catch(IOException e){
            System.out.println(e.getStackTrace());
        }
    }
}
