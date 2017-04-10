package de.fzi.cep.sepa.flink.samples.count.aggregate;

import de.fzi.cep.sepa.flink.FlinkDeploymentConfig;
import de.fzi.cep.sepa.flink.FlinkSepaRuntime;
import org.apache.commons.collections.map.HashedMap;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CountAggregateProgram extends FlinkSepaRuntime<CountAggregateParameters>{

	private static String AGGREGATE_COUNT = "aggregate_count";
	public CountAggregateProgram(CountAggregateParameters params) {
		super(params);
		this.streamTimeCharacteristic = TimeCharacteristic.EventTime;
	}

	public CountAggregateProgram(CountAggregateParameters params, FlinkDeploymentConfig config) {
		super(params, config);
	}

	@Override
	protected DataStream<Map<String, Object>> getApplicationLogic(
			DataStream<Map<String, Object>>... messageStream) {

//		this.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		List<String> groupBy = params.getGroupBy();

		DataStream<Map<String, Object>> result = messageStream[0]
//                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Map<String, Object>>() {
//
//					@Override
//					public long extractAscendingTimestamp(Map<String, Object> element) {
//					    System.out.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format((long) element.get("tpep_pickup_datetime")));
//						return (long) element.get("tpep_pickup_datetime");
//					}
//				})
				.map(new MapFunction<Map<String, Object>, Tuple2<String, Map<String, Object>>>() {
					@Override
					public Tuple2<String, Map<String, Object>> map(Map<String, Object> value) throws Exception {

						String newKey = "";

						for (String s : groupBy) {
							newKey = newKey + value.get(s).toString();
						}

						return new Tuple2<String, Map<String, Object>>(newKey, value);
					}
				})
				.assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Tuple2<String, Map<String, Object>>>() {

					@Override
					public long extractAscendingTimestamp(Tuple2<String, Map<String, Object>> element) {
						return (long) element.f1.get("tpep_pickup_datetime");
					}
				})
				.keyBy(0)
				.timeWindow(params.getTimeWindowSize(), params.getSlideWindowSize())
				.apply(new MyWindow2Function())
				.map(new MapFunction< Tuple2<String, Map<String, Object>>, Map<String, Object>>() {
					@Override
					public Map<String, Object> map(Tuple2<String, Map<String, Object>> value) throws Exception {
						return value.f1;
					}
				});

		return result;
	}

	private static class MyWindow2Function implements WindowFunction<Tuple2<String,Map<String,
			Object>>, Tuple2<String, Map<String, Object>>, Tuple, TimeWindow> {

		@Override
		public void apply(Tuple tuple, TimeWindow timeWindow, Iterable<Tuple2<String, Map<String, Object>>> iterable,
						  Collector<Tuple2<String, Map<String, Object>>> collector) throws Exception {

			Map<String, Object> result = new HashMap<>();
			String key = "";
			Iterator<Tuple2<String, Map<String, Object>>> iterator = iterable.iterator();
			int count = 0;

			while(iterator.hasNext()) {
				Tuple2<String, Map<String, Object>> tmp = iterator.next();
				count++;

				result.put("vendor_id", tmp.f1.get("vendor_id"));
				key = tmp.f0;
			}

			System.out.println("============================================");
			System.out.println("Window start: " + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(timeWindow.getStart()));
			System.out.println("Vendor Id: " + result.get("vendor_id") + " Count: " + count);
			System.out.println("Window end: " + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(timeWindow.getEnd()));
			System.out.println("============================================");

			result.put(AGGREGATE_COUNT, count);
			collector.collect(new Tuple2<String, Map<String, Object>>(key, result));
		}
	}

}