ML Custom Adapter Extension
===========================

This OSGi bundle demonstrates how you could extend WSO2 ML input/output adapters.

1. This bundle is a fragment bundle of org.wso2.carbon.ml.core bundle.
2. You can build the bundle and copy it into {ML_HOME}/repository/components/dropins folder.
3. Once you copy the bundle, you could configure the new adapter using {ML_HOME}/repository/conf/machine-learner.xml file. Please refer to [WSO2 ML documentation](https://docs.wso2.com/display/ML100/WSO2+ML-Specific+Configurations#WSO2ML-SpecificConfigurations-Input/outputhandlingconfigurations). In order to try this sample, you have to change the **file.in** property as below;

```
<Property name="file.in" value="org.wso2.carbon.ml.custom.adapter.input.CustomMLInputAdapter" />
```

