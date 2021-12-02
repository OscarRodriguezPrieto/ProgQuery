package es.uniovi.reflection.progquery.pdg;

public class MutatedParamInCallInfo {

    private int argNumber;
    private boolean isMay, isVargArgs;

    public MutatedParamInCallInfo(int argNumber, boolean isMay, boolean isVargArgs) {
        this.argNumber = argNumber;
        this.isMay = isMay;
        this.isVargArgs = isVargArgs;
    }

    public int getArgNumber() {
        return argNumber;
    }

    public boolean isMay() {
        return isMay;
    }

    public boolean isVargArgs() {
        return isVargArgs;
    }
}
