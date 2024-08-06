import xgboost as xgb # type: ignore
import pandas as pd
from sklearn.model_selection import train_test_split # type: ignore
from sklearn.metrics import mean_squared_error # type: ignore

# Load data from CSV file
file_path = '/Users/nguyennam/Desktop/Mlops/house_prices.csv'
df = pd.read_csv(file_path)

# Separate features and target variable
X = df[['feature1', 'feature2']]
y = df['price']

# Split data into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Convert data into DMatrix format for XGBoost
dtrain = xgb.DMatrix(X_train, label=y_train)
dtest = xgb.DMatrix(X_test, label=y_test)

# Define parameters for XGBoost
params = {
    'objective': 'reg:squarederror',  # regression task
    'eval_metric': 'rmse',  # evaluation metric
    'max_depth': 6,  # maximum depth of the tree
    'eta': 0.1,  # learning rate
    'subsample': 0.8,  # fraction of samples used for fitting the trees
    'colsample_bytree': 0.8  # fraction of features used for fitting the trees
}

# Train the model
num_round = 100  # number of boosting rounds
model = xgb.train(params, dtrain, num_round)

# Make predictions on the test data
y_pred = model.predict(dtest)

# Evaluate the model
rmse = mean_squared_error(y_test, y_pred, squared=False)
print(f"RMSE: {rmse}")

# Example of saving the model (for MLOps, you would typically save to a storage or version control system)
model.save_model('house_price_prediction.model')
