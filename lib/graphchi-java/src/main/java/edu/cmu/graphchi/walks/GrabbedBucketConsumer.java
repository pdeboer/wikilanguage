package edu.cmu.graphchi.walks;


public interface GrabbedBucketConsumer {
     void consume(int firstVertexInBucket, int[] walkBucket, int len);
}
