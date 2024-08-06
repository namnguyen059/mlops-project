import pandas as pd
import numpy as np
import argparse

# Generate synthetic data
num_samples = 1000
feature1 = np.random.rand(num_samples) * 100  # Random values for feature1
feature2 = np.random.rand(num_samples) * 50   # Random values for feature2

# Generate target variable (price) with some noise
price = 50 + 0.5 * feature1 + 2 * feature2 + np.random.randn(num_samples) * 10

# Create DataFrame
data = pd.DataFrame({
    'feature1': feature1,
    'feature2': feature2,
    'price': price
})

# Argument parsing
parser = argparse.ArgumentParser()
parser.add_argument('--output', type=str, default='house_prices(live).csv', help='Output file for the generated data')
args = parser.parse_args()

# Save to CSV
data.to_csv(args.output, index=False)

print(f"{args.output} has been generated!")
