package io.github.lucysuslova.testrail;

public enum Status {

    //ids for corresponding statuses in TestRail
    PASSED(1),
    BLOCKED(2),
    RETEST(4),
    FAILED(5);

    public int id;

    Status(int id) {
        this.id = id;
    }

}
