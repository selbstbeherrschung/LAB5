package UI.Commandes;

import GUI.CommandFormer;
import UI.AbstractCommand;
import WebRes.Command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import static Starter.ClientMain.receiver;

public class PrintFieldDescending extends AbstractCommand {
    @Override
    public void check(String command, String arg) {
        int num = getField(arg);
        this.command.setSecondArgument(num);
        super.check(command,arg);
    }

    public static int getField(String arg){
        int num = -1;
        switch (arg) {
            case "id":
                num = (0);
                break;
            case "name":
                num = (1);
                break;
            case "coordinates.x":
                num = (2);
                break;
            case "coordinates.y":
                num = (3);
                break;
            case "creation date":
                num = (4);
                break;
            case "prise":
                num = (5);
                break;
            case "comment":
                num = (6);
                break;
            case "refundable":
                num = (7);
                break;
            case "type":
                num = (8);
                break;
            case "venue.id":
                num = (9);
                break;
            case "venue.name":
                num = (10);
                break;
            case "venue.capacity":
                num = (11);
                break;
            case "venue.type":
                num = (12);
                break;
            case "venue.address.zipCode":
                num = (13);
                break;
            case "venue.address.town.x":
                num = (14);
                break;
            case "venue.address.town.y":
                num = (15);
                break;
            case "venue.address.town.z":
                num = (16);
                break;
            case "venue.address.town.name":
                num = (17);
                break;
            default:
                System.out.println("There is not field with this name!");
                System.out.println(getHelp());
                System.out.print("Enter field\n>");
                Scanner scan = new Scanner(System.in);
                return getField(scan.nextLine());
        }
        return num;
    }

    public static String getHelp() {
        String str = "id" + "\n" +
                "name" + "\n" +
                "coordinates.x" + "\n" +
                "coordinates.y" + "\n" +
                "creation date" + "\n" +
                "prise" + "\n" +
                "comment" + "\n" +
                "refundable" + "\n" +
                "type" + "\n" +
                "venue.id" + "\n" +
                "venue.name" + "\n" +
                "venue.capacity" + "\n" +
                "venue.type" + "\n" +
                "venue.address.zipCode" + "\n" +
                "venue.address.town.x" + "\n" +
                "venue.address.town.y" + "\n" +
                "venue.address.town.z" + "\n" +
                "venue.address.town.name" + "\n" +
                "Enter number\n>";
        return str;
    }

    ArrayList<Command> ac = new ArrayList<>();

    @Override
    public boolean receive() {
        try {
            Command com = receiver.receive();
            if(com.getFirstArgument() != null){
                CommandFormer.answer = com.getFirstArgument();
            }
            int cs = (int) com.getSecondArgument();
            ac = new ArrayList<>();
            for (int i = 0; i < cs; i++) {
                try {
                    ac.add(receiver.receive());
                }catch (IOException e) {
                    CommandFormer.setServerStatus(0);
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            CommandFormer.setServerStatus(0);
            return false;
        }
    }

    @Override
    public Object getResult() {
        return ac;
    }
}
