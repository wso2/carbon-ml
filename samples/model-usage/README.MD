WSO2 Machine Learner Sample - Using a downloaded ML Model generated using WSO2 ML in plain Java code
====================================================================================================

Generated Model
===============

Path to model     : resources/downloaded-ml-model
Dataset used      : https://archive.ics.uci.edu/ml/datasets/Pima+Indians+Diabetes
Algorithm         : Logistic-Regression
Response variable : Class

To build the sample
===================
mvn clean install

To run the sample
=================
mvn exec:java

Outcome
=======
Running the sample will print the details of the MLModel and the prediction as below.

INFO  [MLModelUsageSample] - Algorithm Type : Classification
INFO  [MLModelUsageSample] - Algorithm Name : LOGISTIC_REGRESSION
INFO  [MLModelUsageSample] - Response Variable : Class
INFO  [MLModelUsageSample] - Features : [Feature [name=Age, index=7, type=NUMERICAL, imputeOption=DISCARD, include=true],
Feature [name=BMI, index=5, type=NUMERICAL, imputeOption=DISCARD, include=true],
Feature [name=DBP, index=2, type=NUMERICAL, imputeOption=DISCARD, include=true],
Feature [name=DPF, index=6, type=NUMERICAL, imputeOption=DISCARD, include=true],
Feature [name=NumPregnancies, index=0, type=NUMERICAL, imputeOption=DISCARD, include=true],
Feature [name=PG2, index=1, type=NUMERICAL, imputeOption=DISCARD, include=true],
Feature [name=SI2, index=4, type=NUMERICAL, imputeOption=DISCARD, include=true],
Feature [name=TSFT, index=3, type=NUMERICAL, imputeOption=DISCARD, include=true]]

INFO  [MLModelUsageSample] - Predictions : [0, 0]

