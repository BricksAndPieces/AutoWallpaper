import java.util.concurrent.TimeUnit;

/**
 * Simple class that just contains the main method
 * Used mostly for showing how the Wallpaper object works
 */
public class Runner {

    /**
     * Main method of the project
     * Simply sets up a Wallpaper object and turns on the looping feature
     *
     * @param args
     * The command line arguments passed when running this project
     */
    public static void main(final String[] args) {
        final String topic = getValue("topic", args);
        final String timeString = getValue("time", args);
        final String unitString = getValue("unit", args);

        if(topic == null || timeString == null || unitString == null) {
            System.err.println("Invalid args : 'java -jar AutoWallpaper.jar -time [NUMBER] -unit [TIMEUNIT] -topic [TOPIC]'");
            System.exit(1);
        }

        Long time = null;
        try {
            time = Long.valueOf(timeString);
            if(time < 1) {
                System.err.println("Invalid args : '-time' must be positive");
                System.exit(1);
            }
        }catch(final NumberFormatException e) {
            System.err.println("Invalid args : '-time' must provide an integer number");
            System.exit(1);
        }

        TimeUnit unit = null;
        switch(unitString) {
            case "m":
            case "min":
            case "minutes": {
                unit = TimeUnit.MINUTES;
                break;
            }
            case "hr":
            case "hrs":
            case "hours": {
                unit = TimeUnit.HOURS;
                break;
            }
            case "d":
            case "days": {
                unit = TimeUnit.DAYS;
                break;
            }
            default: {
                System.err.println("Invalid args : '-unit' must provide a valid time unit");
                System.exit(1);
            }
        }

        final Wallpaper wallpaper;
        try { wallpaper = new Wallpaper(topic, time, unit); }
        catch(final IllegalArgumentException e) {
            System.err.println("Illegal topic provided");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(wallpaper::stop));
        wallpaper.start();

        //TODO: put the try catch for the illegal topic
    }

    /**
     * Small helper method to help parse the command line arguments
     *
     * @param key
     * The key to get the String value for (not including the dash)
     *
     * @param args
     * The command line arguments to parse for the value
     *
     * @return
     * Returns the value associated with the key provided
     * Can return null if the key does not exist
     */
    private static String getValue(final String key, final String[] args) {
        final String s = String.join(" ", args);
        if(!s.contains("-"+key))
            return null;

        return s.substring(s.indexOf("-"+key)+key.length()+1).split("-")[0].trim();
    }
}