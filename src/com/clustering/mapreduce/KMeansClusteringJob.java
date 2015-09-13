package com.clustering.mapreduce;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import com.clustering.model.ClusterCenter;
import com.clustering.model.Vector;

public class KMeansClusteringJob {

	private static final Log LOG = LogFactory.getLog(KMeansClusteringJob.class);

	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {

		int iteration = 1;
		Configuration conf = new Configuration();
		conf.set("num.iteration", iteration + "");

		Path in = new Path("files/clustering/import/data");
		Path center = new Path("files/clustering/import/center/cen.seq");
		conf.set("centroid.path", center.toString());
		Path out = new Path("files/clustering/depth_1");

		Job job = Job.getInstance(conf);
		job.setJobName("KMeans Clustering");

		job.setMapperClass(KMeansMapper.class);
		job.setReducerClass(KMeansReducer.class);
		job.setJarByClass(KMeansMapper.class);

		SequenceFileInputFormat.addInputPath(job, in);
		FileSystem fs = FileSystem.get(conf);
		if (fs.exists(out)) {
			fs.delete(out, true);
		}

		if (fs.exists(center)) {
			fs.delete(out, true);
		}

		if (fs.exists(in)) {
			fs.delete(in, true);
		}

		/*
		 * Add 2 cluster centers.
		 */
		writeExampleCenters(conf, center, fs);

		/*
		 * Add 9 data vectors.
		 */
		writeExampleVectors(conf, in, fs);

		SequenceFileOutputFormat.setOutputPath(job, out);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		job.setOutputKeyClass(ClusterCenter.class);
		job.setOutputValueClass(Vector.class);

		job.waitForCompletion(true);

		long counter = job.getCounters().findCounter(KMeansReducer.Counter.CONVERGED).getValue();
		iteration++;
		while (counter > 0) {
			conf = new Configuration();
			conf.set("centroid.path", center.toString());
			conf.set("num.iteration", iteration + "");
			job = Job.getInstance(conf);
			job.setJobName("KMeans Clustering " + iteration);

			job.setMapperClass(KMeansMapper.class);
			job.setReducerClass(KMeansReducer.class);
			job.setJarByClass(KMeansMapper.class);

			in = new Path("files/clustering/depth_" + (iteration - 1) + "/");
			out = new Path("files/clustering/depth_" + iteration);

			SequenceFileInputFormat.addInputPath(job, in);
			if (fs.exists(out))
				fs.delete(out, true);

			SequenceFileOutputFormat.setOutputPath(job, out);
			job.setInputFormatClass(SequenceFileInputFormat.class);
			job.setOutputFormatClass(SequenceFileOutputFormat.class);
			job.setOutputKeyClass(ClusterCenter.class);
			job.setOutputValueClass(Vector.class);

			job.waitForCompletion(true);
			iteration++;
			counter = job.getCounters().findCounter(KMeansReducer.Counter.CONVERGED).getValue();
		}

		Path result = new Path("files/clustering/depth_" + (iteration - 1) + "/");

		FileStatus[] stati = fs.listStatus(result);
		for (FileStatus status : stati) {
			if (!status.isDir() && !status.getPath().toString().contains("/_")) {
				Path path = status.getPath();
				LOG.info("FOUND " + path.toString());
				try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf)) {
					ClusterCenter key = new ClusterCenter();
					Vector v = new Vector();
					while (reader.next(key, v)) {
						LOG.info(key + " / " + v);
					}
					reader.close();
				}
			}
		}
	}

	public static void writeExampleVectors(Configuration conf, Path in, FileSystem fs) throws IOException {
		try (SequenceFile.Writer dataWriter = SequenceFile.createWriter(fs, conf, in, ClusterCenter.class,
				Vector.class)) {
			dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(1, 2));
			dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(16, 3));
			dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(3, 3));
			dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(2, 2));
			dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(2, 3));
			dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(25, 1));
			dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(7, 6));
			dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(6, 5));
			dataWriter.append(new ClusterCenter(new Vector(0, 0)), new Vector(-1, -23));
		}
	}

	public static void writeExampleCenters(Configuration conf, Path center, FileSystem fs) throws IOException {
		try (SequenceFile.Writer centerWriter = SequenceFile.createWriter(fs, conf, center, ClusterCenter.class,
				IntWritable.class)) {
			final IntWritable value = new IntWritable(0);
			centerWriter.append(new ClusterCenter(new Vector(1, 1)), value);
			centerWriter.append(new ClusterCenter(new Vector(5, 5)), value);
		}
	}

}
