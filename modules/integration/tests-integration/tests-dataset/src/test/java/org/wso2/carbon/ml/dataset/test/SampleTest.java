package org.wso2.carbon.ml.dataset.test;

import org.testng.annotations.BeforeClass;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.ml.common.utils.MLIntegrationBaseTest;

public class SampleTest extends MLIntegrationBaseTest {
    @BeforeClass(alwaysRun = true, groups = {"wso2.ml"})

    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
    }
}
