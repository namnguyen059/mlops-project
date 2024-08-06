FROM --platform=linux/amd64 python:3.8-slim-buster AS build

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

FROM --platform=linux/amd64 python:3.8-slim-buster

WORKDIR /app

RUN pip install --no-cache-dir \
    flask \
    xgboost \
    cassandra-driver \
    pandas \
    numpy \
    joblib \
    scikit-learn

COPY --from=build /app /app

EXPOSE 5000

CMD ["python", "service.py"]
