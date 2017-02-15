package jbignums;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class DateCalcer {
    public static void doStuff() {
        System.out.println("Enter the date until which calcos will be done:");
        Scanner sc = new Scanner(System.in);
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date nd;
        try
        {
            nd = inputFormat.parse(sc.nextLine());
        }
        catch(ParseException e)
        {
            System.out.println("Error parsing date");
            return;
        }
        
        //System.out.println(inputFormat.format(nd));
        
        Calendar current = Calendar.getInstance();
        if(current.getTimeInMillis() > nd.getTime())
        {
            System.out.println("Time entered is lower than current!");
            return;
        }
        
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
        do
        {
            System.out.print(('\n' + new SimpleDateFormat("yyyy-MM").format(current.getTime())) + ": " );
            String cMonth = monthFormat.format(current.getTime());
            do
            {
                System.out.print(dayFormat.format(current.getTime()) + ' ');
                current.add(Calendar.DAY_OF_MONTH, 7);
            } while ( cMonth.equals(monthFormat.format(current.getTime())) 
                      && current.getTimeInMillis() <= nd.getTime() );
            
        } while(current.getTimeInMillis() <= nd.getTime());
    }
    
}
