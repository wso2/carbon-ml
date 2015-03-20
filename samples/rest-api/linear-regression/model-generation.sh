#!/bin/bash

echo "testing Linear Regression workflow"

# Die on any error:
set -e

echo "#create a dataset"
path=$(pwd)

# General commands
if [ "$(uname)" == "Darwin" ]; then
    # Do something under Mac OS X platform
        sed -i '' "s~PATH~$path~g"  create-dataset
else
    # Do something else under some other platform
        sed -i "s~PATH~$path~g"  create-dataset
fi

curl -X POST -d @'create-dataset' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/datasets -k
sleep 5
# changing create-dataset file back to original
if [ "$(uname)" == "Darwin" ]; then
    # Do something under Mac OS X platform
        sed -i '' "s~$path~PATH~g"  create-dataset
else
    # Do something else under some other platform
        sed -i "s~$path~PATH~g"  create-dataset
fi

# creating a project
echo "#creating a project"
curl -X POST -d @'create-project' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/projects -k
sleep 2

#getting the project id
echo "#getting the project id"
#curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/projects/lrboston -k
sleep 2

#creating an analysis
echo "creating an analysis"
curl -X POST -d @'create-analysis' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses -k
sleep 2

#getting analysis id
echo "getting analysis id"
#curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses/lrboston-analysis -k

#setting model configs
echo "#setting model configs"
curl -X POST -d @'create-model-config' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses/1/configurations -k -v
sleep 2

echo "#adding default features with customized options"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses/1/features/defaults -k -v -d @'customized-features'
sleep 2

echo "#adding default hyper params"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses/1/hyperParams/defaults -k -v
sleep 2

echo "#create model"
curl -X POST -d @'create-model' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/models -k

echo "#add model storage information"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/models/1/storages -k -v -d @'create-model-storage'
sleep 2

echo "#building the model"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/models/1 -k -v

sleep 30
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/models/1/predict -k -v -d @'prediction-test'


