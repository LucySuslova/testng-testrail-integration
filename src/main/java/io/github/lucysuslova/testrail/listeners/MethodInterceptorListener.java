package io.github.lucysuslova.testrail.listeners;

import com.codepine.api.testrail.model.Run;
import io.github.lucysuslova.testrail.api.TestRailApi;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static io.github.lucysuslova.testrail.utils.TestRailUtils.getCaseIds;
import static io.github.lucysuslova.testrail.utils.TestRailUtils.isMethodValidForTestRail;


public class MethodInterceptorListener implements IMethodInterceptor {

    private static final String EXISTING_RUN = "EXISTING_RUN";
    private static final String NEW_RUN = "NEW_RUN";
    private static final String NO_RUN = "NO_RUN";
    private TestRailApi testRailApi = new TestRailApi();

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {

        String runId = System.getProperty("runId");
        String reportToTestRail = System.getProperty("reportToTestRail");

        String runType = getRunType(runId, reportToTestRail);
        List<IMethodInstance> result = new ArrayList<>();
        switch (runType) {
            case EXISTING_RUN -> {
                result.addAll(handleExistingRun(methods, runId));
                return result;
            }
            case NEW_RUN -> {
                result = handleNewRun(methods, context);
                return result;
            }
            case NO_RUN -> {
                return methods;
            }
            default -> throw new IllegalArgumentException("Run type is not recognized: " + runType);
        }
    }

    private String getRunDescription() {
        return "Automated test run based on automated scripts.";
    }

    private String getRunType(String runId, String reportToTestRail) {
        if (null != runId && !runId.isBlank())
            return EXISTING_RUN; //test run already created in test rail, just need to update results
        else if ((null == runId || runId.isBlank()) && "true".equals(reportToTestRail))
            return NEW_RUN; //create new test run automatically
        else if ((null == runId || runId.isBlank()) && "false".equals(reportToTestRail))
            return NO_RUN; //no reporting to test rail required

        return runId;
    }

    private Integer addNewRun(ITestContext context, List<Integer> testCaseIds) {
        Run testRun = new Run();
        String testRunName = String.format("[Automated test run] %s",
                context.getSuite().getName());
        testRun
                .setName(testRunName)
                .setSuiteId(111) //move to properties
                .setDescription(getRunDescription())
                .setIncludeAll(false)
                .setCaseIds(testCaseIds);

        int createdRunId = testRailApi.addNewTestRun(1, testRun); //move to properties project id
        System.setProperty("runId", String.valueOf(createdRunId));
        return createdRunId;
    }

    private List<IMethodInstance> handleNewRun(List<IMethodInstance> methods, ITestContext context) {
        List<IMethodInstance> result = new ArrayList<>();
        List<Integer> testCaseIds = new ArrayList<>();
        Set<Integer> uniqueTestCaseIds = new HashSet<>();
        for (IMethodInstance m : methods) {
            Method method = m.getMethod().getConstructorOrMethod().getMethod();
            if (isMethodValidForTestRail(method)) {
                int[] testCaseIdsForThisTest = getCaseIds(method);
                IntStream.of(testCaseIdsForThisTest).forEach(t -> uniqueTestCaseIds.add(t));
                result.add(m);
            }
        }
        testCaseIds.addAll(uniqueTestCaseIds);
        addNewRun(context, testCaseIds);
        return result;
    }

    private Set<IMethodInstance> handleExistingRun(List<IMethodInstance> methods, String runId) {
        Set<IMethodInstance> uniqueMethods = new HashSet<>();
        int[] testRailCaseIds = testRailApi.getCaseIds(Integer.parseInt(runId));
        for (IMethodInstance m : methods) {
            Method method = m.getMethod().getConstructorOrMethod().getMethod();
            if (isMethodValidForTestRail(method)) {
                IntStream.of(getCaseIds(method)).forEach(t -> {
                    if (ArrayUtils.contains(testRailCaseIds, t)) uniqueMethods.add(m);
                });
            }
        }
        return uniqueMethods;
    }
}
