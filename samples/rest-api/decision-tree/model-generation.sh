#!/bin/bash
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
#get valueset id
echo "#create a project"
curl -X POST -d @'create-project' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/projects -k
sleep 2
echo "#get the project id"
#curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/projects/nirmal1 -k
sleep 2
#update the create-analysis payload
echo "#create an analysis"
curl -X POST -d @'create-analysis' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses -k
sleep 2
echo "#get analysis id"
#curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses/nirmal-analysis1 -k
sleep 2
echo "#setting model configs"
curl -X POST -d @'create-model-config' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses/1/configurations -k -v
sleep 2
echo "#add default features with customized options"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses/1/features/defaults -k -v -d @'customized-features'
sleep 2
echo "#add default hyper params"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses/1/hyperParams/defaults -k -v
sleep 2
echo "#create model"
curl -X POST -d @'create-model' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/models -k
sleep 2
echo "#add model storage information"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/models/1/storages -k -v -d @'create-model-storage'
sleep 2
echo "#build model"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/models/1 -k -v
sleep 10
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/models/1/predict -k -v -d @'prediction-test'
