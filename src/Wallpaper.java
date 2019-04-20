import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a single Wallpaper object
 * Important to note: This project will only function on macs or other Apple devices
 */
public class Wallpaper {

    private final long time;
    private final TimeUnit unit;
    private final String imgUrl;
    private final File imgFile;
    private final ProcessBuilder process;
    private final ScheduledExecutorService service;

    /**
     * Constructor for the Wallpaper object
     * This defines a single Wallpaper object and allows use of its wallpaper capabilities
     *
     * @param topic
     * The topic or search term all the images should follow
     * This will usually a one word topic such as "nature" or "space"
     *
     * @param time
     * The amount of time between which to change the wallpaper
     *
     * @param unit
     * The TimeUnit to use for the time between each change of wallpaper
     *
     * @throws IllegalArgumentException
     * Throws if an invalid topic or search term is provided
     */
    public Wallpaper(final String topic, final long time, final TimeUnit unit) {
        this.time = time;
        this.unit = unit;
        this.service = Executors.newSingleThreadScheduledExecutor();
        this.imgFile = new File("AutoWallpaper.png");

        this.process = new ProcessBuilder(
                 "osascript", "-e",
                 "tell application \"Finder\" to set desktop picture to POSIX file \""
                 + this.imgFile.getAbsolutePath() + "\"").inheritIO();

        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.imgUrl = String.format("https://source.unsplash.com/random/%dx%d?%s", dim.width, dim.height, topic);

        try { getRandomImage(); }
        catch(final IOException e) {
            throw new IllegalArgumentException("Invalid topic - " + e.getMessage());
        }
    }

    /**
     * Begins the Wallpaper auto changer
     * This will immediately set a new wallpaper and then switch it every once in a while
     * depending on time and unit given as parameters in the constructor
     *
     * @throws IllegalStateException
     * Throws if the ScheduledExecutorService has already been shutdown
     */
    public void start() {
        if(this.service.isShutdown())
            throw new IllegalStateException("Wallpaper cannot be restarted once stopped!");

        this.service.scheduleAtFixedRate(this::setWallpaper, 0, this.time, this.unit);
    }

    /**
     * Shuts down the auto changer for the wallpapers
     * Once the Wallpaper#stop() has been called, Wallpaper#start() will throw
     * an IllegalStateException
     */
    public void stop() {
        if(!this.service.isShutdown())
            this.service.shutdown();
    }

    /**
     * Private helper method for the Wallpaper class
     * Sets the wallpaper of your current device to a random image that is based of
     * the topic specified in the constructor
     */
    private void setWallpaper() {
        try {
            ImageIO.write(getRandomImage(), "png", this.imgFile);
            final int errorCode =  this.process.start().waitFor(); //TODO: debug this line

            if(errorCode != 0)
                System.err.println("Error - AutoWallpaper stopped from setting wallpaper with code: " + errorCode);

        }catch(final IOException | InterruptedException e) { e.printStackTrace(); }
    }

    /**
     * Gets a random image based on the topic specified in the constructor
     *
     * @return
     * BufferedImage representation of a random image
     *
     * @throws IOException
     * Throws if unable to get random image based on topic
     * This is mostly just to carry the Exception thrown from Wallpaper#getImageUrl() onwards
     */
    private BufferedImage getRandomImage() throws IOException {
        return ImageIO.read(new URL(this.getImageURL(this.imgUrl)));
    }

    /**
     * Recursive method to navigate through URL redirects to get the final image URL
     *
     * @param url
     * A String representation of the original URL to be followed
     * Most likely will be the same URL every time because the redirect is random
     *
     * @return
     * Returns a String representation of the final image URL
     *
     * @throws IOException
     * Throws in cases where it is not able to establish a connection or follow redirects
     */
    private String getImageURL(String url) throws IOException {
        final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        con.getInputStream();

        final int code = con.getResponseCode();
        if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP)
            return this.getImageURL(con.getHeaderField("Location"));

        System.out.println(url);
        return url;
    }
}