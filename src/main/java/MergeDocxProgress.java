package com.plutext.samples.mergedocx;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
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

	public static void main(String[] args) throws Exception {

		// Creation of message bus
		MBassador<Docx4jEvent> bus = new MBassador<Docx4jEvent>(
				new BusConfiguration()
			     .addFeature(Feature.SyncPubSub.Default()) // configure the synchronous message publication
			     .addFeature(Feature.AsynchronousHandlerInvocation.Default()) // configure asynchronous invocation of handlers
			     .addFeature(Feature.AsynchronousMessageDispatch.Default()) // configure asyncronous message publication (fire&forget)
			     );
		//  and registration of listeners
		ListeningBean listener = new ListeningBean();
		bus.subscribe(listener);		
		
		// Docx4J.setEventNotifier(bus);  // for simplicity, avoid getting these events
		
		String[] files = {"11v李笑笑.docx", "11v吴翔宇.docx" , "SolarSystem.docx"};
		
		List<BlockRange> blockRanges = new ArrayList<BlockRange>();
		for (int i=0 ; i< files.length; i++) {

			BlockRange block = new BlockRange(Docx4J.load(new File(files[i])));
			
//			BlockRange block = new BlockRange(WordprocessingMLPackage.load(
//					new File(DIR_IN + files[i])));
			
			// Give the block range a name, for convenience in progress reporting
			block.setName(files[i]);
			
			blockRanges.add( block );
			
			// No pages breaks
			block.setSectionBreakBefore(SectionBreakBefore.CONTINUOUS);
			
			// if you want no headers on the pages from docx2:
			block.setHeaderBehaviour(HfBehaviour.NONE);
		}
		
		// Perform the actual merge
		DocumentBuilder documentBuilder = new DocumentBuilder();
		Docx4jEvent.setEventNotifier(bus);
		
		WordprocessingMLPackage output = documentBuilder.buildOpenDocument(blockRanges);

		// Save the result
		Docx4J.save(output, new File(DIR_OUT+"MergeDocxProgress.docx"), Docx4J.FLAG_NONE);
		
	}	
	
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
