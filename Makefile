javac -classpath lib/commons-logging-1.2.jar:~lib/hadoop-mapreduce-client-core-2.7.1.jar:lib/hadoop-common-2.7.1.jar:lib/mapreduce/* -sourcepath src -d bin src/com/clustering/mapreduce/KMeansClusteringJob.java
jar -cvf kmeans.jar -C bin/ .
