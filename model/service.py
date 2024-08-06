from flask import Flask, request, jsonify
import xgboost as xgb
from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider
import uuid
import os

app = Flask(__name__)

# Cassandra connection parameters
cassandra_host = 'host.docker.internal'
cassandra_port = 9042
cassandra_username = 'cassandra'
cassandra_password = 'cassandra'
keyspace = 'model_repo'
table = 'models'
model_id = uuid.UUID('14c372cb-2abd-446b-bc32-2a01300567bd')  # The actual UUID of the model

def load_model_from_cassandra():
    auth_provider = PlainTextAuthProvider(username=cassandra_username, password=cassandra_password)
    cluster = Cluster([cassandra_host], port=cassandra_port, auth_provider=auth_provider)
    session = cluster.connect(keyspace)

    query = f"SELECT model_file FROM {table} WHERE model_id = %s"
    row = session.execute(query, (model_id,)).one()

    model_file_path = 'loaded_house_price_prediction.model'
    with open(model_file_path, 'wb') as f:
        f.write(row.model_file)

    cluster.shutdown()

    model = xgb.Booster()
    model.load_model(model_file_path)
    return model

model = load_model_from_cassandra()

# Cassandra feature store connection parameters
feature_store_keyspace = 'house_prices'
feature_store_table = 'feature_store'

def get_features_from_cassandra():
    auth_provider = PlainTextAuthProvider(username=cassandra_username, password=cassandra_password)
    cluster = Cluster([cassandra_host], port=cassandra_port, auth_provider=auth_provider)
    session = cluster.connect(feature_store_keyspace)

    query = f"SELECT feature1, feature2 FROM {feature_store_table} LIMIT 1000"
    rows = session.execute(query)

    cluster.shutdown()

    features = []
    for row in rows:
        features.append([row.feature1, row.feature2])
    
    return features

@app.route('/predict', methods=['GET'])
def predict():
    try:
        features = get_features_from_cassandra()
        if not features:
            return jsonify(error="No features found in Cassandra"), 404

        dmatrix = xgb.DMatrix(features)
        predictions = model.predict(dmatrix)
        
        # Convert predictions to a list of values
        predictions_list = predictions.tolist()
        
        return jsonify(predictions=predictions_list)
    except Exception as e:
        return jsonify(error=str(e)), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
