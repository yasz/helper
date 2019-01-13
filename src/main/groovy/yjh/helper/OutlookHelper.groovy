
import com.independentsoft.msg.DisplayType;
import com.independentsoft.msg.Message;
import com.independentsoft.msg.MessageFlag;
import com.independentsoft.msg.ObjectType;
import com.independentsoft.msg.Recipient;
import com.independentsoft.msg.RecipientType;
import com.independentsoft.msg.StoreSupportMask;

class OutlookHelper {


    static void  main(String[] args)
    {

        try
        {
            Message message = new Message()
            def mailTo= { user,type->
                user.each {
                    Recipient recipient = new Recipient();
                    recipient.setAddressType("SMTP");
                    recipient.setDisplayType(DisplayType.MAIL_USER);
                    recipient.setObjectType(ObjectType.MAIL_USER);
                    recipient.setDisplayName(it);
                    recipient.setEmailAddress(it);
                    if(type == 'cc'){
                        recipient.setRecipientType(RecipientType.CC)
                    }else{
                        recipient.setRecipientType(RecipientType.TO)
                    }
                    message.getRecipients().add(recipient)
                }
            }
            message.setSubject("杨家昊-周报-20170707".getBytes("GBK"))
            message.setBodyRtf(File2byte("C:\\cv.rtf"));
            mailTo(["py186003@teradata.com"],"cc")
            message.getMessageFlags().add(MessageFlag.UNSENT);
            message.getStoreSupportMasks().add(StoreSupportMask.CREATE);
            message.save("c:\\message.msg", true);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public static byte[] File2byte(String filePath)
    {
        byte[] buffer = null;
        try
        {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return buffer;
    }
}