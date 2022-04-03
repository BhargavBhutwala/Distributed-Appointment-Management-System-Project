package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import Appointments.HospInterface;
import Appointments.HospInterfaceHelper;

public class Client
{
    static BufferedReader br;
    static Logger logger;
    static FileHandler fh;
    static HospInterface obj;

    public static void main(String[] args)
    {
        br=new BufferedReader(new InputStreamReader(System.in));
        try
        {
            ORB orb = ORB.init(args, null);
            // -ORBInitialPort 1050 -ORBInitialHost localhost
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            while (true)
            {
                System.out.println("Enter your Id:");
                String id=br.readLine().trim();
                obj=null;
                setLogger("C:\\Users\\Bhargav\\OneDrive\\Desktop\\Comp6231_Project\\logs"+id+".txt",id);
                if(id.charAt(3)=='A')
                {
                    createAdminObject(id.substring(0, 3),ncRef);
                    adminOption(id);
                }
                else if(id.charAt(3)=='P')
                {
                    createAdminObject(id.substring(0, 3),ncRef);
                    patientOption(id);
                }
                if(id.equals("quit"))
                    break;
            }

        }
        catch (Exception e)
        {
            // TODO: handle exception
            System.out.println(e.getMessage());
        }
    }

    private static void patientOption(String id) throws IOException {
        // TODO Auto-generated method stub
        System.out.println("1. Book Appointment ");
        System.out.println("2. List appointment schedule");
        System.out.println("3. Cancel appointment");
        System.out.println("4. Swap appointment");
        System.out.println("Select Any above option:");
        String option = br.readLine().trim();
        if(option.equals("1"))
        {
            logger.info(id+" Book appointment operation");
            bookAppointment(id);
        }
        else if(option.equals("2"))
        {
            logger.info("List appointment operation");
            logger.info(obj.getBookingSchedule(id));
        }
        else if(option.equals("3"))
        {
            logger.info(id+" Cancel appointment operation");
            cancelAppointment(id);
        }
        else if(option.equals("4"))
        {
            logger.info(id+" Swap appointment operation ");
            swapAppointment(id);
        }

    }

    private static void swapAppointment(String id) throws IOException {
        // TODO Auto-generated method stub
        System.out.println("1. New Appointment ID ");
        String newAppointId = br.readLine().trim();
        System.out.println("2. New Appointment Type");
        String newAppointType = br.readLine().trim();
        System.out.println("3. Old Appointment ID ");
        String oldAppointId = br.readLine().trim();
        System.out.println("4. Old Appointment Type");
        String oldAppointType = br.readLine().trim();
        if ((newAppointId.charAt(3)=='M'||newAppointId.charAt(3)=='A'||newAppointId.charAt(3)=='E')&&(oldAppointId.charAt(3)=='M'||oldAppointId.charAt(3)=='A'||oldAppointId.charAt(3)=='E')){
            logger.info(obj.swapAppoint(id, newAppointId, newAppointType,oldAppointId,oldAppointType));
        }
        else {
            logger.info("Please Enter proper appointment Id");
        }
    }

    private static void adminOption(String id) throws IOException {
        // TODO Auto-generated method stub
        System.out.println("1. Add Appointment ");
        System.out.println("2. Remove Appointment");
        System.out.println("3. List all Available Appointment");
        System.out.println("4. Book Appointment ");
        System.out.println("5. List appointment schedule");
        System.out.println("6. Cancel appointment");
        System.out.println("7. Swap appointment");
        System.out.println("Select Any above option:");
        String option = br.readLine().trim();
        if(option.equals("1"))
        {
            logger.info(id+" Add appointment");
            addAppointment(id);
        }
        else if(option.equals("2"))
        {
            logger.info(id+" Remove appointment");
            removeAppointment(id);
        }
        else if(option.equals("3"))
        {
            logger.info(id+" List appointment availability");
            listAppointmentAvailability(id);
        }
        else if(option.equals("4"))
        {
            System.out.println("Enter patient id:");
            String patientId=br.readLine();
            logger.info(id+" Book appointment for "+patientId);
            bookAppointment(patientId);
        }
        else if(option.equals("5"))
        {
            System.out.println("Enter patient id:");
            String patientId=br.readLine();
            logger.info(id+" Get booking schedule");
            logger.info(obj.getBookingSchedule(patientId));
        }
        else if(option.equals("6"))
        {
            System.out.println("Enter patient id:");
            String patientId=br.readLine();
            logger.info(id+" Cancel appointment for "+patientId);
            cancelAppointment(patientId);
        }
        else if(option.equals("7"))
        {
            System.out.println("Enter patient id:");
            String patientId=br.readLine();
            logger.info(id+" Swap appointment for "+patientId);
            swapAppointment(patientId);
        }

    }

    private static void cancelAppointment(String id) throws IOException {
        // TODO Auto-generated method stub
        System.out.println("1. Appointment ID ");
        String appointId = br.readLine().trim();
        System.out.println("2. Appointment Type");
        String appointType = br.readLine().trim();
        if (appointId.charAt(3)=='M'||appointId.charAt(3)=='A'||appointId.charAt(3)=='E'){
            logger.info(obj.cancelAppoint(id, appointId, appointType));
        }
        else {
            logger.info("Please Enter proper appointment Id");
        }

    }

    private static void bookAppointment(String id) throws IOException {
        // TODO Auto-generated method stub
        System.out.println("1. Appointment ID ");
        String appointId = br.readLine().trim();
        System.out.println("2. Appointment Type");
        String appointType = br.readLine().trim();
        if (appointId.charAt(3)=='M'||appointId.charAt(3)=='A'||appointId.charAt(3)=='E'){
            logger.info(obj.bookAppoint(id, appointId, appointType));
        }
        else {
            logger.info("Please Enter proper appointment Id");
        }

    }

    private static void listAppointmentAvailability(String id) throws IOException {
        // TODO Auto-generated method stub
        System.out.println("1. Appointment Type");
        String appointType = br.readLine().trim();
        logger.info(obj.listAppointAvailability(id, appointType));

    }

    private static void removeAppointment(String id) throws IOException {
        // TODO Auto-generated method stub
        System.out.println("1. Appointment ID ");
        String appointId = br.readLine().trim();
        System.out.println("2. Appointment Type");
        String appointType = br.readLine().trim();
        if (appointId.charAt(3)=='M'||appointId.charAt(3)=='A'||appointId.charAt(3)=='E'){
            logger.info(obj.removeAppoint(id, appointId, appointType));
        }
        else {
            logger.info("Please Enter proper appointment Id");
        }

    }

    private static void addAppointment(String id) throws IOException {
        // TODO Auto-generated method stub
        System.out.println("1. Appointment ID ");
        String appointId = br.readLine().trim();
        System.out.println("2. Appointment Type");
        String appointType = br.readLine().trim();
        System.out.println("3. Booking Capacity");
        String capacity = br.readLine().trim();
        if ((Integer.parseInt(capacity.trim())>=0)&&(appointId.charAt(3)=='M'||appointId.charAt(3)=='A'||appointId.charAt(3)=='E')){
            logger.info(obj.addAppoint(id, appointId, appointType, capacity));
        }
        else {
            logger.info("Please Enter proper appointment Id or event capacity");
        }

    }

    private static void createAdminObject(String server, NamingContextExt ncRef) throws NotFound, CannotProceed, InvalidName {
        // TODO Auto-generated method stub
       /** if(server.startsWith("MTL"))
        {
            obj= HospInterfaceHelper.narrow(ncRef.resolve_str("MTL"));
        }
        else if(server.startsWith("QUE"))
        {
            obj= HospInterfaceHelper.narrow(ncRef.resolve_str("QUE"));
        }
        else if(server.startsWith("SHE"))
        {
            obj= HospInterfaceHelper.narrow(ncRef.resolve_str("SHE"));
        }**/
       obj=HospInterfaceHelper.narrow(ncRef.resolve_str("FrontEnd"));

    }

    private static void setLogger(String location, String id) {
        // TODO Auto-generated method stub
        try
        {
            logger = Logger.getLogger(id);
            fh = new FileHandler(location, true);
            SimpleFormatter sf = new SimpleFormatter();
            fh.setFormatter(sf);
            logger.addHandler(fh);
        }
        catch (Exception err)
        {
            logger.info("Couldn't Initiate Logger. Please check file permission");
        }

    }
}

