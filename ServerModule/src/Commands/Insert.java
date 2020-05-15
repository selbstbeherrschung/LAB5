package Commands;


import Collection.Ticket;
import DataBase.ThreadResurses;
import WebRes.Command;
import WriteInOut.TicketReader;

import java.util.ArrayList;
import java.util.Scanner;


/** This class insert element by key*/
public class Insert extends AbstractCommand {

    public Insert(){
        name = "insert";
    }

    public Insert(ThreadResurses threadResurses){
        name = "insert";
        tr = threadResurses;
    }

    @Override
    public void execute(String string, Scanner scan, ExeClass eCla) {
        String key = string;
        if(key.isEmpty()){
            System.out.println("Need a key");
            return;
        }
        Ticket tick = eCla.getTicket();
        tick.setKey(key);
        try{
            if(tick == null){
                System.out.println("Bad ticket");
            }else {
                tr.putT(key,tick);
            }
        }catch (NullPointerException e){
            System.out.println("Wrong ticket");
        }

    }

    @Override
    public void exe() {
        tr.putT(com.getFirstArgument(),(Ticket) com.getThirdArgument());
        send(null);
    }

    @Override
    public void send(ArrayList<Command> commands) {
        com.setFirstArgument("Ticket inserted with key: " + com.getFirstArgument());
        tr.sender.send(com);
    }

    @Override
    protected void setArgs(String str, Scanner scanner) {
        com.setFirstArgument(str);
        Ticket t = new TicketReader(scanner,true).readTicket();
        t.setKey(str);
        com.setThirdArgument(t);
    }

}
