
import net.engio.mbassy.listener.Handler;

import org.docx4j.Docx4J;
import org.docx4j.events.Docx4jEvent;
import org.docx4j.events.StartEvent;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import com.plutext.merge.BlockRange;
import com.plutext.merge.BlockRange.HfBehaviour;
import com.plutext.merge.BlockRange.SectionBreakBefore;
import com.plutext.merge.DocumentBuilder;



public class MergeDocxProgress {
	

	public final static String DIR_OUT = System.getProperty("user.dir")+ "/";


	static class ListeningBean {
		
		 // every message of type Docx4jEvent or MergeEvent  will be delivered
	    // to this handler; NPEs etc in this handler will be silently ignored.
	    @Handler
	    public void handleMessage(Docx4jEvent message) {
	    	
	    	String state = (message instanceof StartEvent) ? "starting" : "finished";
	    	
	    	if (message.getPkgIdentifier()==null) {

	    		System.out.println("\n\n\n\n **** MERGE " + state + " ***** \n\n");
	    		
	    	} else {

	    		System.out.println("\n\n\n\n **** " + message.getPkgIdentifier().name() + ": " + state + " ***** \n\n");
	    		
	    	}
	    		
	    }
		
	}
}
