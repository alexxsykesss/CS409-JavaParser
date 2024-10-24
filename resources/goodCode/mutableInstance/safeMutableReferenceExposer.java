package goodCode.mutableInstance;

public class safeMutableReferenceExposer {

    private mutableObject mObj;

    public safeMutableReferenceExposer(){
        mObj = new mutableObject(10);
    }

    public void displayData(){
        mObj.displayData();
    }

    // returns a copy of private mutable class member
    public mutableObject returnCopy() throws CloneNotSupportedException {
        return mObj.clone();
    }
}

