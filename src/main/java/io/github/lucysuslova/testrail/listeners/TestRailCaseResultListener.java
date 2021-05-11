package io.github.lucysuslova.testrail.listeners;

import com.codepine.api.testrail.model.Result;
import io.github.lucysuslova.testrail.Status;
import io.github.lucysuslova.testrail.api.TestRailApi;
import io.github.lucysuslova.testrail.utils.TestRailUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestRailCaseResultListener extends TestListenerAdapter implements ISuiteListener {

    private TestRailApi testRailApi = new TestRailApi();

    @Override
    public void onTestSuccess(ITestResult tr) {
        super.onTestSuccess(tr);
        if (runExists()) {
            Arrays.stream(getCaseIds(tr)).forEach(caseId -> {
                Result result = new Result()
                        .setStatusId(Status.PASSED.id)
                        .setComment("Automated test passed successfully." + "\n" +
                                String.format("Test script: %s.%s %s", tr.getInstanceName(), tr.getMethod().getMethodName(), getCurrentParam(tr)));
                testRailApi.updateResult(Integer.parseInt(getRunId()), caseId, result);
            });
        }
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        super.onTestFailure(tr);
        if (runExists()) {
            Arrays.stream(getCaseIds(tr)).forEach(caseId -> {
                Result result = new Result()
                        .setStatusId(Status.FAILED.id)
                        .setComment("Automated test failed." + "\n" +
                                String.format("Test script: %s.%s %s", tr.getInstanceName(), tr.getMethod().getMethodName(),
                                        getCurrentParam(tr)) + "\n" +
                                String.format("Error: %s", tr.getThrowable().getMessage()));
                testRailApi.updateResult(Integer.parseInt(getRunId()), caseId, result);
            });
        }
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        super.onTestSkipped(tr);
        if (runExists()) {
            Arrays.stream(getCaseIds(tr)).forEach(caseId -> {
                Result result = new Result()
                        .setStatusId(Status.RETEST.id)
                        .setComment("Automated test was skipped, need to retest." + "\n" +
                                String.format("Test script: %s.%s %s", tr.getInstanceName(), tr.getMethod().getMethodName(), getCurrentParam(tr)));
                testRailApi.updateResult(Integer.parseInt(getRunId()), caseId, result);
            });
        }
    }

    @Override
    public void onFinish(ISuite suite) {
        if (runExists()) {
            int[] testRailExecutedCaseIds = testRailApi.getCaseIds(Integer.parseInt(getRunId()));
            IntStream.of(testRailExecutedCaseIds).forEach(this::recheckTestRailResults);
        }
    }

    private int[] getCaseIds(ITestResult tr) {
        Method method = tr
                .getMethod()
                .getConstructorOrMethod()
                .getMethod();
        return TestRailUtils.getCaseIds(method);
    }

    private String getRunId() {
        return System.getProperty("runId");
    }

    private String getCurrentParam(ITestResult tr) {
        return tr.getMethod().isDataDriven() ? "\n" + String.format("Parameter: %s", getParameterValues(tr)) : "";
    }

    private void recheckTestRailResults(int testId) {
        int[] testCaseStatuses = testRailApi.getCaseStatuses(Integer.parseInt(getRunId()), testId);

        boolean containsNegativeStatus = testCaseStatuses.length > 1
                && IntStream.of(testCaseStatuses).anyMatch(id ->
                id == Status.FAILED.id
                        || id == Status.BLOCKED.id
                        || id == Status.RETEST.id
        );

        if (containsNegativeStatus) {
            Result result = new Result()
                    .setStatusId(Status.FAILED.id);
            testRailApi.updateResult(Integer.parseInt(getRunId()), testId, result);
        }
    }

    private boolean runExists() {
        return !StringUtils.isBlank(getRunId());
    }

    private String getParameterValues(ITestResult tr) {
        return Arrays.stream(tr.getParameters())
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

}
