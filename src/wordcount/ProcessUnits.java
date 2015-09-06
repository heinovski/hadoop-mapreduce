package wordcount;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

public class ProcessUnits {
	// Mapper class
	public static class E_EMapper extends MapReduceBase
			implements Mapper<LongWritable, /* Input key Type */
			Text, /* Input value Type */
			Text, /* Output key Type */
			IntWritable> /* Output value Type */
	{

		// Map function
		@Override
		public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			String line = value.toString();
			String lasttoken = null;
			StringTokenizer s = new StringTokenizer(line, "\t");
			String year = s.nextToken();

			while (s.hasMoreTokens()) {
				lasttoken = s.nextToken();
			}

			int avgprice = Integer.parseInt(lasttoken);
			output.collect(new Text(year), new IntWritable(avgprice));
		}
	}

	// Reducer class
	public static class E_EReduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {

		// Reduce function
		@Override
		public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output,
				Reporter reporter) throws IOException {
			int maxavg = 30;
			int val = Integer.MIN_VALUE;

			while (values.hasNext()) {
				if ((val = values.next().get()) > maxavg) {
					output.collect(key, new IntWritable(val));
				}
			}

		}
	}

	// Main function
	public static void main(String args[]) throws Exception {
		JobConf conf = new JobConf();

		conf.setJobName("max_eletricityunits");
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);
		conf.setMapperClass(E_EMapper.class);
		conf.setCombinerClass(E_EReduce.class);
		conf.setReducerClass(E_EReduce.class);
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
	}
}