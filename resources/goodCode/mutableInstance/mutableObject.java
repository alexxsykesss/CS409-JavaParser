package goodCode.mutableInstance;

public class mutableObject implements Cloneable{
    private int data;

    public mutableObject(int val){
        data = val;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public mutableObject clone() throws CloneNotSupportedException{
        super.clone();
        mutableObject tmp = new mutableObject(data);
        return tmp;
    }

    public void displayData(){
        System.out.println("data: "+data);
    }
}
