from pyspark.sql import SparkSession

spark = SparkSession.builder \
    .appName("Preprocess House Prices") \
    .config("hive.metastore.uris", "thrift://hive-metastore:9083") \
    .enableHiveSupport() \
    .getOrCreate()

# Load data from HDFS
df = spark.read.csv("hdfs://namenode:8020/data/house_prices.csv", header=True, inferSchema=True)

# Perform data preprocessing (example: select relevant features)
processed_df = df.select("feature1", "feature2", "price")

# Save processed data to Hive
processed_df.write.mode("overwrite").saveAsTable("default.house_prices_features")

spark.stop()
