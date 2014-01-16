package edu.cmu.graphchi.walks.deprecated;

import edu.cmu.graphchi.ChiFilenames;
import edu.cmu.graphchi.ChiVertex;
import edu.cmu.graphchi.GraphChiContext;
import edu.cmu.graphchi.GraphChiProgram;
import edu.cmu.graphchi.datablocks.IntConverter;
import edu.cmu.graphchi.engine.GraphChiEngine;
import edu.cmu.graphchi.engine.VertexInterval;
import edu.cmu.graphchi.util.IdInt;
import edu.cmu.graphchi.util.Toplist;
import edu.cmu.graphchi.walks.WalkManager;
import edu.cmu.graphchi.walks.WalkSnapshot;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

/**
 * Launch millions (?) of random walks and record the
 * hops for each source. This version can be used only for computing
 * distribution of the source-destinations. For recording the actual
 * paths, use DrunkardMobForPaths
 * Done partially during authors internship at Twitter, Fall 2012.
 * @author Aapo Kyrola, akyrola@cs.cmu.edu
 */
public class DrunkardMob implements GraphChiProgram<Integer, Float> {

    private WalkManager walkManager;
    private WalkSnapshot curWalkSnapshot;

    public DrunkardMob() {
    }

    private static final double RESETPROB = 0.15;

    public void update(ChiVertex<Integer, Float> vertex, GraphChiContext context) {
        int[] walksAtMe = curWalkSnapshot.getWalksAtVertex(vertex.getId(), true);
        if (context.getIteration() == 0) vertex.setValue(0);
        if (walksAtMe == null) return;

        int walkLength = walksAtMe.length;

        int numWalks = 0;
        for(int i=0; i < walkLength; i++) {
            int walk = walksAtMe[i];
            boolean hop = walkManager.hop(walk);
            // Choose a random destination and move the walk forward
            int dst;
            if (vertex.getId() != walkManager.getSourceVertex(walk)) {
                numWalks++;
            }
            if (vertex.numOutEdges() > 0 && (context.getIteration() == 0 || Math.random() > RESETPROB)) {
                dst = vertex.getRandomOutNeighbor();
            } else {
                // Dead end!
                dst = walkManager.getSourceVertex(walk);
            }
            walkManager.updateWalk(walkManager.sourceIdx(walk), dst, !hop);
            context.getScheduler().addTask(dst);

        }
        vertex.setValue(vertex.getValue() + numWalks);
    }


    public void beginIteration(GraphChiContext ctx) {
        if (ctx.getIteration() == 0) {
            ctx.getScheduler().removeAllTasks();
            walkManager.populateSchedulerWithSources(ctx.getScheduler());
        }
    }

    public void endIteration(GraphChiContext ctx) {}

    /**
     * At the start of interval - grab the snapshot of walks
     */
    public void beginSubInterval(GraphChiContext ctx, final VertexInterval interval) {
        long t = System.currentTimeMillis();
        curWalkSnapshot = walkManager.grabSnapshot(interval.getFirstVertex(), interval.getLastVertex());
        System.out.println("Grab snapshot took " + (System.currentTimeMillis() - t) + " ms.");

        String walkDir = System.getProperty("walk.dir", ".");
        final String filename = walkDir + "/walks_.dat";
        if (ctx.getIteration() == 0) { // NOTE, temporary hack to save disk space but have the same I/O cost for testing
            new File(filename).delete();
        }

        // Launch a thread to dump
        final WalkSnapshot snapshot = curWalkSnapshot;
       synchronized (filename.intern()) {
           try {
               walkManager.dumpToFile(snapshot, filename);
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
    }

    public void endSubInterval(GraphChiContext ctx, final VertexInterval interval) {
        curWalkSnapshot.restoreUngrabbed();
        curWalkSnapshot = null; // Release memory
    }

    public void beginInterval(GraphChiContext ctx, VertexInterval interval) {}

    public void endInterval(GraphChiContext ctx, VertexInterval interval) {}

    public static void main(String[] args) throws  Exception {

        String baseFilename = args[0];

        if (args.length > 1) {
            int nShards = Integer.parseInt(args[1]);
            int nSources = Integer.parseInt(args[2]);
            int walksPerSource = Integer.parseInt(args[3]);
            int maxHops = Integer.parseInt(args[4]);

            System.out.println("Walks will start from " + nSources + " sources.");
            System.out.println("Going to start " + walksPerSource + " walks per source.");
            System.out.println("Max hops: " + maxHops);

            /* Delete vertex data */
            File vertexDataFile = new File(ChiFilenames.getFilenameOfVertexData(baseFilename, new IntConverter(), false));
            if (vertexDataFile.exists()) {
                vertexDataFile.delete();
            }

            /* Initialize GraphChi engine */
            GraphChiEngine<Integer, Float> engine = new GraphChiEngine<Integer, Float>(baseFilename, nShards);
            engine.setEdataConverter(null);
            engine.setVertexDataConverter(new IntConverter());
            engine.setModifiesInedges(false);
            engine.setModifiesOutedges(false);
            engine.setEnableScheduler(true);
            engine.setOnlyAdjacency(true);
            engine.setDisableInedges(true);
            engine.setMemoryBudgetMb(1200);
            engine.setUseStaticWindowSize(false); // Disable dynamic window size detection
            engine.setEnableDeterministicExecution(false);
            engine.setAutoLoadNext(false);
            engine.setMaxWindow(2000000); // Handle maximum 2M vertices a time.

            long t1 = System.currentTimeMillis();

            /* Initialize application object */
            DrunkardMob mob = new DrunkardMob();

            /* Initialize Random walks */
            int nVertices = engine.numVertices();
            mob.walkManager = new WalkManager(nVertices, nSources);

            for(int i=0; i < nSources; i++) {
                int source = 234224 + i;
                mob.walkManager.addWalkBatch(source, walksPerSource);
            }
            mob.walkManager.initializeWalks();

            System.out.println("Configured " + mob.walkManager.getTotalWalks() + " walks in " +
                    (System.currentTimeMillis() - t1) + " ms");


            /* Run */
            engine.run(mob, maxHops + 1);

            System.out.println("Ready. Going to output...");

            TreeSet<IdInt> top20 = Toplist.topListInt(baseFilename, engine.numVertices(), 20);
            int i = 0;
            for(IdInt vertexRank : top20) {
                System.out.println(++i + ": " +
                        engine.getVertexIdTranslate().backward(vertexRank.getVertexId()) + " = " + vertexRank.getValue());
            }
            System.out.println("Finished.");
        }

    }
}
