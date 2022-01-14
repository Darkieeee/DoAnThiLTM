package extension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class FormatDate {
    public static final String STANDARD_TIME = "HH:mm:ss";
    
    public static String getCurrent(String format) {
        LocalDateTime ld = LocalDateTime.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern(format);
        return f.format(ld);
    }
    
    public static void main(String... args)
    {
        System.out.println(FormatDate.getCurrent(FormatDate.STANDARD_TIME));
    }
}
