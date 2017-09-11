package soot.jimple.infoflow.android.TestApps;

import java.util.Collections;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.options.Options;

public class FindAsyncs {
	
	public static void main(String...args)
	{
			soot.G.reset();
			
			Options.v().set_allow_phantom_refs(true);
			//Options.v().set_whole_program(true);
			Options.v().set_prepend_classpath(true);
			Options.v().set_validate(true);
			//Options.v().set_output_format(Options.output_format_dex);
			Options.v().set_process_dir(Collections.singletonList(args[0]));
			Options.v().set_force_android_jar(args[1]);
			Options.v().set_src_prec(Options.src_prec_apk);
			
			Options.v().set_soot_classpath(args[1]);
			
			Scene.v().loadNecessaryClasses();
			
			PackManager.v().getPack("jtp").add(new Transform("jtp.myAnalysis", new MyBodyTransformer()));
			//  PackManager.v().getPack("wjtp").add(
			//	      new Transform("wjtp.myTransform", new SceneTransformer() {
			//	        protected void internalTransform(String phaseName,
			//	            Map options) {
			//	          System.err.println(Scene.v().getApplicationClasses());
			//	        }
			//	      }));
				  //soot.Main.main(args);
			PackManager.v().runPacks();
			//PackManager.v().writeOutput();
	}
	
		
		
		static class MyBodyTransformer extends BodyTransformer{

			@Override
			protected void internalTransform(Body body, String arg0, Map arg1) {
				if (body.getMethod().getDeclaringClass().getName().startsWith("com.nkt")) {
					System.err.println(body.getMethod().getDeclaringClass().getName());
					Iterator<Unit> i = body.getUnits().snapshotIterator();
					while (i.hasNext()) {
						Unit u = i.next();
						if(u.toString().contains("execute")){
							System.err.println("found one in" + body.getMethod().getDeclaringClass().getName());
						}
					}
				}
			}

		}
		
		static class MySceneTransformer extends SceneTransformer{

			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				
				for(SootClass c : Scene.v().getApplicationClasses())
					if(c.getName().startsWith("com.aps"))
					for(SootMethod m : c.getMethods())
						if(m.isConcrete())
						{
							Body body = m.getActiveBody();
							Iterator<Unit> i = body.getUnits().snapshotIterator();
							while (i.hasNext()) {
								Unit u = i.next();
								System.out.println(u.toString());
							}
						}
				
			}
			
		}
	
}
