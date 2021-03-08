import tool.PPTXHelper;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Peter.Yang on 2021/2/20.
 */
public class JavaTest {
    public static void main(String[] args) {
        PPTXHelper ppt = new PPTXHelper();
        ppt.parseUnits2(new ArrayList<String>(Arrays.asList(new String[]{"街は光の中に","街は光の中に2"})),
                new ArrayList<String>(Arrays.asList(new String[]{"街は光の中に","街は光の中に2"})));
        ppt.export("2.pptx");

    }
}
