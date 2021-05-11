package io.github.lucysuslova.testrail.api;

import com.codepine.api.testrail.TestRail;
import com.codepine.api.testrail.model.Result;
import com.codepine.api.testrail.model.ResultField;
import com.codepine.api.testrail.model.Run;
import com.codepine.api.testrail.model.Test;

import java.util.List;

public class TestRailApi {

    private static final TestRail TEST_RAIL = TestRailClient.getInstance();

    public void updateResult(int runId, int caseId, Result result) {
        List<ResultField> customResultFields = TEST_RAIL.resultFields().list().execute();
        TEST_RAIL.results()
                .addForCase(runId, caseId, result, customResultFields).execute();
    }

    public int[] getCaseIds(int runId) {
        List<Test> tests = TEST_RAIL
                .tests()
                .list(runId)
                .execute();
        return tests.stream()
                .mapToInt(Test::getCaseId).toArray();
    }

    public int addNewTestRun(int projectId, Run newRun) {
        Run run = TEST_RAIL.runs()
                .add(projectId, newRun)
                .execute();
        return run.getId();
    }

    public int[] getCaseStatuses(int runId, int testCaseId) {
        List<ResultField> customResultFields = TEST_RAIL.resultFields().list().execute();
        List<Result> testResults = TEST_RAIL.results().listForCase(runId, testCaseId, customResultFields).execute();
        return testResults.stream().mapToInt(Result::getStatusId).toArray();
    }

}
