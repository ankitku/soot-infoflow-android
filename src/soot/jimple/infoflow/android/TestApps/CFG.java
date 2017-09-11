package soot.jimple.infoflow.android.TestApps;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParserException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointCreator;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

//dump the call graph from FlowDroid
public class CFG {
	
	private static String dumpCallGraph(CallGraph cg){
		Iterator<Edge> itr = cg.iterator();
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();

		while(itr.hasNext()){
			Edge e = itr.next();
			String srcSig = e.getSrc().toString();
			String destSig = e.getTgt().toString();
			Set<String> neighborSet;
			if(map.containsKey(srcSig)){
				neighborSet = map.get(srcSig);
			}else{
				neighborSet = new HashSet<String>();
			}
			neighborSet.add(destSig);
			map.put(srcSig, neighborSet );
			
		}
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String json = gson.toJson(map);
		return json;
	}
	
	public static void printCG(CallGraph cg) {
        Iterator<Edge> edgeItr = cg.iterator();
        List<String> edgeList = new ArrayList<>();

        while(edgeItr.hasNext()){
            Edge edge = edgeItr.next();

            SootMethod srcMethod = edge.getSrc().method();
            String srcMethodDeclaration = srcMethod.getDeclaringClass().toString() + "." +
                    srcMethod.getName() +
                    srcMethod.getParameterTypes().toString()
                            .replace('[', '(').replace(']', ')');

            SootMethod tgtMethod = edge.getTgt().method();
            String tgtMethodDeclaration = tgtMethod.getDeclaringClass().toString() + "." +
                    tgtMethod.getName() +
                    tgtMethod.getParameterTypes().toString()
                            .replace('[', '(').replace(']', ')');
            //System.out.println(tgtMethod.getActiveBody().toString());

            edgeList.add(srcMethodDeclaration + " => " + tgtMethodDeclaration);
            //System.out.println(srcMethodDeclaration + " => " + tgtMethodDeclaration);
        }
        //System.out.println(applicationCallGraph.size());
        for (String edgeStr : edgeList){
            System.out.println(edgeStr);
        }
    }

	public static void main(String[] args) throws XmlPullParserException {
		SetupApplication app = new SetupApplication(args[1],args[0]);
		final long beforeRun = System.nanoTime();
		   //app.calculateSourcesSinksEntrypoints("SourcesAndSinks.txt"); 

		app.constructCallgraph();
		
	CallGraph appCallGraph = Scene.v().getCallGraph();
	System.out.println("" + appCallGraph.size());
	
		System.out.println("Analysis has run for " + (System.nanoTime() - beforeRun) / 1E9 + " seconds");

		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_process_dir(Collections.singletonList(args[0]));
		Options.v().set_android_jars(args[1]);
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().setPhaseOption("cg.spark", "on");

		Scene.v().loadNecessaryClasses();

		SootMethod entryPoint = app.getDummyMainMethod();
		Options.v().set_main_class(entryPoint.getSignature());
		Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
		System.out.println(entryPoint.getActiveBody());
		
		List applicationClasses = new ArrayList<>();
		for (SootClass cls : Scene.v().getApplicationClasses()) {
		    applicationClasses.add(cls);
		}
      // Collections.sort(applicationClasses);
     

		PackManager.v().runPacks();
			PackManager.v().writeOutput();
			
		 appCallGraph = Scene.v().getCallGraph();
		System.out.println("" + appCallGraph.size());
		
		
		printCG(appCallGraph);
	}
}
