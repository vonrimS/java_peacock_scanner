package von.rims.constants;

import java.util.regex.Pattern;

public class AppConstants {
    public static final String FILE_URL = "https://github.com/PeacockTeam/new-job/releases/download/v1.0/lng-4.txt.gz";
    public static final String ENCODING = "UTF-8";
    public static final Pattern VALID_LINE_PATTERN = Pattern.compile("^(\"[^\"]*(\"{2}[^\"]*)*\";)*\"[^\"]*(\"{2}[^\"]*)*\"$");
}
